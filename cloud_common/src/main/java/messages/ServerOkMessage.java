package messages;

public class ServerOkMessage extends AbstractMessage {

    private String msg;
    private boolean isOk;

    public ServerOkMessage(String msg, boolean isOk) {
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
