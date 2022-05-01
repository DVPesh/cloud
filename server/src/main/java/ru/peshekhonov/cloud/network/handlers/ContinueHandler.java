package ru.peshekhonov.cloud.network.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.StatusType;
import ru.peshekhonov.cloud.messages.Message;
import ru.peshekhonov.cloud.messages.StatusData;
import ru.peshekhonov.cloud.messages.SubsequentData;
import ru.peshekhonov.cloud.network.Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;

@Slf4j
public class ContinueHandler extends SimpleChannelInboundHandler<Message> {

    private final ByteBuffer buffer = ByteBuffer.allocate(Server.BUFFER_SIZE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof SubsequentData data) {
            String filename = data.getFilename();
            log.info("Continue frame of the file \"{}\" is received", filename);
            try {
                SeekableByteChannel writeChannel = ctx.pipeline().get(StartHandler.class).getWriteChannel();
                buffer.put(data.getData());
                buffer.flip();
                writeChannel.write(buffer);
                buffer.clear();
                if (data.isEndOfFile()) {
                    writeChannel.close();
                    ctx.writeAndFlush(new StatusData(filename, StatusType.OK));
                    log.info("The file \"{}\" is saved successfully", filename);
                }
            } catch (IOException e) {
                String str = "I/O error";
                ctx.writeAndFlush(new StatusData(filename, StatusType.ERROR, str));
                log.error("File \"{}\": {}", filename, str);
                try {
                    Files.deleteIfExists(Server.serverDir.resolve(filename));
                } catch (IOException ex) {
                    log.error("", ex);
                }
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
