package messages;

import fileInfo.FileInfo;

import java.util.List;

public class CloudInfoListSendMsg extends AbstractMessage {

    public List<FileInfo> listFileInfo;

    public CloudInfoListSendMsg(List<FileInfo> listFileInfo) {
        this.listFileInfo = listFileInfo;
    }

    public List<FileInfo> getListFileInfo() {
        return listFileInfo;
    }

   }
