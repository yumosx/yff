package cn.fnmain.netapi;

import cn.fnmain.execption.ExceptionType;
import cn.fnmain.execption.FrameworkException;
import cn.fnmain.lib.Constants;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public record Carrier(
        Thread thread,
        AtomicReference<Object> target
) {
    public Carrier {
        if(thread == null || target == null) {
            throw new FrameworkException(ExceptionType.CONTEXT, Constants.UNREACHED);
        }
    }

    public static final Object HOLDER = new Object();
    public static final Object FAILED = new Object();

    public static Carrier create() {
        Thread currentThread = Thread.currentThread();
        if(!currentThread.isVirtual()) {
            throw new FrameworkException(ExceptionType.CONTEXT, "Must be created in virtual thread");
        }
        return new Carrier(currentThread, new AtomicReference<>(HOLDER));
    }

    public void cas(Object expectedValue, Object newValue) {
        if(target.compareAndSet(expectedValue, newValue)) {
            LockSupport.unpark(thread);
        }
    }
}
