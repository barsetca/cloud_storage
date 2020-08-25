import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import messages.AbstractMessage;
import messages.AuthRegMsg;
import messages.ServerOkMsg;

import java.io.IOException;

public class AuthRegController extends AbstractController {

    public TextField login;
    public PasswordField password;
    public Button auth;
    public Button reg;

    public void regCommand(ActionEvent actionEvent) {
        AuthRegMsg arm = checkData();
        if (arm != null) {
            openNewMainScene(arm);
        }
    }

    public void authCommand(ActionEvent actionEvent) {
        AuthRegMsg arm = checkData();
        if (arm != null) {
            arm.setNew(false);
            openNewMainScene(arm);
        }
    }

    public AuthRegMsg checkData() {
        String userLogin = login.getText().trim();
        String userPass = password.getText().trim();
        if (userLogin.isEmpty() || userPass.isEmpty()) {
            alertMsg("Fields login and password must be filled");
            return null;
        }
        return new AuthRegMsg(userLogin, userPass, true);
    }

    public void openNewMainScene(AuthRegMsg arm) {

        sendMsg(arm);
        AbstractMessage am = readMsg();
        if (am instanceof ServerOkMsg) {
            ServerOkMsg som = (ServerOkMsg) am;
            if (som.isOk()) {
                try {
                    System.out.println(som.getMsg());
                    auth.getScene().getWindow().hide();
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("main.fxml"));
                    try {
                        loader.load();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Parent root = loader.getRoot();
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.setTitle(arm.getLogin());
                    stage.showAndWait();
                }catch (Exception e) {
                    System.out.println("Exit");
                }
            } else {
                alertMsg(som.getMsg());
            }
        }
    }
}
