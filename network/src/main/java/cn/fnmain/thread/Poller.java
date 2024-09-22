package cn.fnmain.thread;

import cn.fnmain.lib.Constants;
import cn.fnmain.Mux;
import cn.fnmain.Timeout;
import cn.fnmain.config.PollerConfig;
import cn.fnmain.execption.ExceptionType;
import cn.fnmain.execption.FrameworkException;
import cn.fnmain.lib.IntMap;
import cn.fnmain.lib.IntPair;
import cn.fnmain.lib.OsNetworkLibrary;
import cn.fnmain.netapi.Channel;
import cn.fnmain.node.PollerNode;
import cn.fnmain.node.impl.SentryPollerNode;
import cn.fnmain.protocol.Sentry;
import org.jctools.queues.atomic.MpscUnboundedAtomicArrayQueue;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;


public class Poller {
    private final Thread pollerThread;
    private static final AtomicInteger counter = new AtomicInteger(0);
    private Queue<PollerTask> queue = new MpscUnboundedAtomicArrayQueue<>(Constants.KB);
    private OsNetworkLibrary os = OsNetworkLibrary.CURRENT;
    private Mux mux = os.createMux();

    public Mux mux() {
        return mux;
    }

    public Thread thread() {
        return pollerThread;
    }

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

    private void handleBindMsg(IntMap<PollerNode> map, PollerTask pollerTask) {
        Channel channel = pollerTask.channel();

        SentryPollerNode sentryPollerNode = switch (pollerTask.msg()) {
            case Sentry sentry -> new SentryPollerNode(map, channel, sentry, null);
            default -> throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        };

        map.put(channel.socket().intValue(), sentryPollerNode);
    }

    private void handleUnBindMsg(IntMap<PollerNode>map, PollerTask pollerTask) {
        Channel channel = pollerTask.channel();
        PollerNode pollerNode = map.get(channel.socket().intValue());

        if (pollerNode instanceof SentryPollerNode sentryPollerNode) {
            sentryPollerNode.onClose(pollerTask);
        }
    }

    private void handleRegisterMsg(IntMap<PollerNode> map, PollerTask pollerTask) {
        Channel channel = pollerTask.channel();
        PollerNode pollerNode = map.get(channel.socket().intValue());
        if (pollerNode != null) {
            pollerNode.onRegisterTaggedMsg(pollerTask);
        }
    }

    private void handleUnRegisterMsg(IntMap<PollerNode> map, PollerTask pollerTask) {
        Channel channel = pollerTask.channel();
        PollerNode pollerNode = map.get(channel.socket().intValue());
        if (pollerNode != null) {
            pollerNode.onUnRegisterTaggedMsg(pollerTask);
        }
    }

    private void handleCloseMsg(IntMap<PollerNode> map, PollerTask pollerTask) {
        Channel channel = pollerTask.channel();
        PollerNode pollerNode = map.get(channel.socket().intValue());
        if (pollerNode != null) {
            pollerNode.onClose(pollerTask);
        }
    }

    public int processTask(IntMap nodeMap, int state) {
        while (true) {
            PollerTask pollerTask = queue.poll();

            if (pollerTask == null) {
                return state;
            }

            switch (pollerTask.type()) {
                case BIND       ->  handleBindMsg(nodeMap, pollerTask);
                case UNBIND     ->  handleUnBindMsg(nodeMap, pollerTask);
                case REGISTER   ->  handleRegisterMsg(nodeMap, pollerTask);
                case UNREGISTER ->  handleUnRegisterMsg(nodeMap, pollerTask);
                case CLOSE      ->  handleCloseMsg(nodeMap, pollerTask);
                case POTENTIAL_EXIT-> {
                    if (state == Constants.CLOSING && nodeMap.isEmpty()) {
                        return Constants.STOPPED;
                    }
                }
                case EXIT -> {
                    if(state == Constants.RUNNING) {
                        if(nodeMap.isEmpty()) {
                            return Constants.STOPPED;
                        }else if(pollerTask.msg() instanceof Duration duration) {
                            //nodeMap.asList().forEach(pollerNode -> pollerNode.exit(duration));
                            return Constants.CLOSING;
                        }else {
                            throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
                        }
                    }
                }
            }
        }
    }

    private void processEvent(PollerNode pollerNode, int event, MemorySegment segment, int size) {
        switch (event) {
            case Constants.NET_R:
                pollerNode.onReadableEvent(segment, size);
                break;
            case Constants.NET_W:
            case Constants.NET_OTHER:
                pollerNode.onWriteableEvent();
                break;
            default:
                throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        }
    }


    private Thread createPollerThread(PollerConfig pollerConfig) {
        int sequence = counter.getAndIncrement();

        return Thread.ofPlatform().name(STR."poller-\{sequence}").unstarted(()->{
            System.out.println(STR."init the poller thread-\{sequence}");
            IntMap<PollerNode> nodeIntMap = new IntMap<>(pollerConfig.getMapSize());

            try (Arena arena = Arena.ofConfined()) {
                int maxEvent = pollerConfig.getMaxEvent();
                System.out.println(maxEvent);

                Timeout timeout = Timeout.of(arena, pollerConfig.getMuxTimeout());

                MemorySegment events = arena.allocate(MemoryLayout.sequenceLayout(maxEvent, os.eventLayout()));
                MemorySegment[] reversedArray = new MemorySegment[maxEvent];

                int readBufferSize = pollerConfig.getReadBufferSize();

                for (int i = 0; i < reversedArray.length; i++) {
                    reversedArray[i] = arena.allocateArray(ValueLayout.JAVA_BYTE, readBufferSize);
                }

                int state = Constants.RUNNING;

                while (true) {
                    int t = os.muxWait(mux, events, maxEvent, timeout);
                    if (!checkErr(t))
                        return;

                    state = processTask(nodeIntMap, state);
                    if (state == Constants.STOPPED)
                        break;

                    for (int i = 0; i < t; i++) {
                        MemorySegment reversed = reversedArray[i];
                        IntPair pair = os.access(events, i);

                        PollerNode pollerNode = nodeIntMap.get(pair.first());

                        if (pollerNode != null) {
                            int event = pair.second();
                            processEvent(pollerNode, event, reversed, readBufferSize);
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
