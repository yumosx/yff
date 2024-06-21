package cn.fnmain.netapi;

import cn.fnmain.WriterBuffer;

@FunctionalInterface
public interface Encoder {
    void encode(WriterBuffer writerBuffer, Object o);
}
