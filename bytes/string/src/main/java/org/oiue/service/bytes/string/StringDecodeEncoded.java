package org.oiue.service.bytes.string;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.oiue.service.bytes.api.BytesDecodeEncoded;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.bytes.ByteUtil;

@SuppressWarnings({ "serial"})
public class StringDecodeEncoded implements BytesDecodeEncoded, Serializable {

    Logger logger;

    public StringDecodeEncoded(LogService logService) {
        this.logger = logService.getLogger(this.getClass());
    }

    @Override
    public byte[] encoded(byte[] s, int index, int size, Object value) {
        if (value == null)
            return s;
        byte[] sb = value.toString().getBytes();
        if (size == -1)
            size = sb.length;
        byte[] rtn = null;
        if (s != null && s.length > index + size) {
            rtn = s;
        } else {
            rtn = new byte[index + size];
            if (s != null)
                System.arraycopy(s, 0, rtn, 0, s.length);
        }
        System.arraycopy(sb, 0, rtn, index, size > sb.length ? sb.length : size);
        if (logger.isDebugEnabled()) {
            logger.debug("string rtn:" + ByteUtil.toHexString(rtn));
        }
        return rtn;
    }

    @Override
    public Object decode(byte[] s, int index, int size, Map<?, ?> d) {
        if(s==null||(s.length==0&&size==-1)){
            return "";
        }
        if (s == null || s.length < index + size)
            throw new RuntimeException("undecode s[" + ByteUtil.toHexString(s) + "] ,index=" + index + ",size=" + size);
        byte[] strb = new byte[size];
        System.arraycopy(s, index, strb, 0, size);

        try {
            return new String(strb, "UTF-8").trim();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        // try {
        // return new String(getUTF8BytesFromGBKString(new String(strb,"GBK")),"UTF-8");
        // } catch (UnsupportedEncodingException e) {
        // throw new RuntimeException(e);
        // }
    }

    public static byte[] getUTF8BytesFromGBKString(String gbkStr) {
        int n = gbkStr.length();
        byte[] utfBytes = new byte[3 * n];
        int k = 0;
        for (int i = 0; i < n; i++) {
            int m = gbkStr.charAt(i);
            if (m < 128 && m >= 0) {
                utfBytes[k++] = (byte) m;
                continue;
            }
            utfBytes[k++] = (byte) (0xe0 | (m >> 12));
            utfBytes[k++] = (byte) (0x80 | ((m >> 6) & 0x3f));
            utfBytes[k++] = (byte) (0x80 | (m & 0x3f));
        }
        if (k < utfBytes.length) {
            byte[] tmp = new byte[k];
            System.arraycopy(utfBytes, 0, tmp, 0, k);
            return tmp;
        }
        return utfBytes;
    }

}
