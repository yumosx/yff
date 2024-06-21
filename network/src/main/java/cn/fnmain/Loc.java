package cn.fnmain;

import cn.fnmain.config.IpType;
import cn.fnmain.execption.ExceptionType;
import cn.fnmain.execption.FrameworkException;

public record Loc(IpType ipType, String ip, int port) {
    private static final int PORT_MAX = 65535;
    public short shortPort() {
        if (port < 0 || port > PORT_MAX) {
            throw new FrameworkException(ExceptionType.NETWORK, "port number overflow");
        }

        return (short) port;
    }

    @Override
    public String toString() {
        return STR."[\{ip==null || ip.isBlank() ? "localhost" : ip}:\{port}]";
    }
}
