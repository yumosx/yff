package cn.fnmain;

import cn.fnmain.execption.ExceptionType;
import cn.fnmain.execption.FrameworkException;
import cn.fnmain.lib.Constants;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public record Mutex(Lock lock) implements AutoCloseable {

    public Mutex{
        if (lock == null) {
            throw new FrameworkException(ExceptionType.CONTEXT, Constants.UNREACHED);
        }
    }

    public Mutex() {
        this(new ReentrantLock());
    }

    public Mutex acquire() {
        lock.lock();
        return this;
    }

    @Override
    public void close(){
        lock.unlock();
    }
}
