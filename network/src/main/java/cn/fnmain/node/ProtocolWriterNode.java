package cn.fnmain.node;

import cn.fnmain.Constants;
import cn.fnmain.Mutex;
import cn.fnmain.State;
import cn.fnmain.WriteBuffer;
import cn.fnmain.execption.ExceptionType;
import cn.fnmain.execption.FrameworkException;
import cn.fnmain.lib.IntMap;
import cn.fnmain.lib.OsNetworkLibrary;
import cn.fnmain.netapi.Channel;
import cn.fnmain.tcp.Protocol;
import cn.fnmain.thread.WriterTask;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;

public class ProtocolWriterNode implements WriterNode {
    private final IntMap<PollerNode> nodeMap;
    private final Channel channel;
    private final Protocol protocol;
    private final State channelState;

    private Duration timeout;
    private Deque<Task> taskDeque;
    private static final OsNetworkLibrary os = OsNetworkLibrary.CURRENT;

    private record Task(Arena arena, MemorySegment memorySegment, WriterCallback writerCallback){}

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
            case Constants.NET_PRW -> Constants.NET_RW;
            default -> throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        };
    }

    private boolean ctlWithStateChecked(int expected) {
        try (Mutex _ = channelState.withMutex()) {
            int state = channelState.get();

            if ((state & Constants.NET_PC) == Constants.NET_PC) {
                return true;
            }

            int current = state & Constants.NET_RW;
            int to = current | expected;

            if (to != current) {
                os.ctlMux(channel.poller().mux(), channel.socket(), current, to);
                channelState.set(state + (to - current));
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

    private void copyLocally(MemorySegment segment, WriterCallback writerCallback) {
        Arena arena = Arena.ofConfined();
        long size = segment.byteSize();
        MemorySegment memorySegment = arena.allocateArray(ValueLayout.JAVA_BYTE, size);
        MemorySegment.copy(segment, 0, memorySegment, 0, size);
        taskDeque.addLast(new Task(arena, memorySegment, writerCallback));
    }

    private void sendMsg(WriteBuffer writeBuffer, WriterCallback writerCallback) {
        MemorySegment data = writeBuffer.toSegment();
        if (taskDeque == null) {
            int len = (int) data.byteSize();
            int r;
            while (true) {
                try {
                    r = protocol.doWrite(data, len);
                    if (r > 0 && r < len) {
                        len = len - r;
                        data = data.asSlice(r, len);
                    } else {
                        break;
                    }
                } catch (RuntimeException e) {
                    System.out.println("failed to perform dowrite()");
                    close();
                    return;
                }
            }

            if (r == len) {
                if (writerCallback != null) {
                    writerCallback.invokeOnsuccess(channel);
                }
            } else {
                taskDeque = new ArrayDeque<>();
                copyLocally(data, writerCallback);
                if (r < 0) {
                    handleEvent(r);
                }
            }
        } else {
            copyLocally(data, writerCallback);
        }
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

    @Override
    public void close() {

    }
}
