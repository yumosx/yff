package cn.fnmain.thread;

import cn.fnmain.Constants;
import cn.fnmain.Mux;
import cn.fnmain.State;
import cn.fnmain.config.PollerConfig;
import cn.fnmain.execption.ExceptionType;
import cn.fnmain.execption.FrameworkException;
import cn.fnmain.lib.IntMap;
import cn.fnmain.netapi.Channel;
import cn.fnmain.node.PollerNode;
import cn.fnmain.node.SentryPollerNode;
import cn.fnmain.tcp.Sentry;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class Poller {
    private final Thread pollerThread;
    private Mux mux;
    private IntMap<PollerNode> nodeMap;
    private State channelState;
    private Queue<PollerTask> queue;
    private static final AtomicInteger counter = new AtomicInteger(0);

    public Mux mux() {
        return mux;
    }

    public Thread thread() {
        return pollerThread;
    }

    private Thread createPollerThread(PollerConfig pollerConfig) {
        int sequence = counter.incrementAndGet();
        return Thread.ofPlatform().name(STR."").unstarted(()->{

        });
    }

    public Poller(PollerConfig  pollerConfig) {
        this.pollerThread = createPollerThread(pollerConfig);
    }

    private void handleBindMsg(IntMap<PollerNode> map, PollerTask pollerTask) {
        Channel channel = pollerTask.channel();
        SentryPollerNode sentryPollerNode = switch (pollerTask.msg()) {
            case Sentry sentry -> new SentryPollerNode(nodeMap, channel, sentry, null);
            default -> throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        };
        nodeMap.put(channel.socket().intValue(), sentryPollerNode);
    }

    public State processTask() {
        while (true) {
            PollerTask pollerTask = queue.poll();
            if (pollerTask == null) {
                return channelState;
            }
            switch (pollerTask.type()) {
                case BIND:  handleBindMsg(nodeMap, pollerTask);
            }
        }
    }

    public void submit(PollerTask pollerTask) {

    }
}
