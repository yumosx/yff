package cn.fnmain;

/*
这个接口主要是用来控制net类
*/
public interface LifeCycle {
    void init();
    void exit() throws  InterruptedException;
}
