
public class FileSendMessage extends AbstractMessage {

    String fileName;
    int partNumber;
    int partsCount;
    byte[] partContent;

    public FileSendMessage(String fileName, int partNumber, int partsCount, byte[] partContent) {
        this.fileName = fileName;
        this.partNumber = partNumber;
        this.partsCount = partsCount;
        this.partContent = partContent;
    }
}
