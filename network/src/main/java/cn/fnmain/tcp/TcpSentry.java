package cn.fnmain.tcp;

import cn.fnmain.Constants;
import cn.fnmain.execption.ExceptionType;
import cn.fnmain.execption.FrameworkException;
import cn.fnmain.lib.OsNetworkLibrary;
import cn.fnmain.netapi.Channel;

import java.lang.foreign.MemorySegment;

public record TcpSentry(Channel channel) implements Sentry{
    private static final OsNetworkLibrary os = OsNetworkLibrary.CURRENT;

    @Override
    public int onReadableEvent(MemorySegment reversed, int offset) {
       throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
    }

    @Override
    public int onWritableEvent() {
        int errOpt = os.getErrOpt(channel.socket());
        if (errOpt == 0) {
            return Constants.NET_UPDATE;
        } else {
            throw new FrameworkException(ExceptionType.NETWORK, STR."Failed to establish connection, err opt : \{errOpt}");
        }
    }

    @Override
    public Protocol toProtocol() {
        return new TcpProtocol(channel);
    }

    @Override
    public void doClose() {
        int r = os.closeSocket(channel.socket());
        if (r < 0) {
            throw new FrameworkException(ExceptionType.NETWORK, STR."Failed to close socket, errno : \{Math.abs(r)}");
        }
    }
}
