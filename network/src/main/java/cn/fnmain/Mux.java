package cn.fnmain;

import java.lang.foreign.MemorySegment;

public record Mux(MemorySegment winHandle, int epfd, int kqfd) {
    public static Mux mac(int kqfd) {
        return new Mux(NativeUtil.NULL_POINTER, Integer.MAX_VALUE, kqfd);
    }
}
