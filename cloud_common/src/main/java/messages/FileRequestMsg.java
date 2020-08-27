package messages;

public class FileRequestMsg extends AbstractMessage {

    private String fileName;

    public FileRequestMsg(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
