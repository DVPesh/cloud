package ru.peshekhonov.cloud;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Setter
public class FileInfo implements Serializable {

    public enum FileType {
        FILE, DIRECTORY
    }

    private String filename;
    private FileType type;
    private long size;
    private LocalDateTime lastModified;
    private long loadFactor = -1;

    public FileInfo(Path path) throws IOException {
        if (path.getFileName() != null) {
            this.filename = path.getFileName().toString();
        } else {
            this.filename = path.getRoot().toString();
        }
        this.type = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;
        if (this.type == FileType.DIRECTORY) {
            this.size = -1L;
        } else {
            this.size = Files.size(path);
        }
        this.lastModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneId.systemDefault());
    }
}
