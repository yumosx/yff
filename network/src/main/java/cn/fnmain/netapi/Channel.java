package cn.fnmain.netapi;

import cn.fnmain.Loc;
import cn.fnmain.Socket;
import cn.fnmain.thread.Poller;
import cn.fnmain.thread.Writer;

public interface Channel {
    Socket socket();
    Encoder encoder();
    Decoder decoder();
    Handler handler();
    Poller poller();
    Writer writer();
    Loc loc();
}
