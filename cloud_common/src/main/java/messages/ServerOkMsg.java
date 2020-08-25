package messages;

public class ServerOkMsg extends AbstractMessage {

    private String msg;
    private boolean isOk;

    public ServerOkMsg(String msg, boolean isOk) {
        this.msg = msg;
        this.isOk = isOk;
    }

    public String getMsg() {
        return msg;
    }

    public boolean isOk() {
        return isOk;
    }
}
