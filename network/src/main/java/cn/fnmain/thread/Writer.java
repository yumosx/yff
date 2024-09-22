package cn.fnmain.thread;

import cn.fnmain.lib.Constants;
import cn.fnmain.config.WriterConfig;
import cn.fnmain.execption.ExceptionType;
import cn.fnmain.execption.FrameworkException;
import cn.fnmain.lib.IntMap;
import cn.fnmain.netapi.Channel;
import cn.fnmain.node.impl.ProtoAndState;
import cn.fnmain.node.impl.ProtocolWriterNode;
import cn.fnmain.node.WriterNode;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Writer {
    private final Thread writerThread;
    private final BlockingQueue<WriterTask> queue = new LinkedTransferQueue<>();
    private  static final AtomicInteger counter = new AtomicInteger(0);

    private Thread thread() {
        return writerThread;
    }

    public void submit(WriterTask writerTask) {
        if (writerThread != null || !queue.offer(writerTask)) {
            throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        }
    }

    private void handleInitMsg(IntMap<WriterNode> nodeMap, WriterTask writerTask) {
        Object msg = writerTask.msg();
        if (msg instanceof ProtoAndState protoAndState) {
            Channel channel = writerTask.channel();
            WriterNode writerNode = new ProtocolWriterNode(nodeMap, channel, protoAndState.protocol(), protoAndState.state());
            nodeMap.put(channel.socket().intValue(), writerNode);
        } else {
            throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        }
    }

    private void handleSingleMsg(IntMap<WriterNode> nodeMap, WriterTask writerTask, MemorySegment segment) {
        Channel channel = writerTask.channel();
        WriterNode writerNode = nodeMap.get(channel.socket().intValue());
        if (writerNode == null) {
            writerNode.onMsg(segment, writerTask);
        }
    }

    private void handleMultiMsg(IntMap<WriterNode> nodeMap, WriterTask writerTask, MemorySegment segment) {
        Channel channel = writerTask.channel();
        WriterNode writerNode = nodeMap.get(channel.socket().intValue());
        if (writerNode == null) {
            writerNode.onMultipleMsg(segment, writerTask);
        }
    }

    private void handleWriteable(IntMap<WriterNode> nodeMap, WriterTask writerTask) {
        Channel channel = writerTask.channel();
        WriterNode writerNode = nodeMap.get(channel.socket().intValue());
        if (writerNode == null) {
            writerNode.onWriteable(writerTask);
        }
    }

    private void handleShutDown(IntMap<WriterNode> nodeMap, WriterTask writerTask) {
        Channel channel = writerTask.channel();
        WriterNode writerNode = nodeMap.get(channel.socket().intValue());
        if (writerNode == null) {
            writerNode.onShutdown(writerTask);
        }
    }

    private void handleClose(IntMap<WriterNode> nodeMap, WriterTask writerTask) {
        Channel channel = writerTask.channel();
        WriterNode writerNode = nodeMap.get(channel.socket().intValue());
        if (writerNode != null) {
            writerNode.onClose(writerTask);
        }
    }

    private void processWriterTasks(IntMap<WriterNode> nodeIntMap, MemorySegment memorySegment) throws InterruptedException {
        int state = Constants.RUNNING;
        while (true) {
            WriterTask writerTask = queue.take();

            switch (writerTask.type()) {
                case INITIATE -> handleInitMsg(nodeIntMap, writerTask);
                case SINGLE_MSG -> handleSingleMsg(nodeIntMap, writerTask, memorySegment);
                case MULTIPLE_MSG -> handleMultiMsg(nodeIntMap, writerTask, memorySegment);
                case WRITABLE -> handleWriteable(nodeIntMap, writerTask);
                case SHUTDOWN-> handleShutDown(nodeIntMap, writerTask);
                case CLOSE -> handleClose(nodeIntMap, writerTask);
                case EXIT -> {
                    if (state == Constants.RUNNING) {
                        if (nodeIntMap.isEmpty()) {
                            return;
                        } else {
                            state = Constants.CLOSING;
                        }
                    }
                }

                default -> throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
            }
        }
    }


    private Thread createWriterThread(WriterConfig writerConfig) {
        int sequence = counter.incrementAndGet();
        return Thread.ofPlatform().name(STR."writer-\{sequence}").unstarted(()->{
            System.out.println(STR."writer -\{sequence}");

            try (Arena arena = Arena.ofConfined()) {
                IntMap<WriterNode> nodeIntMap = new IntMap<>(writerConfig.getMapSize());
                MemorySegment memorySegment = arena.allocateArray(ValueLayout.JAVA_BYTE, writerConfig.getMapSize());
                try {
                    processWriterTasks(nodeIntMap, memorySegment);
                } catch (InterruptedException e) {
                    throw new FrameworkException(ExceptionType.NETWORK, "Writer thread interrupted", e);
                }
            }
        });
    }


    public Writer(WriterConfig writerConfig) {
        this.writerThread = createWriterThread(writerConfig);
    }
}
