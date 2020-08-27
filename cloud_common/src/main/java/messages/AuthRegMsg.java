package messages;

public class AuthRegMsg extends AbstractMessage {

    private String login;
    private String password;
    private boolean isNew;

    public AuthRegMsg(String login, String password, boolean isNew) {
        this.login = login;
        this.password = password;
        this.isNew = isNew;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }
}
