package cn.fnmain.node;


import cn.fnmain.*;
import cn.fnmain.execption.ExceptionType;
import cn.fnmain.execption.FrameworkException;
import cn.fnmain.lib.IntMap;
import cn.fnmain.lib.OsNetworkLibrary;
import cn.fnmain.netapi.Channel;
import cn.fnmain.netapi.TaggedResult;
import cn.fnmain.tcp.Protocol;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;

public class ProtocolPollerNode implements PollerNode {
    private final Protocol protocol;
    private final Channel channel;
    private State channelState;
    private IntMap<PollerNode> nodeMap;
    private WriteBuffer tempBuffer;
    private static final int MAX_LIST_SIZE = 64;
    private List<Object> entityList = new ArrayList<>(MAX_LIST_SIZE);

    private OsNetworkLibrary osNetworkLibrary = OsNetworkLibrary.CURRENT;

    public ProtocolPollerNode(IntMap<PollerNode> nodeMap, Protocol protocol, Channel channel) {
        this.nodeMap = nodeMap;
        this.protocol = protocol;
        this.channel = channel;
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

        //解析完成，调用handler来处理我们的对象
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
        } else {    //如果备份缓冲区，那么需要进行拼接
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


    /*
    在接收的时候我们会判断一下具体的情况
    */
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
    public void onReadableEvent(MemorySegment ptr, int len) {
        int r = protocol.onReadableEvent(ptr, len);

        if (r >= 0) {
            handleReceived(ptr, len, r);
        } else {
            handleEvent(r);
        }
    }

    @Override
    public void onWriteableEvent() {
        protocol.onWritableEvent();
    }

    @Override
    public void exit() {

    }

    @Override
    public void doExit() {

    }

    public void close() {

    }
}
