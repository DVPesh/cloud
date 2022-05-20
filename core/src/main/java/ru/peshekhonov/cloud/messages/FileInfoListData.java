package ru.peshekhonov.cloud.messages;

import lombok.Getter;
import ru.peshekhonov.cloud.FileInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FileInfoListData extends Message {

    private final @Getter
    List<FileInfo> fileInfoList;
    private final String directory;

    public FileInfoListData(Path path) throws IOException {
        try(Stream<Path> pathStream = Files.list(path)) {
            List<Path> pathList = pathStream.toList();
            fileInfoList = new ArrayList<>(pathList.size()); //нельзя использовать Stream API из-за IOException!
            for (Path element : pathList) {
                fileInfoList.add(new FileInfo(element));
            }
            this.directory = path.normalize().toString();
        }
    }

    public Path getDirectory() {
        return Path.of(directory);
    }
}
