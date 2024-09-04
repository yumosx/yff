package cn.fnmain;

public final class Clock {
    private Clock() {
        throw new UnsupportedOperationException();
    }

    public static long current() {
        return System.currentTimeMillis();
    }

    public static long nano() {
        return System.nanoTime();
    }

    public static long elapsed(long nano) {
        return nano() - nano;
    }
}
