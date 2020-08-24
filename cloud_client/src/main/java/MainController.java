import fileInfo.FileInfo;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import messages.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainController extends AbstractController {

    public TableView<FileInfo> clientTable;
    public TableView<FileInfo> cloudTable;
    public ComboBox<String> clientDisks;
    public TextField clientTextField;
    public Button exit;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        TableColumn<FileInfo, String> typeColumn = new TableColumn<>();
        typeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileType().getName()));
        typeColumn.setPrefWidth(34);

        TableColumn<FileInfo, String> nameColumn = getFormattedNameColumn();
        TableColumn<FileInfo, Long> sizeColumn = getFormattedSizeColumn();

        clientTable.getColumns().addAll(typeColumn, nameColumn, sizeColumn);
        clientTable.getSortOrder().add(typeColumn);

        clientDisks.getItems().clear();
        for (Path path : FileSystems.getDefault().getRootDirectories()) {
            clientDisks.getItems().add(path.toString());
        }
        clientDisks.getSelectionModel().select(0);

        TableColumn<FileInfo, String> nameColumn2 = getFormattedNameColumn();
        TableColumn<FileInfo, Long> sizeColumn2 = getFormattedSizeColumn();
        cloudTable.getColumns().addAll(nameColumn2, sizeColumn2);

        clientTable.setOnMouseClicked(event -> {
            cloudTable.getSelectionModel().clearSelection();
            if (event.getClickCount() == 2) {
                Path path = Paths.get(clientTextField.getText()).resolve(
                        clientTable.getSelectionModel().getSelectedItem().getFileName());
                if (Files.isDirectory(path)) {
                    updateSelectedClientDir(path);
                }
            }
        });

        updateSelectedClientDir(Paths.get(""));
        updateCloudFilesList();
    }

    public void delCommand(ActionEvent actionEvent) {
        FileInfo toDelete = cloudTable.getSelectionModel().getSelectedItem();
        String del;
        if (toDelete != null) {
            del = toDelete.getFileName();
            FileDeleteMessage fdm = new FileDeleteMessage(del);
            sendMsg(fdm);
            updateCloudFilesList();
        } else {
            del = getSelectedClientFileName();
            if (del == null) {
                alertMsg();
                return;
            }
            try {
                String pathToDel = getCurrentClientPath();
                Path toDel = Paths.get(pathToDel, del);
                System.out.println(toDel.toString());
                Files.deleteIfExists(toDel);
                updateSelectedClientDir(Paths.get(pathToDel));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(del);
    }

    public void uploadCommand(ActionEvent actionEvent) {

        if (getSelectedClientFileName() == null) {
            alertMsg();
            return;
        }
        Path pathToUpload = Paths.get(getCurrentClientPath(), getSelectedClientFileName());
        Thread t = new Thread(() -> {
            if (Files.exists(pathToUpload)) {
                File file = new File(pathToUpload.toString());
                int bufSize = 1024 * 1024 * 10;
                int partsCount = new Long(file.length() / bufSize).intValue();
                System.out.println(file.length());
                if (file.length() % bufSize != 0) {
                    partsCount++;
                }
                FileSendMessage fsm = new FileSendMessage(file.getName(), -1, partsCount, new byte[bufSize]);
                try (FileInputStream fis = new FileInputStream(file)) {
                    for (int i = 0; i < partsCount; i++) {
                        int read = fis.read(fsm.partContent);
                        fsm.partNumber = i + 1;

                        if (read < bufSize) {
                            fsm.partContent = Arrays.copyOfRange(fsm.partContent, 0, read);
                        }
                        sendMsg(fsm);
                        Thread.sleep(10);
                        System.out.println("Send part number " + fsm.partNumber);
                    }

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        updateCloudFilesList();
    }

    public void downloadCommand(ActionEvent actionEvent) {

        FileInfo toDownload = cloudTable.getSelectionModel().getSelectedItem();
        if (toDownload == null) {
            alertMsg();
            return;
        }
        FileRequestMessage frm = new FileRequestMessage(toDownload.getFileName());
        sendMsg(frm);

        Thread t = new Thread(() -> {
            try {
                while (true) {
                    Object dl = in.readObject();
                    FileSendMessage fsm = (FileSendMessage) dl;
                    Path pathToDownload = Paths.get(getCurrentClientPath(), fsm.fileName);
                    if (fsm.partNumber == 1) {
                        if (Files.deleteIfExists(pathToDownload)) {
                            System.out.println("Deleted existing file " + fsm.fileName);
                        }
                        Files.write(pathToDownload, fsm.partContent, StandardOpenOption.CREATE);
                        System.out.println("Downloaded " + fsm.partNumber + "/" + fsm.partsCount);
                    }
                    if (fsm.partNumber > 1) {
                        System.out.println("Downloaded " + fsm.partNumber + "/" + fsm.partsCount);
                        Files.write(pathToDownload, fsm.partContent, StandardOpenOption.APPEND);
                    }
                    if (fsm.partNumber == fsm.partsCount) {
                        System.out.println("File " + fsm.fileName + " downloaded!");
                        System.out.println(new File(pathToDownload.toString()).length());
                        break;
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
                socketClose();
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        updateSelectedClientDir(Paths.get(getCurrentClientPath()));
    }

    public void exitCommand(ActionEvent actionEvent) {
        Platform.exit();
    }

    private void alertMsg() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Result: ");
        alert.setContentText("No file selected");
        alert.show();
    }

    private void updateCloudFilesList() {
        cloudTable.getItems().clear();
        FilesListRequest rfl = new FilesListRequest();
        sendMsg(rfl);
        try {
            Object list = in.readObject();
            if (list instanceof CloudInfoList) {
                CloudInfoList cfl = (CloudInfoList) list;
                List<FileInfo> cloudFilesList = cfl.getListFileInfo();
                cloudTable.getItems().addAll(cloudFilesList);
                cloudTable.sort();
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    public void updateSelectedClientDir(Path path) {
        try {
            clientTextField.setText(path.normalize().toAbsolutePath().toString());

            clientTable.getItems().clear();
            clientTable.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            clientTable.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Failed to update the file list", ButtonType.OK);
            alert.showAndWait();
        }
    }

    private TableColumn<FileInfo, String> getFormattedNameColumn() {
        TableColumn<FileInfo, String> nameColumn = new TableColumn<>("File name");
        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        nameColumn.setPrefWidth(250);
        return nameColumn;
    }

    private TableColumn<FileInfo, Long> getFormattedSizeColumn() {
        TableColumn<FileInfo, Long> sizeColumn = new TableColumn<>("Size");
        sizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        sizeColumn.setPrefWidth(150);

        sizeColumn.setCellFactory(MainController::setSizeTableCell);
        return sizeColumn;
    }

    private static TableCell<FileInfo, Long> setSizeTableCell(TableColumn<FileInfo, Long> column) {
        return new TableCell<FileInfo, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    String text = String.format("%,d bytes", item);
                    if (item >= 1_000L && item < 1_000_000L) {
                        text = String.format("%,d Kb", item / 1000);
                    }
                    if (item >= 1_000_000L) {
                        text = String.format("%,d Mb", item / 1000000);
                    }
                    if (item == -1L) {
                        text = "[DIR]";
                    }
                    setText(text);
                }
            }
        };
    }

    public String getSelectedClientFileName() {
        if (!clientTable.isFocused()) {
            return null;
        }
        return clientTable.getSelectionModel().getSelectedItem().getFileName();
    }

    public String getCurrentClientPath() {
        return clientTextField.getText();
    }

    public void clearSelectionLocalList(MouseEvent mouseEvent) {
        clientTable.getSelectionModel().clearSelection();
    }

    public void btnPathUp(ActionEvent actionEvent) {
        Path upPath = Paths.get(clientTextField.getText()).getParent();
        if (upPath != null) {
            updateSelectedClientDir(upPath);
        }
    }

    public void cmbSelectDisk(ActionEvent actionEvent) {
        updateSelectedClientDir(Paths.get(clientDisks.getSelectionModel().getSelectedItem()));

    }
}

