package cn.fnmain.config;

import cn.fnmain.netapi.Decoder;
import cn.fnmain.netapi.Encoder;
import cn.fnmain.netapi.Handler;

import java.util.function.Supplier;

public class ListenConfig {
    private Supplier<Encoder> encoderSupplier;
    private Supplier<Decoder> decoderSupplier;
    private Supplier<Handler> handlerSupplier;
}
