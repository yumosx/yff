package cn.fnmain.node.impl;

import cn.fnmain.*;
import cn.fnmain.execption.ExceptionType;
import cn.fnmain.execption.FrameworkException;
import cn.fnmain.lib.Constants;
import cn.fnmain.lib.IntMap;
import cn.fnmain.lib.OsNetworkLibrary;
import cn.fnmain.netapi.Carrier;
import cn.fnmain.netapi.Channel;
import cn.fnmain.netapi.TaggedMsg;
import cn.fnmain.netapi.TaggedResult;
import cn.fnmain.node.PollerNode;
import cn.fnmain.protocol.Protocol;
import cn.fnmain.thread.PollerTask;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ProtocolPollerNode implements PollerNode {
    private final Protocol protocol;
    private Carrier carrier;
    private final Channel channel;
    private State channelState;
    private IntMap<PollerNode> nodeMap;
    private WriteBuffer tempBuffer;
    private static final int MAX_SIZE = 16;
    private static final int MAX_LIST_SIZE = 64;
    private List<Object> entityList = new ArrayList<>(MAX_LIST_SIZE);

    private OsNetworkLibrary osNetworkLibrary = OsNetworkLibrary.CURRENT;

    public ProtocolPollerNode(IntMap<PollerNode> nodeMap, Protocol protocol, Channel channel, State channelState) {
        this.nodeMap = nodeMap;
        this.protocol = protocol;
        this.channel = channel;
        this.channelState = channelState;
    }

    private long process(MemorySegment memorySegment) {
        ReadBuffer readBuffer = new ReadBuffer(memorySegment);
        try {
            channel.decoder().decode(readBuffer, entityList);
        } catch (RuntimeException e) {
            System.out.println("Err occurred in decoder");
            close();
            return -1;
        }

        if (!entityList.isEmpty()) {
            for (Object entity : entityList) {
                TaggedResult taggedResult = null;
                try {
                    channel.handler().onRecv(channel, entity);
                } catch (RuntimeException e) {
                    System.out.println("Err occurred in decoder");
                    close();
                    return -1;
                }

                if (taggedResult != null) {
                    int tag = taggedResult.tag();
                }
            }

            if (entityList.size() > MAX_LIST_SIZE) {
                entityList = new ArrayList<>();
            } else {
                entityList.clear();
            }
        }

        return readBuffer.readIndex();
    }


    private void onReceive(MemorySegment memorySegment) {
        if (tempBuffer == null) { //备份数据缓冲区域
            long len = memorySegment.byteSize();
            long readIndex = process(memorySegment);

            if (readIndex >= 0 && readIndex < len) {
                tempBuffer = WriteBuffer.newDefaultWriteBuffer(Arena.ofConfined(), len);
                tempBuffer.writeSegment(readIndex == 0 ? memorySegment : memorySegment.asSlice(readIndex, len - readIndex));
            }
        } else {
            tempBuffer.writeSegment(memorySegment);
            long len = tempBuffer.writeIndex();
            long readIndex = process(tempBuffer.toSegment());
            if (readIndex == len) {
                tempBuffer.close();
                tempBuffer = null;
            } else if (readIndex > 0) {
                tempBuffer = tempBuffer.truncate(readIndex);
            }
        }
    }


    public void handleReceived(MemorySegment ptr, int len, int r) {
        if (r == len) {
            onReceive(ptr);
        } else if (r > len) {
            onReceive(ptr.asSlice(0, r));
        } else if (r == 0) {
            close();
        }
    }

    public void ctl(int r) {
        try (Mutex _ = channelState.withMutex()) {
            int state = channelState.get();
            int current = state & Constants.NET_RW;
            if (current != r) {
                osNetworkLibrary.ctl(channel.poller().mux(), channel.socket(), current, r);
                channelState.set((r - current) + state);
            }
        }
    }

    public void handleEvent(int r) {
        if (r == Constants.NET_W || r == Constants.NET_R || r == Constants.NET_RW) {
            ctl(r);
        } else if (r == Constants.NET_IGNORED) {
            throw new FrameworkException(ExceptionType.CONTEXT, Constants.UNREACHED);
        }
    }

    @Override
    public void onReadableEvent(MemorySegment segment, int len) {
        int r;
        try {
           r = protocol.onReadableEvent(segment, len);
        } catch (RuntimeException e) {
            System.out.println("Exception thrown in protocolPollerNode when invoking onReadableEvent()");
            close();
            return;
        }

        if (r >= 0) {
            handleReceived(segment, len, r);
        } else {
            handleEvent(r);
        }
    }

    @Override
    public void onWriteableEvent() {
        int r;
        r = protocol.onWritableEvent();

        if (r < 0) {
            handleEvent(r);
        }
    }

    @Override
    public void onRegisterTaggedMsg(PollerTask pollerTask) {
        if (pollerTask.channel() == channel && pollerTask.msg() instanceof TaggedMsg taggedMsg) {
            int tag = taggedMsg.tag();
            if (tag == channel.SEQ) {
                if (carrier != null) {
                    carrier.cas(Carrier.HOLDER, Carrier.FAILED);
                }
                carrier = taggedMsg.carrier();
            } else {
               if (nodeMap == null) {
                   nodeMap = new IntMap<>(MAX_SIZE);
               }
            }
        }
    }

    @Override
    public void onUnRegisterTaggedMsg(PollerTask pollerTask) {
        if (pollerTask.channel() == channel && pollerTask.msg() instanceof TaggedMsg taggedMsg) {
            int tag = taggedMsg.tag();
            if (tag == channel.SEQ) {
                if (carrier != null && taggedMsg.carrier() == carrier) {
                    carrier.cas(Carrier.HOLDER, Carrier.FAILED);
                }
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
        channel.shutdown(duration);
    }

    public void close() {
        if (nodeMap.remove(channel.socket().intValue(), this)) {
            if (tempBuffer != null) {
                tempBuffer.close();
                tempBuffer = null;
            }

        }
    }

    private void closeProtocol() {
        try {
            protocol.doClose();
        } catch (RuntimeException e) {
            System.out.println("failed to close protocol");
        }
    }
}
