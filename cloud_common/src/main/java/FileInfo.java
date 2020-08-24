import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileInfo implements Serializable {
    static final long serialVersionUID = 1L;

    public enum FileType implements Serializable {

        FILE("File"),
        DIRECTORY("Dir");

        private String name;

        FileType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private String fileName;
    private FileType fileType;
    private long size;

    public FileInfo(Path path) {

        try {
            this.fileName = path.getFileName().toString();
            this.size = Files.size(path);
            this.fileType = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;
            if (this.fileType == FileType.DIRECTORY) {
                this.size = -1L;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed path: " + path.toAbsolutePath().toString());
        }

    }

    public String getFileName() {
        return fileName;
    }

    public FileType getFileType() {
        return fileType;
    }

    public long getSize() {
        return size;
    }

   }
