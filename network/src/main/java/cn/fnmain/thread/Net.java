package cn.fnmain.thread;

import cn.fnmain.*;
import cn.fnmain.config.ListenConfig;
import cn.fnmain.config.NetConfig;
import cn.fnmain.config.PollerConfig;
import cn.fnmain.config.WriterConfig;
import cn.fnmain.execption.ExceptionType;
import cn.fnmain.execption.FrameworkException;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class Net extends AbstractLifeCycle {
    private static final AtomicBoolean instance = new AtomicBoolean(false);
    private final State state = new State();


    public Net(NetConfig netConfig, PollerConfig pollerConfig, WriterConfig writerConfig) {
        if (netConfig == null || pollerConfig == null || writerConfig == null) {
            throw new NullPointerException();
        }

        if (!instance.compareAndSet(false, true)) {
            throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        }

        int PollerCounter = pollerConfig.getPollerCount();
        if (PollerCounter <= 0) {
            throw new FrameworkException(ExceptionType.NETWORK, "Poller instances cannot be zero");
        }

        int writerCount = writerConfig.getWriterCounter();
        if (writerCount <= 0) {
            throw new FrameworkException(ExceptionType.NETWORK, "Writer instances cannot be zero");
        }

        this.pollers = IntStream.range(0, pollerCount).mapToObj(_ -> new Poller(pollerConfig)).toList();
        this.writers = IntStream.range(0, writerCount).mapToObj(_ -> new Writer(writerConfig)).toList();
        this.netThread = createNetThread(netConfig);
    }

    private Thread createNetThread(NetConfig netConfig) {
        return Thread.ofPlatform().unstarted(() -> {
            // 创建主线程
        });
    }

    public void addListener(ListenConfig listenConfig) {
        try(Mutex _ = state.withMutex()) {
            int current = state.get();
            if (current > Constants.RUNNING) {
                throw new RuntimeException(Constants.UNREACHED);
            }
        }
    }


    @Override
    protected void doInit() {
        try(Mutex _ = state.withMutex()) {

        }
    }

    @Override
    protected void doExit() throws InterruptedException {
        try(Mutex _ = state.withMutex()) {

        }
    }
}
