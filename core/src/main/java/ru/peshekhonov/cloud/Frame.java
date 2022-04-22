package ru.peshekhonov.cloud;

import ru.peshekhonov.cloud.frames.FileListData;
import ru.peshekhonov.cloud.frames.StartData;
import ru.peshekhonov.cloud.frames.StatusData;
import ru.peshekhonov.cloud.frames.SubsequentData;

import java.io.File;
import java.io.Serializable;

public class Frame implements Serializable {

    private Object data;
    private FrameType type;

    public Object getData() {
        return data;
    }

    public FrameType getType() {
        return type;
    }

    public static Frame createStartFrame(String filename, long length, byte[] data) {
        Frame frame = new Frame();
        frame.data = new StartData(filename, length, data);
        frame.type = FrameType.START;
        return frame;
    }

    public static Frame createContinueFrame(byte[] data) {
        Frame frame = new Frame();
        frame.data = new SubsequentData(data);
        frame.type = FrameType.CONTINUE;
        return frame;
    }

    public static Frame createStatusFrame(String filename, StatusType status, String message) {
        Frame frame = new Frame();
        frame.data = new StatusData(filename, status, message);
        frame.type = FrameType.STATUS;
        return frame;
    }

    public static Frame createStatusFrame(String filename, StatusType status) {
        Frame frame = new Frame();
        frame.data = new StatusData(filename, status);
        frame.type = FrameType.STATUS;
        return frame;
    }

    public static Frame createFileListFrame(File file) {
        Frame frame = new Frame();
        frame.data = new FileListData(file);
        frame.type = FrameType.FILE_LIST;
        return frame;
    }

    public static Frame createFileListRequestFrame() {
        Frame frame = new Frame();
        frame.type = FrameType.FILE_LIST_REQUEST;
        return frame;
    }
}
