package cn.fnmain;

public interface WriteBufferPolicy {
    void resize(WriteBuffer writeBuffer, long nextIndex);
    void close(WriteBuffer writeBuffer);
}
