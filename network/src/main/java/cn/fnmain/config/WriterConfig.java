package cn.fnmain.config;

import cn.fnmain.lib.Constants;
import cn.fnmain.NativeUtil;

public class WriterConfig {
    private int writerCount = Math.max(NativeUtil.getCpuCores() >> 1, 4);
    private int writeBufferSize = 64 * Constants.KB;
    private int mapSize = Constants.KB;

    public int getWriterCount() {
        return writerCount;
    }

    public void setWriterCount(int writerCount) {
        this.writerCount = writerCount;
    }

    public int getWriteBufferSize() {
        return writeBufferSize;
    }

    public void setWriteBufferSize(int writeBufferSize) {
        this.writeBufferSize = writeBufferSize;
    }

    public int getMapSize() {
        return mapSize;
    }

    public void setMapSize(int mapSize) {
        this.mapSize = mapSize;
    }
}
