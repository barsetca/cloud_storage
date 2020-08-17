import java.util.ArrayList;
import java.util.List;

public class CloudFilesList extends AbstractMessage{

    private List<String> cloudFilesList;

    public CloudFilesList(List<String> cloudFilesList) {
        this.cloudFilesList = cloudFilesList;
    }

    public List<String> getCloudFilesList() {
        return cloudFilesList;
    }
}
