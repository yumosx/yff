package cn.fnmain.config;

public class SocketConfig {
    private boolean reuseAddr = true;
    private boolean keepAlive = false;
    private boolean tcpNoDelay = true;
    private boolean ipv6Only = false;

    public boolean isReuseAddr() {
        return true;
    }

    public boolean isKeepAlive() {
        return true;
    }

    public boolean isTcpNoDelay() {
        return true;
    }

    public boolean isIpv6Only() {
        return true;
    }
}
