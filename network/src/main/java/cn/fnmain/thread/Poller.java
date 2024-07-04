package cn.fnmain.thread;

import cn.fnmain.Constants;
import cn.fnmain.Mux;
import cn.fnmain.State;
import cn.fnmain.Timeout;
import cn.fnmain.config.PollerConfig;
import cn.fnmain.execption.ExceptionType;
import cn.fnmain.execption.FrameworkException;
import cn.fnmain.lib.IntMap;
import cn.fnmain.lib.IntPair;
import cn.fnmain.lib.OsNetworkLibrary;
import cn.fnmain.netapi.Channel;
import cn.fnmain.node.PollerNode;
import cn.fnmain.node.SentryPollerNode;
import cn.fnmain.tcp.Sentry;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class Poller {
    private final Thread pollerThread;
    private Mux mux;
    private IntMap<PollerNode> nodeMap;
    private State channelState;
    private Queue<PollerTask> queue;
    private OsNetworkLibrary os = OsNetworkLibrary.CURRENT;
    private static final AtomicInteger counter = new AtomicInteger(0);

    public Mux mux() {
        return mux;
    }

    public Thread thread() {
        return pollerThread;
    }

    /*
     这是一个辅助函数, 这个函数主要的作用是为了检查返回的操作码
     */
    public boolean checkErr(int t) {
        if (t < 0) {
            int errno = Math.abs(t);
            if (errno == os.interruptCode()) {
                return false;
            } else {
                throw new FrameworkException(ExceptionType.NETWORK, STR."Mux wait failed with errno : \{errno}");
            }
        }
        return true;
    }

    /*
    连接的认证
    也就是处理bind消息，这个阶段会调用对应sentryPollerNode
    */
    private void handleBindMsg(IntMap<PollerNode> map, PollerTask pollerTask) {
        Channel channel = pollerTask.channel();
        SentryPollerNode sentryPollerNode = switch (pollerTask.msg()) {
            case Sentry sentry -> new SentryPollerNode(nodeMap, channel, sentry, null);
            default -> throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        };
        nodeMap.put(channel.socket().intValue(), sentryPollerNode);
    }

    /*
    消费者函数
    根据不同的任务类型，来执行相应的操作, 主要是转构造对应的节点
    */
    public int processTask(IntMap nodeMap, int state) {
        while (true) {
            PollerTask pollerTask = queue.poll();

            if (pollerTask == null) {
                return state;
            }
            switch (pollerTask.type()) {
                case BIND:  handleBindMsg(nodeMap, pollerTask);
            }
        }
    }

    private Thread createPollerThread(PollerConfig pollerConfig) {
        int sequence = counter.getAndIncrement();

        return Thread.ofPlatform().name(STR."poller-\{sequence}").unstarted(()->{
            System.out.println(STR."init the poller thread-\{sequence}");
            IntMap<PollerNode> nodeIntMap = new IntMap<>(pollerConfig.getMapSize());

            try (Arena arena = Arena.ofConfined()) {
                int maxEvent = pollerConfig.getMaxEvent();

                //分配对应的内存
                MemorySegment events = arena.allocate(MemoryLayout.sequenceLayout(maxEvent, os.eventLayout()));
                MemorySegment[] reversedArray = new MemorySegment[maxEvent];
                int readBufferSize = pollerConfig.getReadBufferSize();

                for (int i = 0; i < reversedArray.length; i++) {
                    reversedArray[i] = arena.allocateArray(ValueLayout.JAVA_BYTE, readBufferSize);
                }

                int state = Constants.RUNNING;
                Timeout timeout = Timeout.of(arena, pollerConfig.getMuxTimeout());

                while (true) {
                    //操作系统作为监听者
                    int t = os.muxWait(mux, events, maxEvent, timeout);

                    if (!checkErr(t)) return;
                    state = processTask(nodeMap, state);
                    if (state == Constants.STOPPED) break;

                    for (int i = 0; i < t; i++) {
                        MemorySegment reversed = reversedArray[i];
                        IntPair pair = os.access(events, i);

                        //通过这种nodeMap来维护
                        PollerNode pollerNode = nodeIntMap.get(pair.first());

                        if (pollerNode != null) {
                            int event = pair.second();

                            if (event == Constants.NET_W) {
                                pollerNode.onWriteableEvent();
                            } else if (event == Constants.NET_R || event == Constants.NET_OTHER) {
                                pollerNode.onReadableEvent(reversed, readBufferSize);
                            } else {
                                throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
                            }
                        }
                    }
                }
            } finally {
                System.out.println(STR."exiting poller thread-\{sequence}");
                os.exitMux(mux);
            }

        });
    }

    public Poller(PollerConfig  pollerConfig) {
        this.pollerThread = createPollerThread(pollerConfig);
    }


    public void submit(PollerTask pollerTask) {
        if (pollerTask != null) {
            queue.add(pollerTask);
        }
    }
}
