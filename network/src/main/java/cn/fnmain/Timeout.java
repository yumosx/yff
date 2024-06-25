package cn.fnmain;

import java.lang.foreign.MemorySegment;

public record Timeout(int val, MemorySegment ptr) {

}
