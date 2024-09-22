package cn.fnmain.netapi;

import cn.fnmain.execption.ExceptionType;
import cn.fnmain.execption.FrameworkException;
import cn.fnmain.lib.Constants;


public record TaggedMsg(
        int tag,
        Carrier carrier
) {
    public TaggedMsg {
        if(carrier == null) {
            throw new FrameworkException(ExceptionType.NETWORK, Constants.UNREACHED);
        }
    }

    public TaggedMsg(int tag) {
        this(tag, Carrier.create());
    }
}
