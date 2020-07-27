import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public ListView<String> lv;
    public TextField txt;
    public Button send;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private final String clientFilesPath = "./cloud_common/src/main/resources/clientFiles";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socket = new Socket("localhost", 8189);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
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
        String [] op = command.split(" ");
        if (op[0].equals("./download")) {
            try {
                dos.writeUTF(op[0]);
                dos.writeUTF(op[1]);
                String response = dis.readUTF();
                System.out.println("resp: " + response);
                if (response.equals("OK")) {
                    File file = new File(clientFilesPath + "/" + op[1]);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    long len = dis.readLong();
                    byte [] buffer = new byte[1024];
                    try(FileOutputStream fos = new FileOutputStream(file)) {
                        if (len < 1024) {
                            int count = dis.read(buffer);
                            fos.write(buffer, 0, count);
                        } else {
                            for (long i = 0; i <= len / 1024; i++) {
                                int count = dis.read(buffer);
                                fos.write(buffer, 0, count);
                            }
                        }
                    }
                    lv.getItems().add(op[1]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (op[0].equals("./upload")) {
            // TODO: 7/23/2020 upload

            File file = new File(clientFilesPath + "/" + op[1]);
            if (!file.exists()){
                System.out.println("File " + file.getAbsolutePath() + " is not exists");

            }

            try( InputStream is = new FileInputStream(file)) {
//                dos.writeUTF(op[0]);
//                dos.writeUTF(op[1]);
                dos.writeByte(25);
                byte[] fileName = op[1].getBytes();
                dos.writeInt(fileName.length);
                System.out.println(fileName.length);
                dos.writeBytes(op[1]);
                long len = file.length();
                System.out.println(file.length());
                dos.writeLong(len);

                byte[] buffer = new byte[8192];
                System.out.print("/...");
                while (is.available() > 0) {
                    int readBytes = is.read(buffer);
                    dos.write(buffer, 0, readBytes);
                }
                System.out.println("/");

                //String response = dis.readUTF();
//                if (response.equals("OK")) {
//                    System.out.println("File uploaded, resp: " + response);
//                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
