package cn.fnmain;

public interface LifeCycle {
    void init();
    void exit() throws  InterruptedException;
}
