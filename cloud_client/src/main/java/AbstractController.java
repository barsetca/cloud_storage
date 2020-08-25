import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import messages.AbstractMessage;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public abstract class AbstractController implements Initializable {

    public static Socket socket;
    public static ObjectEncoderOutputStream out;
    public static ObjectDecoderInputStream in;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socket = new Socket("localhost", 8189);
            out = new ObjectEncoderOutputStream(socket.getOutputStream());
            in = new ObjectDecoderInputStream(socket.getInputStream(), 1024 * 1024 * 100);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("initialize abstract");
            socketClose();
        }
    }

    public static AbstractMessage readMsg() {
        Object o = null;
        try {
            o = in.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            System.out.println("readMsg");
            socketClose();
        }
        return (AbstractMessage) o;
    }

    public static boolean sendMsg(AbstractMessage msg) {
        try {
            out.writeObject(msg);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("sendMsg");
            socketClose();
        }
        return false;
    }

    public static void alertMsg(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Result: ");
        alert.setContentText(msg);
        alert.show();
    }


    public static void socketClose() {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("socketClose");
            e.printStackTrace();
        }
    }


}
