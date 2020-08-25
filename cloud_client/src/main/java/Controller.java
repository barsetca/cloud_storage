import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public ListView<String> lv;
    public ListView<String> serv;
    public TextField txt;
    public Button send;
    private static Socket socket;
    private static ObjectEncoderOutputStream out;
    private static ObjectDecoderInputStream in;
    private final String clientFilesPath = "clientFiles";


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socket = new Socket("localhost", 8189);
            out = new ObjectEncoderOutputStream(socket.getOutputStream());
            in = new ObjectDecoderInputStream(socket.getInputStream(), 1024 * 1024 * 100);


        } catch (IOException e) {
            e.printStackTrace();
            socketClose();
        }

//        File dir = new File(clientFilesPath);
//        for (String file : dir.list()) {
//            lv.getItems().add(file);
//        }
        refreshLocalFileList(lv);
        getCloudFilesList(serv);
    }

    // ./download fileName
    // ./upload fileName
    public void sendCommand(ActionEvent actionEvent) {
        String command = txt.getText();
        String[] op = command.split(" ");
        if (op[0].equals("./download")) {

            FileRequestMessage frm = new FileRequestMessage(op[1]);
            System.out.println(frm.getFileName());
            sendMsg(frm);

            new Thread(() -> {

                try {
                    int count = 0;
                    while (true) {
                        Object download = in.readObject();
                        FileSendMessage fsm = (FileSendMessage) download;
                        count++;
                        if (fsm.partNumber == 1) {
                            if (Files.deleteIfExists(Paths.get("clientFiles/" + fsm.fileName))){
                                System.out.println("Удалили существующий файл");
                            }

                            Files.write(Paths.get("clientFiles/" + fsm.fileName), fsm.partContent, StandardOpenOption.CREATE);
                            System.out.println("Downloaded " + fsm.partNumber + "/" + fsm.partsCount);
                        }
                        if (fsm.partNumber > 1) {
                            System.out.println("Downloaded " + fsm.partNumber + "/" + fsm.partsCount);
                            Files.write(Paths.get("clientFiles/" + fsm.fileName), fsm.partContent, StandardOpenOption.APPEND);
                        }
                        if (fsm.partNumber == fsm.partsCount) {
                            System.out.println("File " + fsm.fileName + " downloaded!");
                            System.out.println(new File("clientFiles/" + fsm.fileName).length());

                            System.out.println(count);
                            break;
                        }

                    }
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                    socketClose();
                }

            }).start();
            lv.getItems().remove(op[1]);
            lv.getItems().add(op[1]);

        } else if (op[0].equals("./upload")) {
            Thread t = new Thread(() -> {

                if (Files.exists(Paths.get("clientFiles/" + op[1]))) {
                    File file = new File("clientFiles/" + op[1]);
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

        } else {
            System.out.println("Unknown command");
        }
    }

    public static boolean sendMsg(AbstractMessage msg) {
        try {
            out.writeObject(msg);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            socketClose();
        }
        return false;
    }

    public static void getCloudFilesList(ListView<String> serv) {
        serv.getItems().clear();
        RequestFilesList rfl = new RequestFilesList();
      sendMsg(rfl);
        try {
            Object list = in.readObject();
            if (list instanceof CloudFilesList){
                CloudFilesList cfl = (CloudFilesList) list;
                List<String> cloudFilesList = cfl.getCloudFilesList();
                cloudFilesList.forEach(s -> serv.getItems().add(s));
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

    }

    public static void refreshLocalFileList(ListView<String> lv) {
        lv.getItems().clear();
        File dir = new File("clientFiles/");
        String[] fileList = dir.list();
        if (fileList != null) {
            for (String file : dir.list()) {
                lv.getItems().add(file);
            }
        }
    }


    public static void socketClose() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void delCommand(ActionEvent actionEvent)  {
        String del = serv.getSelectionModel().getSelectedItem();
        if (del == null){
            del = lv.getSelectionModel().getSelectedItem();
            try {
                Files.deleteIfExists(Paths.get("clientFiles/" + del));
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
    }

    public void downloadCommand(ActionEvent actionEvent) {
    }
}
