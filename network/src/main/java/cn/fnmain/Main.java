package cn.fnmain;

import cn.fnmain.config.IpType;
import cn.fnmain.config.PollerConfig;
import cn.fnmain.config.WriterConfig;
import cn.fnmain.thread.*;

public class Main {
    public static void main(String[] args) {
        //接口演示
        Loc loc = new Loc(IpType.IPV4, "127.0.0.1", 8080);
        System.out.println(loc.ipType());

        System.out.println(NativeUtil.ostype());
        System.out.println(NativeUtil.getCpuCores());

        //poller线程
        PollerConfig pollerConfig = new PollerConfig();
        Poller poller = new Poller(pollerConfig);
        poller.submit(new PollerTask(PollerTaskType.BIND, null, "xxxx"));

        //writer线程
        WriterConfig writerConfig = new WriterConfig();
        Writer writer = new Writer(writerConfig);
        writer.submit(new WriterTask(WriterTaskType.SINGLE_MSG, null, "xxx", null));
    }
}