package cn.fnmain.node;

import cn.fnmain.Constants;
import cn.fnmain.State;
import cn.fnmain.lib.IntMap;
import cn.fnmain.netapi.Channel;
import cn.fnmain.tcp.Protocol;

import java.lang.foreign.MemorySegment;

public class ProtocolWriterNode implements WriterNode {
    private final IntMap<PollerNode> nodeMap;
    private final Channel channel;
    private final Protocol protocol;
    private final State channelState;


    public ProtocolWriterNode(IntMap<PollerNode> nodeMap, Channel channel, Protocol protocol, State channelState) {
        this.nodeMap = nodeMap;
        this.channel = channel;
        this.protocol = protocol;
        this.channelState = channelState;
    }

    public void ctl(int id) {

    }

    public void handleEvent(int id) {
        if (id == Constants.NET_W) {
            ctl(id);
        }
    }

    public void handleReceived(MemorySegment ptr, int len, int received) {

    }

    public void onReadableEvent(MemorySegment ptr, int len) {
        int r;
        r = protocol.onReadableEvent(ptr, len);

        //此时可能存在两种情况
        //1. 可以进行读取
        //2. 不可以进行读取, 但是可能会有写事件
        if (r > 0) {
            handleReceived(ptr, len, r);
        } else {
            handleEvent(r);
        }
    }

    public void onWritableEvent() {
        int r;
        r = protocol.onWritableEvent();
        if (r < 0) {
            handleEvent(r);
        }
    }

    private void close() {

    }
}
