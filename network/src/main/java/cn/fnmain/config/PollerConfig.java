package cn.fnmain.config;


import cn.fnmain.lib.Constants;
import cn.fnmain.NativeUtil;

public class PollerConfig {
    private int pollerCount = Math.max(NativeUtil.getCpuCores() >> 1, 4);
    private int maxEvent = 16;
    private int muxTimeout = 25;
    private int readBufferSize = 64 * Constants.KB;
    private int mapSize =Constants.KB;


    public int getPollerCount() {
        return pollerCount;
    }

    public void setPollerCount(int pollerCount) {
        this.pollerCount = pollerCount;
    }

    public int getMaxEvent() {
        return maxEvent;
    }

    public void setMaxEvent(int maxEvent) {
        this.maxEvent = maxEvent;
    }

    public int getMuxTimeout() {
        return muxTimeout;
    }

    public void setMuxTimeout(int muxTimeout) {
        this.muxTimeout = muxTimeout;
    }

    public int getReadBufferSize() {
        return readBufferSize;
    }

    public void setReadBufferSize(int readBufferSize) {
        this.readBufferSize = readBufferSize;
    }

    public int getMapSize() {
        return mapSize;
    }

    public void setMapSize(int mapSize) {
        this.mapSize = mapSize;
    }
}
