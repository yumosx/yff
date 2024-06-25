package cn.fnmain.node;

import cn.fnmain.Constants;
import cn.fnmain.State;
import cn.fnmain.lib.IntMap;
import cn.fnmain.lib.OsNetworkLibrary;
import cn.fnmain.netapi.Channel;
import cn.fnmain.tcp.Sentry;

import java.lang.foreign.MemorySegment;

public class SentryPollerNode implements PollerNode {
    private IntMap<PollerNode> map;
    private Channel channel;
    private Sentry sentry;
    private Runnable callback;
    private OsNetworkLibrary os = OsNetworkLibrary.CURRENT;
    private State channelState = new State(Constants.NET_W);

    public SentryPollerNode(IntMap<PollerNode> map, Channel channel, Sentry sentry, Runnable callback) {
        this.map = map;
        this.channel = channel;
        this.sentry = sentry;
        this.callback = callback;
    }

    public void ctl(int id) {
        int current = channelState.get();
        if (current != id) {
            os.ctlMux(channel.poller().mux(), channel.socket(), current, id);
            channelState.set(id);
        }
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

    @Override
    public void onWriteableEvent() {
        handleEvent(sentry.onWritableEvent());
    }

    @Override
    public void exit() {

    }

    public void close() {

    }

    @Override
    public void doExit() {
        close();
    }


    public void closeSentry() {

    }
}
