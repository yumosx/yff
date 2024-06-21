package cn.fnmain.config;


import cn.fnmain.NativeUtil;

public class PollerConfig {
    private int pollerCount = Math.max(NativeUtil.getCpuCores() >> 1, 4);

    public int getPollerCount() {
        return pollerCount;
    }
}
