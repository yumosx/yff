package cn.fnmain.node.impl;

import cn.fnmain.State;
import cn.fnmain.protocol.Protocol;

public record ProtoAndState(Protocol protocol, State state) {
}
