package cn.fnmain.protocol.impl;

import cn.fnmain.lib.Constants;
import cn.fnmain.Socket;
import cn.fnmain.execption.ExceptionType;
import cn.fnmain.execption.FrameworkException;
import cn.fnmain.lib.OsNetworkLibrary;
import cn.fnmain.netapi.Channel;
import cn.fnmain.protocol.Protocol;
import cn.fnmain.thread.WriterTask;
import cn.fnmain.thread.WriterTaskType;

import java.lang.foreign.MemorySegment;

public record TcpProtocol(Channel channel) implements Protocol {
    private static final OsNetworkLibrary os = OsNetworkLibrary.CURRENT;

    @Override
    public int onReadableEvent(MemorySegment reserved, int len) {
        int r = os.recv(channel.socket(), reserved, len);

        if (r < 0) {
            throw new FrameworkException(ExceptionType.NETWORK, STR."Failed to perform recv(), errno : \{Math.abs(r)}");
        } else {
            return r;
        }
    }

    @Override
    public int onWritableEvent() {
        channel.writer().submit(new WriterTask(WriterTaskType.WRITABLE, channel, null, null));
        return 0;
    }

    @Override
    public int doWrite(MemorySegment data, int len) {
        Socket socket = channel.socket();
        int t = os.send(socket, data, len);

        if (t < 0) {
            int errno = Math.abs(t);
            if (errno == os.sendBlockCode()) {
                return Constants.NET_PW;
            } else {
                throw new FrameworkException(ExceptionType.NETWORK, STR."failed to perform send(), errno: \{errno}");
            }
        } else {
            return t;
        }
    }

    @Override
    public void doShutdown() {
        int r = os.shutdownWrite(channel.socket());
        if (r < 0) {
            throw new FrameworkException(ExceptionType.NETWORK, STR."Failed to perform shutdown(), errno : \{Math.abs(r)}");
        }
    }

    @Override
    public void doClose() {
        int r = os.closeSocket(channel.socket());
        if (r < 0) {
            throw new FrameworkException(ExceptionType.NETWORK, STR."Failed to close socket, error:\{Math.abs(r)}");
        }
    }
}
