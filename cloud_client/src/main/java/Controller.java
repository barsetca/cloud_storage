import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public ListView<String> lv;
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
            in = new ObjectDecoderInputStream(socket.getInputStream(), 1024*1024*100);

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
    public void sendCommand(ActionEvent actionEvent) throws IOException {
        String command = txt.getText();
        String [] op = command.split(" ");
        if (op[0].equals("./download")) {

            FileRequestMessage frm = new FileRequestMessage(op[1]);
            System.out.println(frm.getFileName());
            sendMsg(frm);
            AbstractMessage am = readMsg();
            if (am instanceof FileSendMessage){
                FileSendMessage fsm = (FileSendMessage) am;
                Files.write(Paths.get("clientFiles/" + fsm.getFileName()), fsm.getContent(), StandardOpenOption.CREATE);
            }

            lv.getItems().add(op[1]);

        } else if (op[0].equals("./upload")) {
            Path path = Paths.get(clientFilesPath, op[1]);
            System.out.println(path);
            FileSendMessage fsm = new FileSendMessage(path);
            sendMsg(fsm);


        }
        else {
            System.out.println("Unknown command");
        }
    }

    public static boolean sendMsg(AbstractMessage msg){
        try {
            out.writeObject(msg);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            socketClose();
        }
        return false;
    }

    public static AbstractMessage readMsg(){
        Object o = null;
        try {
            o = in.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return (AbstractMessage) o;
    }

    public static void socketClose(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
