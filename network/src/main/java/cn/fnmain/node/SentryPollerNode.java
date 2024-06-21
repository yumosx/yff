package cn.fnmain.node;

import cn.fnmain.Constants;
import cn.fnmain.State;
import cn.fnmain.lib.IntMap;
import cn.fnmain.netapi.Channel;
import cn.fnmain.tcp.Sentry;

import java.lang.foreign.MemorySegment;


public class SentryPollerNode implements PollerNode {
    private IntMap<PollerNode> map;
    private Channel channel;
    private Sentry sentry;
    private State state = new State(Constants.NET_W);

    public SentryPollerNode(IntMap<PollerNode> map, Channel channel, Sentry sentry) {
        this.map = map;
        this.channel = channel;
        this.sentry = sentry;
    }

    public void ctl(int id) {

    }

    public void updateProtocol() {

    }

    public void handleEvent(int id) {
        if (id == Constants.NET_R || id == Constants.NET_W) {
            ctl(id);
        } else if (id == Constants.NET_UPDATE) {
            updateProtocol();
        }
    }

    public void onReadableEvent(MemorySegment ptr, int len) {
        handleEvent(sentry.onReadableEvent(ptr, len));
    }

    public void onWritableEvent() {
        handleEvent(sentry.onWritableEvent());
    }

    public void close() {

    }


    @Override
    public void doExit() {
        close();
    }
}
