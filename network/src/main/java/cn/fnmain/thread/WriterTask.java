package cn.fnmain.thread;

import cn.fnmain.netapi.Channel;
import cn.fnmain.node.WriterCallback;

public record WriterTask(
        WriterTaskType type,
        Channel channel,
        Object msg,
        WriterCallback writerCallback
) {

}
