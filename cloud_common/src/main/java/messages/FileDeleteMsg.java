package messages;

public class FileDeleteMsg extends AbstractMessage {

    private String fileName;

    public FileDeleteMsg(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
