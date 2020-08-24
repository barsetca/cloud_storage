package messages;

import fileInfo.FileInfo;

import java.util.List;

public class CloudInfoList extends AbstractMessage {

    public List<FileInfo> listFileInfo;

    public CloudInfoList(List<FileInfo> listFileInfo) {
        this.listFileInfo = listFileInfo;
    }

    public List<FileInfo> getListFileInfo() {
        return listFileInfo;
    }

   }
