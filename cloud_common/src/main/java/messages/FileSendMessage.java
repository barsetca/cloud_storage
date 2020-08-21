package messages;

public class FileSendMessage extends AbstractMessage {

    public String fileName;
    public int partNumber;
    public int partsCount;
    public byte[] partContent;

    public FileSendMessage(String fileName, int partNumber, int partsCount, byte[] partContent) {
        this.fileName = fileName;
        this.partNumber = partNumber;
        this.partsCount = partsCount;
        this.partContent = partContent;
    }
}
