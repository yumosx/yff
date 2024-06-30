package cn.fnmain.thread;

public enum PollerTaskType {
    BIND,//将对应channel绑定到对应的线程上去
    UNBIND,
    REGISTER,
    UNREGISTER,
    CLOSE,
    POTENTIAL_EXIT,
    EXIT,
}
