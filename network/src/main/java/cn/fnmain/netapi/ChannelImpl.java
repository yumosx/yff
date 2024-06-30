package cn.fnmain.netapi;

import cn.fnmain.Constants;
import cn.fnmain.Loc;
import cn.fnmain.Socket;
import cn.fnmain.node.WriterCallback;
import cn.fnmain.thread.Poller;
import cn.fnmain.thread.Writer;
import cn.fnmain.thread.WriterTask;
import cn.fnmain.thread.WriterTaskType;

import java.time.Duration;
import java.util.Collection;

public record ChannelImpl(Socket socket, Encoder encoder, Decoder decoder, Handler handler, Poller poller, Writer writer)
        implements Channel
{

    @Override
    public Socket socket() {
        return null;
    }

    @Override
    public Encoder encoder() {
        return null;
    }

    @Override
    public Decoder decoder() {
        return null;
    }

    @Override
    public Handler handler() {
        return null;
    }

    @Override
    public Poller poller() {
        return null;
    }

    @Override
    public Writer writer() {
        return null;
    }

    @Override
    public Loc loc() {
        return null;
    }

    @Override
    public void sendMsg(Object msg, WriterCallback writerCallback) {
        if (msg == null) {
            throw new RuntimeException(Constants.UNREACHED);
        }
        writer.submit(new WriterTask(WriterTaskType.SINGLE_MSG, this, msg, writerCallback));
    }

    @Override
    public void sendMultipleMsg(Collection<Object> msgs, WriterCallback writerCallback) {
        if (msgs == null) {
            throw new RuntimeException(Constants.UNREACHED);
        }

        writer.submit(new WriterTask(WriterTaskType.MULTIPLE_MSG, this, msgs, writerCallback));
    }

    @Override
    public void shutdown(Duration duration) {

    }
}
