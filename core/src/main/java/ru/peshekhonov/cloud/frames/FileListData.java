package ru.peshekhonov.cloud.frames;

import java.io.File;
import java.io.Serializable;

public class FileListData implements Serializable {

    private final String[] fileList;

    public FileListData(File file) {
        this.fileList = file.list();
    }

    public String[] getFileList() {
        return fileList;
    }
}
