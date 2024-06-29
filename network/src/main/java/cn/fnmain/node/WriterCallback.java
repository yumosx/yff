package cn.fnmain.node;

import cn.fnmain.netapi.Channel;

public interface WriterCallback {
    void onSuccess(Channel channel);
    void onFailure(Channel channel);

    default void invokeOnsuccess(Channel channel) {
        try {
            onSuccess(channel);
        } catch (RuntimeException e) {

        }
    }

    default void invokeOnFailure(Channel channel) {
        try {
            onFailure(channel);
        } catch (RuntimeException e) {
            System.out.println("Err occurred while invoking onSuccess()");
        }
    }
}
