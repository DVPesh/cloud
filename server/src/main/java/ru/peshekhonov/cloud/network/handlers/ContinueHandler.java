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
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.StandardOpenOption;

@Slf4j
public class ContinueHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof SubsequentData data) {
            String filename = data.getFilename();
            log.info("Continue frame of the file \"{}\" is received", filename);
            try {
                Files.write(Server.serverDir.resolve(filename), data.getData(), StandardOpenOption.APPEND);
                if (data.isEndOfFile()) {
                    ctx.writeAndFlush(new StatusData(filename, StatusType.OK));
                    log.info("The file \"{}\" is saved successfully", filename);
                }
            } catch (InvalidPathException e) {
                String str = "the path string cannot be converted to a Path";
                ctx.writeAndFlush(new StatusData(filename, StatusType.ERROR, str));
                log.error("File \"{}\": {}", filename, str);
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
