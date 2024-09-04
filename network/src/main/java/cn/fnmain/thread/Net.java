package cn.fnmain.thread;

import cn.fnmain.*;
import cn.fnmain.config.ListenConfig;
import cn.fnmain.config.NetConfig;
import cn.fnmain.config.PollerConfig;
import cn.fnmain.config.WriterConfig;
import cn.fnmain.execption.ExceptionType;
import cn.fnmain.execption.FrameworkException;
import cn.fnmain.lib.Constants;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

public class Net extends AbstractLifeCycle {
    private static final AtomicBoolean instance = new AtomicBoolean(false);
    private List<Poller> pollers;
    private List<Writer> writers;
    private Thread netThread;

    private final State state = new State();


    public Net(NetConfig netConfig, PollerConfig pollerConfig, WriterConfig writerConfig) {
        if (netConfig == null || pollerConfig == null || writerConfig == null) {
            throw new NullPointerException();
        }

        if (!instance.compareAndSet(false, true)) {
            throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        }

        int pollerCount = pollerConfig.getPollerCount();
        if (pollerCount <= 0) {
            throw new FrameworkException(ExceptionType.NETWORK, "Poller instances cannot be zero");
        }

        int writerCount = writerConfig.getWriterCount();
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
