package cn.fnmain.tcp;

import cn.fnmain.execption.ExceptionType;
import cn.fnmain.execption.FrameworkException;
import cn.fnmain.lib.OsNetworkLibrary;
import cn.fnmain.netapi.Channel;

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
        return 0;
    }

    @Override
    public int doWrite(MemorySegment data, int len) {
        return 0;
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
