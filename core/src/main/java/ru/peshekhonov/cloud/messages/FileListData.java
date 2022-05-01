package ru.peshekhonov.cloud.messages;

import lombok.Getter;
import ru.peshekhonov.cloud.MessageType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Getter
public class FileListData extends Message {

    private final List<String> fileList;

    public FileListData(Path path) throws IOException {
        this.fileList = Files.list(path)
                .map(Path::getFileName)
                .map(Path::toString)
                .toList();
    }

//    @Override
//    public MessageType getMessageType() {
//        return MessageType.FILE_LIST;
//    }
}
