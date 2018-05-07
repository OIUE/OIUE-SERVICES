package org.oiue.service.bytes.int16;

import java.io.Serializable;
import java.util.Map;

import org.oiue.service.bytes.api.BytesDecodeEncoded;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.bytes.ByteUtil;

@SuppressWarnings({ "serial" })
public class IntDecodeEncoded implements BytesDecodeEncoded, Serializable {
	
	Logger logger;
	
	public IntDecodeEncoded(LogService logService) {
		this.logger = logService.getLogger(this.getClass());
	}
	
	@Override
	public byte[] encoded(byte[] s, int index, int size, Object value) {
		byte[] rtn = null;
		if (s != null && s.length > index + size) {
			rtn = s;
		} else {
			rtn = new byte[index + size];
			if (s != null)
				System.arraycopy(s, 0, rtn, 0, s.length);
		}
		if (value != null) {
			if (value instanceof Double)
				value = ((Double) value).intValue();
			System.arraycopy(ByteUtil.int2bytes(Integer.valueOf(value + ""), size), 0, rtn, index, size);
		}
		
		// if(logger.isDebugEnabled()){
		// logger.debug("uint rtn:"+ByteUtil.toHexString(rtn));
		// }
		return rtn;
	}
	
	@Override
	public Object decode(byte[] s, int index, int size, Map<?, ?> d) {
		if (s == null || s.length < index + size)
			throw new RuntimeException("undecode s[" + ByteUtil.toHexString(s) + "] ,index=" + index + ",size=" + size + ",d=" + d);
		byte[] strb = new byte[4];
		System.arraycopy(s, index, strb, 4 - size, size);
		int value;
		value = (int) (((strb[0] & 0xFF) << 24) | ((strb[1] & 0xFF) << 16) | ((strb[2] & 0xFF) << 8) | (strb[3] & 0xFF));
		return value;
	}
	
}
