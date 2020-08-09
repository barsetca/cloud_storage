import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class FileSendMessage extends AbstractMessage {

    private String fileName;
    private byte[] content;

    public FileSendMessage(Path path) throws IOException {
        fileName = path.getFileName().toString();
        content = Files.readAllBytes(path);
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getContent() {
        return content;
    }
}
