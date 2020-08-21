package messages;

public class FileDeleteMessage extends AbstractMessage {

    private String fileName;

    public FileDeleteMessage(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
