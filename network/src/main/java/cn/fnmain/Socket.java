package cn.fnmain;

public record Socket(int intValue, long longValue) {
    public Socket(int socket) {
        this(socket, socket);
    }

    public Socket(long socket) {
        this(NativeUtil.castInt(socket), socket);
    }

    @Override
    public int hashCode() {
        return intValue;
    }
}
