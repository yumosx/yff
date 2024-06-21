package cn.fnmain;

public class State {
    private final Mutex mutex = new Mutex();
    private int state;

    public State() {
        this(0);
    }

    public State(int initialState) {
        this.state = initialState;
    }

    public Mutex withMutex() {
        return mutex.acquire();
    }

    public int get() {
        return state;
    }

    public void set(int state) {
        this.state = state;
    }

    public boolean unregister(int mask) {
        boolean r = (state & mask) > 0;
        state &= ~mask;
        return r;
    }

    public boolean cas(int expectValue, int newValue) {
        if (state == expectValue) {
            state = newValue;
            return true;
        } else {
            return false;
        }
    }
}
