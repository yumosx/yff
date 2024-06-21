package cn.fnmain.netapi;

import cn.fnmain.ReadBuffer;
import java.util.List;

public interface Decoder {
    void decode(ReadBuffer buffer, List<Object> entityList);
}
