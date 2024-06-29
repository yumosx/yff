package cn.fnmain.netapi;

import cn.fnmain.WriteBuffer;

@FunctionalInterface
public interface Encoder {
    void encode(WriteBuffer writerBuffer, Object o);
}
