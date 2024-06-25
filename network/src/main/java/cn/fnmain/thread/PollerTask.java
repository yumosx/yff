package cn.fnmain.thread;

import cn.fnmain.netapi.Channel;

public record PollerTask( PollerTaskType type, Channel channel, Object msg) {}