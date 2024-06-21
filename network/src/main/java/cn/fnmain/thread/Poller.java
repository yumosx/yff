package cn.fnmain.thread;

import cn.fnmain.Constants;
import cn.fnmain.execption.ExceptionType;
import cn.fnmain.execption.FrameworkException;
import cn.fnmain.config.PollerConfig;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class Poller {
    private final Thread pollerThread;
    private static final AtomicInteger counter = new AtomicInteger(0);
    private final Queue<PollerTask> readerTaskQueue = new MpscUnboundedAtomicArrayQueue<>(1024);

    private Thread createPollerThread(PollerConfig pollerConfig) {
        return null;
    }

    public Poller(PollerConfig  pollerConfig) {
        this.pollerThread = createPollerThread(pollerConfig);
    }

    public Thread thread() {
        return pollerThread;
    }

    public void submit(PollerTask pollerTask) {
        if (pollerTask == null || !readerTaskQueue.offer(pollerTask)) {
            throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        }
    }
}
