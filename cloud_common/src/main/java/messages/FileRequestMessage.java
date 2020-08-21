package messages;

public class FileRequestMessage extends AbstractMessage {

    private String fileName;

    public FileRequestMessage(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
