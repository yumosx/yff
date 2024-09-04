package cn.fnmain;

import cn.fnmain.execption.ExceptionType;
import cn.fnmain.execption.FrameworkException;
import cn.fnmain.lib.Constants;

import java.lang.foreign.*;


public record Timeout(int val, MemorySegment ptr) {
    private static final StructLayout timespecLayout = MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withName("tv_sec"),
            ValueLayout.JAVA_LONG.withName("tv_nsec")
    );

    private static final long secOffset = timespecLayout.byteOffset(MemoryLayout.PathElement.groupElement("tv_sec"));
    private static final long nsecOffset = timespecLayout.byteOffset(MemoryLayout.PathElement.groupElement("tv_nsec"));



    public static Timeout of(Arena arena, int milliseconds) {
        switch (NativeUtil.ostype()) {
            case Linux -> {
                return new Timeout(milliseconds, null);
            }
            case null, default -> throw new FrameworkException(ExceptionType.NATIVE, Constants.UNREACHED);
        }
    }
}
