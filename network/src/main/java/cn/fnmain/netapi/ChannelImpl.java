package cn.fnmain.netapi;

import cn.fnmain.Loc;
import cn.fnmain.Socket;
import cn.fnmain.thread.Poller;
import cn.fnmain.thread.Writer;

public record ChannelImpl() implements Channel {

    @Override
    public Socket socket() {
        return null;
    }

    @Override
    public Encoder encoder() {
        return null;
    }

    @Override
    public Decoder decoder() {
        return null;
    }

    @Override
    public Handler handler() {
        return null;
    }

    @Override
    public Poller poller() {
        return null;
    }

    @Override
    public Writer writer() {
        return null;
    }

    @Override
    public Loc loc() {
        return null;
    }

    @Override
    public void sendMsg(String msg) {

    }
}
