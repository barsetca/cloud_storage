package messages;

public class FileSendMsg extends AbstractMessage {

    public String fileName;
    public int partNumber;
    public int partsCount;
    public byte[] partContent;

    public FileSendMsg(String fileName, int partNumber, int partsCount, byte[] partContent) {
        this.fileName = fileName;
        this.partNumber = partNumber;
        this.partsCount = partsCount;
        this.partContent = partContent;
    }
}
