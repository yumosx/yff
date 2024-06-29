package cn.fnmain.node;

import cn.fnmain.Constants;
import cn.fnmain.Mutex;
import cn.fnmain.State;
import cn.fnmain.WriteBuffer;
import cn.fnmain.execption.ExceptionType;
import cn.fnmain.execption.FrameworkException;
import cn.fnmain.lib.IntMap;
import cn.fnmain.netapi.Channel;
import cn.fnmain.tcp.Protocol;
import cn.fnmain.thread.WriterTask;

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

    private int expectedState(int t) {
        return switch (t) {
            case Constants.NET_PW -> Constants.NET_W;
            case Constants.NET_PR -> Constants.NET_R;
            default -> throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        };
    }

    private boolean ctlWithStateChecked(int expected) {
        try (Mutex _ = channelState.withMutex()) {
            int state = channelState.get();

            if (state & Constants.NET_PC == Constants.NET_PC) {
                return true;
            }

            int current = state & Constants.NET_RW;
            int to = current | expected;

            if (to != expected) {

            }
            return false;
        }
    }

    private void ctl(int expected) {
        if (ctlWithStateChecked(expected)) {
            close();
        }
    }

    public void handleEvent(int id) {
        if (id == Constants.NET_PW || id == Constants.NET_PR) {
            ctl(id);
        }
    }


    public void onWritableEvent() {
        int r;
        r = protocol.onWritableEvent();
        if (r < 0) {
            handleEvent(r);
        }
    }

    private void sendMsg(WriteBuffer writeBuffer, WriterCallback writerCallback) {
        MemorySegment data = writeBuffer.toSegment();
    }


    @Override
    public void onMsg(MemorySegment memorySegment, WriterTask writerTask) {
        if (writerTask.channel() == channel) {
            Object msg = writerTask.msg();
            WriterCallback writerCallback = writerTask.writerCallback();

            try (final WriteBuffer writeBuffer = WriteBuffer.newReservedWriteBuffer(memorySegment)) {
                try {
                    channel.encoder().encode(writeBuffer, msg);
                } catch (RuntimeException e) {
                    System.out.println("err occurred in encoder");
                }

                if (writeBuffer.writeIndex() > 0) {
                    sendMsg(writeBuffer, writerCallback);
                } else if (writerCallback != null) {
                    writerCallback.invokeOnsuccess(channel);
                }
            }
        }
    }


    @Override
    public void onMultipleMsg(MemorySegment memorySegment, WriterTask writerTask) {

    }

    @Override
    public void onShutdown(WriterTask writerTask) {

    }

    @Override
    public void onWriteableEvent() {

    }

    private void close() {

    }
}
