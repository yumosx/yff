package cn.fnmain.netapi;

import cn.fnmain.Loc;
import cn.fnmain.Socket;
import cn.fnmain.node.WriterCallback;
import cn.fnmain.thread.Poller;
import cn.fnmain.thread.Writer;

import java.time.Duration;
import java.util.Collection;
import java.util.function.IntFunction;

public sealed interface Channel permits ChannelImpl {
    int SEQ = 0;

    Duration defaultSendTimeoutDuration = Duration.ofSeconds(30);
    Duration defaultShutdownDuration = Duration.ofSeconds(5);

    Socket socket();

    Encoder encoder();

    Decoder decoder();

    Handler handler();

    Poller poller();

    Writer writer();

    Loc loc();

    void sendMsg(Object msg, WriterCallback writerCallback);

    default void sendMsg(Object msg) {
        sendMsg(msg, null);
    }


    void sendMultipleMsg(Collection<Object> msgs, WriterCallback writerCallback);

    default void sendMultipleMsg(Collection<Object> msgs) {
        sendMultipleMsg(msgs, null);
    }


    Object sendTaggedMsg(IntFunction<Object> taggedFunction, Duration timeout);

    default Object sendTaggedMsg(IntFunction<Object> taggedFunction) {
        return sendTaggedMsg(taggedFunction, defaultSendTimeoutDuration);
    }

    Object sendMultipleTaggedMsg(IntFunction<Collection<Object>> taggedFunctions, Duration timeout);

    default Object sendMultipleTaggedMsg(IntFunction<Collection<Object>> taggedFunctions) {
        return sendMultipleTaggedMsg(taggedFunctions, defaultSendTimeoutDuration);
    }

    Object sendCircleMsg(Object msg, Duration timeout);

    default Object sendCircleMsg(Object msg) {
        return sendCircleMsg(msg, defaultSendTimeoutDuration);
    }

    Object sendMultipleCircleMsg(Collection<Object> msgs, Duration timeout);

    default Object sendMultipleCircleMsg(Collection<Object> msgs) {
        return sendMultipleCircleMsg(msgs, defaultSendTimeoutDuration);
    }

    void shutdown(Duration duration);

    default void shutdown() {
        shutdown(defaultShutdownDuration);
    }
}