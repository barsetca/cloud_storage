import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class MainController extends AbstractController {

    public ListView<String> lv;
    public ListView<String> serv;
    private final String clientFilesDir = "clientFiles/";
    private final Path clientFilesPath = Paths.get(clientFilesDir);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshLocalFileList(lv);
        getCloudFilesList(serv);
        try {
            if (!Files.exists(clientFilesPath)) {
                Files.createDirectory(clientFilesPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            socketClose();
        }
    }

    public void delCommand(ActionEvent actionEvent) {
        String del = serv.getSelectionModel().getSelectedItem();
        if (del == null) {
            del = lv.getSelectionModel().getSelectedItem();
            if (del == null) {
                alertMsg();
                return;
            }
            try {
                Files.deleteIfExists(Paths.get(clientFilesDir + del));
                refreshLocalFileList(lv);
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
        String upload = lv.getSelectionModel().getSelectedItem();
        if (upload == null) {
            alertMsg();
            return;
        }
        Thread t = new Thread(() -> {

            if (Files.exists(Paths.get(clientFilesDir + upload))) {
                File file = new File(clientFilesDir + upload);
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
                    if (fsm.partNumber == 1) {
                        if (Files.deleteIfExists(Paths.get(clientFilesDir + fsm.fileName))) {
                            System.out.println("Удалили существующий файл");
                        }

                        Files.write(Paths.get(clientFilesDir + fsm.fileName), fsm.partContent, StandardOpenOption.CREATE);
                        System.out.println("Downloaded " + fsm.partNumber + "/" + fsm.partsCount);
                    }
                    if (fsm.partNumber > 1) {
                        System.out.println("Downloaded " + fsm.partNumber + "/" + fsm.partsCount);
                        Files.write(Paths.get(clientFilesDir + fsm.fileName), fsm.partContent, StandardOpenOption.APPEND);
                    }
                    if (fsm.partNumber == fsm.partsCount) {
                        System.out.println("File " + fsm.fileName + " downloaded!");
                        System.out.println(new File(clientFilesDir + fsm.fileName).length());

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
        refreshLocalFileList(lv);
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

    private void refreshLocalFileList(ListView<String> lv) {
        lv.getItems().clear();
        File dir = new File(clientFilesDir);
        String[] fileList = dir.list();
        if (fileList != null) {
            for (String file : fileList) {
                lv.getItems().add(file);
            }
        }
    }
}

