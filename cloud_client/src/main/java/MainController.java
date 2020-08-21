import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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


    public ListView<String> serv;
    public TableView<FileInfo> tv;
    public TableView<FileInfo> servtv;
    public ComboBox<String> disks;
    public TextField mainTextField;
    private final String clientFilesDir = "clientFiles/";
    private final Path clientFilesPath = Paths.get(clientFilesDir);
    public Button exit;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        getCloudFilesList(serv);
       // updateCloudDir();
//        try {
//            if (!Files.exists(clientFilesPath)) {
//                Files.createDirectory(clientFilesPath);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            socketClose();
//        }
        // формируем таблицу
        //столбец тип файла
        TableColumn<FileInfo, String> typeColumn = new TableColumn<>();
        typeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileType().getName()));
        typeColumn.setPrefWidth(34);


        //столбец имя файла

        TableColumn<FileInfo, String> nameColumn = getNameColumn();
//        TableColumn<FileInfo, String> nameColumn = new TableColumn<>("File name");
//        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
//        nameColumn.setPrefWidth(250);

        //столбец размер файла
        TableColumn<FileInfo, Long> sizeColumn = getSizeColumn();
//        TableColumn<FileInfo, Long> sizeColumn = new TableColumn<>("Size");
//        sizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
//        sizeColumn.setPrefWidth(150);
//
//        sizeColumn.setCellFactory(column -> {
//            return new TableCell<FileInfo, Long>() {
//                @Override
//                protected void updateItem(Long item, boolean empty) {
//                    super.updateItem(item, empty);
//                    if (item == null || empty) {
//                        setText(null);
//                        setStyle("");
//                    } else {
//                        String text = String.format("%,d bytes", item);
//                        if (item >= 1_000L && item < 1_000_000L) {
//                            text = String.format("%,d Kb", item/1000);
//                        }
//                        if (item >= 1_000_000L) {
//                            text = String.format("%,d Mb", item/1000000);
//                        }
//                        if (item == -1L){
//                            text = "[DIR]";
//                        }
//                        setText(text);
//                    }
//                }
//            };
//        });

        disks.getItems().clear();
        for (Path path : FileSystems.getDefault().getRootDirectories()){
            disks.getItems().add(path.toString());
        }
        disks.getSelectionModel().select(0);


        tv.getColumns().addAll(typeColumn, nameColumn, sizeColumn);
        tv.getSortOrder().add(typeColumn);

        //////////////////////////////////////////////////////////////////////////////////////
        //столбец имя файла
        TableColumn<FileInfo, String> nameColumn2 = getNameColumn();

        //столбец размер файла
        TableColumn<FileInfo, Long> sizeColumn2 = getSizeColumn();

        servtv.getColumns().addAll(nameColumn2, sizeColumn2);

///////////////////////////////////////////////////////////////////////////////////////////////////
        tv.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                serv.getSelectionModel().clearSelection();
                if (event.getClickCount() == 2) {
                    Path path = Paths.get(mainTextField.getText()).resolve(
                            tv.getSelectionModel().getSelectedItem().getFileName());
                    if (Files.isDirectory(path)){
                        updateSelectedDir(path);
                    }
                }
            }
        });

        updateSelectedDir(Paths.get(""));
    }

    private TableColumn<FileInfo, String> getNameColumn(){
        TableColumn<FileInfo, String> nameColumn = new TableColumn<>("File name");
        nameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        nameColumn.setPrefWidth(250);
        return nameColumn;
    }

    private TableColumn<FileInfo, Long> getSizeColumn(){
        TableColumn<FileInfo, Long> sizeColumn = new TableColumn<>("Size");
        sizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        sizeColumn.setPrefWidth(150);

        sizeColumn.setCellFactory(column -> {
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
                            text = String.format("%,d Kb", item/1000);
                        }
                        if (item >= 1_000_000L) {
                            text = String.format("%,d Mb", item/1000000);
                        }
                        if (item == -1L){
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });
        return sizeColumn;
    }

    public void updateSelectedDir(Path path){

        try {
            mainTextField.setText(path.normalize().toAbsolutePath().toString());

            tv.getItems().clear();
            tv.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            tv.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING , "Failed to update the file list" , ButtonType.OK);
            alert.showAndWait();
        }
    }

