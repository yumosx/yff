package cn.fnmain.node;

import cn.fnmain.thread.WriterTask;

import java.lang.foreign.MemorySegment;

public interface WriterNode {
    void onMsg(MemorySegment memorySegment, WriterTask writerTask);
    void onMultipleMsg(MemorySegment memorySegment, WriterTask writerTask);
    void onShutdown(WriterTask writerTask);
    void onWriteable(WriterTask writerTask);
    void onClose(WriterTask writerTask);
    void close();
}
