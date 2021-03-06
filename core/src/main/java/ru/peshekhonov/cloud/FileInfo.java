package ru.peshekhonov.cloud;

import lombok.Getter;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Map;

@Getter
public class FileInfo implements Serializable {

    public enum FileType {
        FILE, DIRECTORY
    }

    private final String filename;
    private final FileType type;
    private final long size;
    private final LocalDateTime lastModified;
    private Double loadFactor = -1.0;

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

    public FileInfo(Path path, Map<Path, Metadata> map) throws IOException {
        this(path);
        setLoadFactor(path, map);
    }

    public FileInfo(Path base, Path fullPath, Map<Path, Metadata> map) throws IOException {
        this(fullPath);
        setLoadFactor(base.relativize(fullPath), map);
    }

    public FileInfo() {
        this.filename = "";
        this.type = FileType.DIRECTORY;
        this.size = -1L;
        this.lastModified = LocalDateTime.now();
    }

    private void setLoadFactor(Path path, Map<Path, Metadata> map) {
        if (map == null || type == FileType.DIRECTORY) {
            return;
        }
        Metadata data = map.get(path);
        if (data != null && data.size > 0) {
            loadFactor = (double) size / data.size;
        }
    }
}
