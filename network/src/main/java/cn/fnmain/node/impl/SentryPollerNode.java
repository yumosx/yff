package cn.fnmain.node.impl;

import cn.fnmain.execption.ExceptionType;
import cn.fnmain.execption.FrameworkException;
import cn.fnmain.lib.Constants;
import cn.fnmain.State;
import cn.fnmain.lib.IntMap;
import cn.fnmain.lib.OsNetworkLibrary;
import cn.fnmain.netapi.Channel;
import cn.fnmain.node.PollerNode;
import cn.fnmain.protocol.Protocol;
import cn.fnmain.protocol.Sentry;
import cn.fnmain.thread.PollerTask;
import cn.fnmain.thread.PollerTaskType;
import cn.fnmain.thread.WriterTask;
import cn.fnmain.thread.WriterTaskType;

import java.lang.foreign.MemorySegment;
import java.time.Duration;

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
        try {
            channel.handler().onConnected(channel);
        } catch (RuntimeException e) {
            System.out.println("err occurred in onConnected");
            close();
        }
        ctl(Constants.NET_R);
        Protocol protocol = sentry.toProtocol();
        ProtocolPollerNode protocolPollerNode = new ProtocolPollerNode(map, protocol, channel, channelState);
        map.replace(channel.socket().intValue(), this, protocolPollerNode);
        channel.writer().submit(new WriterTask(WriterTaskType.INITIATE, channel, new ProtoAndState(protocol, channelState), null));
    }


    public void handleEvent(int id) {
        if (id == Constants.NET_R || id == Constants.NET_W || id == Constants.NET_RW) {
            ctl(id);
        } else if (id == Constants.NET_UPDATE) {
            updateProtocol();
        }

        if (id != Constants.NET_IGNORED) {
            throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
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
    public void onRegisterTaggedMsg(PollerTask pollerTask) {
        throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
    }


    @Override
    public void onUnRegisterTaggedMsg(PollerTask pollerTask) {
        throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
    }

    public void close() {
        if (map.remove(channel.socket().intValue(), this)) {
            closeSentry();
            if (map.isEmpty()) {
                channel.poller().submit(new PollerTask(PollerTaskType.POTENTIAL_EXIT, null, null));
            }
        }
    }

    @Override
    public void onClose(PollerTask pollerTask) {
        if (pollerTask.channel() == channel) {
            close();
        }
    }

    @Override
    public void exit(Duration duration) {
        close();
    }

    public void closeSentry() {
        try {
            sentry.doClose();
        } catch (RuntimeException e) {
            System.out.println("failed to close sentry");
        }

        if (callback != null) {
            Thread.ofVirtual().start(callback);
        }
    }
}