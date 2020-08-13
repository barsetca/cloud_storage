import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

public class Controller implements Initializable {

    public ListView<String> lv;
    public TextField txt;
    public Button send;
    private static Socket socket;
    private static ObjectEncoderOutputStream out;
    private static ObjectDecoderInputStream in;
    private final String clientFilesPath = "clientFiles";
    //private static CountDownLatch cdl;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socket = new Socket("localhost", 8189);
            out = new ObjectEncoderOutputStream(socket.getOutputStream());
            in = new ObjectDecoderInputStream(socket.getInputStream(), 1024 * 1024 * 100);
           // cdl = new CountDownLatch(1);

        } catch (IOException e) {
            e.printStackTrace();
            socketClose();
        }
        File dir = new File(clientFilesPath);
        for (String file : dir.list()) {
            lv.getItems().add(file);
        }
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
            lv.getItems().add(op[1]);

        } else if (op[0].equals("./upload")) {
//            Path path = Paths.get(clientFilesPath, op[1]);
//            System.out.println(path);
//            FileSendMessage fsm = new FileSendMessage(path);
//            sendMsg(fsm);

        } else if (op[0].equals("./delete")) {

            FileDeleteMessage fdm = new FileDeleteMessage(op[1]);
            System.out.println(fdm.getFileName());
            sendMsg(fdm);

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

    public static AbstractMessage readMsg() {
        Object o = null;
        try {
            o = in.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return (AbstractMessage) o;
    }

    public static void socketClose() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