//    public void updateCloudDir(){
//
//        try {
//            servtv.getItems().clear();
//            tv.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
//            tv.sort();
//        } catch (IOException e) {
//            Alert alert = new Alert(Alert.AlertType.WARNING , "Failed to update the file list" , ButtonType.OK);
//            alert.showAndWait();
//        }
//    }


    public String getSelectedFileName() {
        // если таблица не в фокусе возвращаем null
        if (!tv.isFocused()){
            return null;
        }
        return tv.getSelectionModel().getSelectedItem().getFileName();
    }

    public String getCurrentPath(){
        return mainTextField.getText();
    }


    public void delCommand(ActionEvent actionEvent) {

        String del = serv.getSelectionModel().getSelectedItem();

        if (del == null) {
            del = getSelectedFileName();
            if (del == null) {
                alertMsg();
                return;
            }
            try {
                Path toDel = Paths.get(getCurrentPath(), getSelectedFileName());
                System.out.println(toDel.toString());
                Files.deleteIfExists(toDel);
                //refreshLocalFileList(lv);
                updateSelectedDir(Paths.get(getCurrentPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            FileDeleteMessage fdm = new FileDeleteMessage(del);
            System.out.println(fdm.getFileName());
            sendMsg(fdm);
            getCloudFilesList(serv);
        }
    }

    public void uploadCommand(ActionEvent actionEvent) {
        //String upload = lv.getSelectionModel().getSelectedItem();

        if (getSelectedFileName() == null) {
            alertMsg();
            return;
        }
        Path pathToUpload = Paths.get(getCurrentPath(), getSelectedFileName());
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
        getCloudFilesList(serv);
    }

    public void downloadCommand(ActionEvent actionEvent) {

        String download = serv.getSelectionModel().getSelectedItem();
        if (download == null) {
            alertMsg();
            return;
        }
        FileRequestMessage frm = new FileRequestMessage(download);
        System.out.println(frm.getFileName());
        sendMsg(frm);

        Thread t = new Thread(() -> {

            try {
                int count = 0;
                while (true) {
                    Object dl = in.readObject();
                    FileSendMessage fsm = (FileSendMessage) dl;
                    count++;
                    Path pathToDownload = Paths.get(getCurrentPath(), fsm.fileName);
                    if (fsm.partNumber == 1) {
                        //if (Files.deleteIfExists(Paths.get(clientFilesDir + fsm.fileName))) {
                        if (Files.deleteIfExists(pathToDownload)) {
                            System.out.println("Удалили существующий файл");
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
                        System.out.println(count);
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
       // refreshLocalFileList(lv);
        updateSelectedDir(Paths.get(getCurrentPath()));
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

    private void getCloudFilesList(ListView<String> serv) {
        serv.getItems().clear();
        FilesListRequest rfl = new FilesListRequest();
        sendMsg(rfl);
        try {
            Object list = in.readObject();
            if (list instanceof CloudFilesList) {
                CloudFilesList cfl = (CloudFilesList) list;
                List<String> cloudFilesList = cfl.getCloudFilesList();
                cloudFilesList.forEach(s -> serv.getItems().add(s));
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

//    private void refreshLocalFileList(ListView<String> lv) {
//        lv.getItems().clear();
//        File dir = new File(clientFilesDir);
//        String[] fileList = dir.list();
//        if (fileList != null) {
//            for (String file : fileList) {
//                lv.getItems().add(file);
//            }
//        }
//    }
//
//    public void clearSelectionServerList(MouseEvent mouseEvent) {
//       serv.getSelectionModel().clearSelection();
//    }

    public void clearSelectionLocalList(MouseEvent mouseEvent) {
        tv.getSelectionModel().clearSelection();
    }

    public void btnPathUp(ActionEvent actionEvent) {
        Path upPath = Paths.get(mainTextField.getText()).getParent();
        if (upPath != null) {
            updateSelectedDir(upPath);
        }
    }

    public void cmbSelectDisk(ActionEvent actionEvent) {
               updateSelectedDir(Paths.get(disks.getSelectionModel().getSelectedItem()));

    }
}

