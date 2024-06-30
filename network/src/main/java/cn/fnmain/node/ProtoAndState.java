package cn.fnmain.node;

import cn.fnmain.State;
import cn.fnmain.tcp.Protocol;

public record ProtoAndState(Protocol protocol, State state) {
}
