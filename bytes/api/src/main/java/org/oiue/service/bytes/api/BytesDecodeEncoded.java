package org.oiue.service.bytes.api;

import java.io.Serializable;
import java.util.Map;

public interface BytesDecodeEncoded extends Serializable {
    /**
     * 编码
     * @param s
     * @param index
     * @param size
     * @param value
     * @return
     */
    public byte[] encoded(byte[] s,int index,int size,Object value);
    
    /**
     * 解码
     * @param s
     * @param index
     * @param size
     * @param d
     * @return
     */
    public Object decode(byte[] s,int index,int size,Map<?, ?> d);
}
