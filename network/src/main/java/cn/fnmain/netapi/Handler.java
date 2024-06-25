package cn.fnmain.netapi;

public interface Handler {
    void onConnected(Channel channel);
    TaggedResult onRecv(Channel channel, Object msg);
    void onShutdown(Channel channel);
    void onRemoved(Channel channel);
}
