package org.oiue.service.bytes.api;

import java.io.Serializable;
import java.util.Map;

import org.oiue.tools.StatusResult;

@SuppressWarnings("rawtypes")
public interface BytesService extends Serializable {
	public StatusResult registerDecodeEncoded(String type, BytesDecodeEncoded enDecode);
	
	public StatusResult unRregisterDecodeEncoded(String type);
	
	public byte[] encoded(byte[] source, Object rule, Map d);
	
	public StatusResult decode(byte[] s, Object rule, Map d);
}
