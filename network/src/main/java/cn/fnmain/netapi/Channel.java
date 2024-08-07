package cn.fnmain.netapi;

import cn.fnmain.Loc;
import cn.fnmain.Socket;
import cn.fnmain.node.WriterCallback;
import cn.fnmain.thread.Poller;
import cn.fnmain.thread.Writer;

import java.time.Duration;
import java.util.Collection;

public interface Channel {
    Socket socket();
    Encoder encoder();
    Decoder decoder();
    Handler handler();
    Poller poller();
    Writer writer();
    Loc loc();

    void sendMsg(Object msg, WriterCallback writerCallback);

    void sendMultipleMsg(Collection<Object> msg, WriterCallback writerCallback);

    default void sendMultipleMsg(Collection<Object> msgs) {
        sendMultipleMsg(msgs, null);
    }

    void shutdown(Duration duration);

    Duration defaultSendTimeoutDuration = Duration.ofSeconds(30);
    Duration DefaultShutDownDuration = Duration.ofSeconds(5);

    default void shutdown() {
        shutdown(DefaultShutDownDuration);
    }
}
