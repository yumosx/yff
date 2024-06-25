package cn.fnmain.thread;

import cn.fnmain.config.WriterConfig;

import java.util.concurrent.atomic.AtomicInteger;

public class Writer {
    private final Thread writerThread;
    private  static final AtomicInteger counter = new AtomicInteger(0);

    private Thread thread() {
        return writerThread;
    }

    public void submit(WriterTask writerTask) {

    }


    private Thread createWriterThread(WriterConfig config) {
        int sequence = counter.incrementAndGet();
        return Thread.ofPlatform().name(STR."writer-\{sequence}").unstarted(()->{

        });
    }

    public Writer(WriterConfig writerConfig) {
        this.writerThread = createWriterThread(writerConfig);
    }
}
