package cn.fnmain.netapi;

public interface Handler {
    void onConnected(Channel channel);
    void onRecv(Channel channel);
    void onShutdown(Channel channel);
    void onRemoved(Channel channel);
}
