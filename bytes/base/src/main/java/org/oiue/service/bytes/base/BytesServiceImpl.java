package org.oiue.service.bytes.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.oiue.service.bytes.api.BytesDecodeEncoded;
import org.oiue.service.bytes.api.BytesRuleField;
import org.oiue.service.bytes.api.BytesService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.StatusResult;
import org.oiue.tools.bytes.ByteUtil;
import org.oiue.tools.exception.ExceptionUtil;
import org.oiue.tools.map.MapUtil;

@SuppressWarnings({ "rawtypes", "serial", "unused", "unchecked" })
public class BytesServiceImpl implements BytesService {
	
	Logger logger;
	private static Map<String,BytesDecodeEncoded> endecodes = new HashMap();
	
	public BytesServiceImpl(LogService logService) {
		logger = logService.getLogger(this.getClass());
	}
	
	@Override
	public StatusResult registerDecodeEncoded(String type, BytesDecodeEncoded enDecode) {
		StatusResult sr = new StatusResult();
		endecodes.put(type, enDecode);
		return sr;
	}
	
	@Override
	public byte[] encoded(byte[] source, Object rule, Map d) {
		int index = 0;
		try {
			try {
				index = MapUtil.getInt(d, BytesRuleField.sys_packet_index);
			} catch (Throwable e) {}
			if (rule == null) {
				return source;
			}
			
			if (rule instanceof Map) {
				Map r = (Map) rule;
				
				String type = MapUtil.getString(r, "type");
				if (BytesRuleField.switchRegexCommand.equals(type)) {
					String key = MapUtil.getString(r, "key");
					Object v = MapUtil.get(d, key);
					Map switch_m = (Map) r.get("case");
					Object tmp_rule = switch_m.get(v);
					encoded(source, tmp_rule, d);
				} else if (BytesRuleField.loopRegexCommand.equals(type)) {
					String key = MapUtil.getString(r, "key");
					int loop = 0;
					try {
						loop = MapUtil.getInt(r, "count");
					} catch (Throwable e) {
						loop = MapUtil.getInt(d, key);
					}
					Object value = null;
					List data = new ArrayList<Map>();
					Object v = MapUtil.get(d, key);
					while (loop-- > 0) {
						Map loopdata = new HashMap();
						loopdata.putAll(d);
						source = encoded(source, v, d);
						index = (Integer) loopdata.remove(BytesRuleField.sys_packet_index);
						MapUtil.put(d, BytesRuleField.sys_packet_index, index);
						data.add(loopdata);
					}
					
					MapUtil.put(d, key, data);
				} else {
					String key = MapUtil.getString(r, "key");
					int size = MapUtil.getInt(r, "length");
					
					int multiply = 0;
					try {
						multiply = MapUtil.getInt(r, "multiply");
					} catch (Throwable e) {}
					
					Object value = MapUtil.get(d, key);
					if (value == null && "[source]".equals(key))
						value = d;
					
					if (size == -1) {
						try {
							size = value.toString().getBytes("UTF-8").length;
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}
					}
					
					BytesDecodeEncoded bpe = endecodes.get(type);
					if (bpe == null) {
						throw new RuntimeException("BytesPackageEncoded con not fond,type " + type + ";encoded=" + endecodes);
					}
					
					source = bpe.encoded(source, index, size, value);
					MapUtil.put(d, BytesRuleField.sys_packet_index, index + size);
				}
				
				return source;
			} else if (rule instanceof Collection) {
				Collection r = (Collection) rule;
				for (Object object : r) {
					source = encoded(source, object, d);
				}
				
				return source;
			} else {// key,type,size
				StringTokenizer st = new StringTokenizer((String) rule);
				String key = st.nextToken(",");
				String type = st.nextToken();
				String sizeStr = st.nextToken("|");
				int size = 0;
				sizeStr = sizeStr.substring(1);
				if (sizeStr.startsWith("g")) {
					size = MapUtil.getInt(d, sizeStr.substring(1));
				} else {
					size = Integer.valueOf(sizeStr);
				}
				
				Object value = MapUtil.get(d, key);
				if (value == null && "[source]".equals(key))
					value = d;
				
				if (size == -1) {
					try {
						if (value == null)
							size = 0;
						else
							size = value.toString().getBytes("UTF-8").length;
					} catch (Exception e) {
						logger.error(d + "|" + key + ":" + e.getMessage(), e);
					}
				}
				
				BytesDecodeEncoded bpe = endecodes.get(type);
				
				if (bpe == null) {
					throw new RuntimeException("BytesPackageEncoded con not fond,type=" + type + ";encoded=" + endecodes);
				}
				source = bpe.encoded(source, index, size, value);
				
				if (st.hasMoreTokens()) {
					String rules = st.nextToken("");
					rules = rules.substring(1);
					MapUtil.put(d, BytesRuleField.sys_packet_index, index + size);
					return encoded(source, rules, d);
				} else
					return source;
				
			}
			
		} catch (Throwable e) {
			throw new RuntimeException("rule=" + rule + "   d=" + d, e);
		}
	}
	
	@Override
	public StatusResult decode(byte[] s, Object rule, Map d) {
		StatusResult sr = new StatusResult();
		int index = 0;
		int size = 0;
		String name = null;
		String type = null;
		try {
			try {
				index = MapUtil.getInt(d, BytesRuleField.sys_packet_index);
				if (index + 1 > s.length) {
					sr.setResult(StatusResult._ncriticalAbnormal);
					sr.setDescription("source length error!");
					return sr;
				}
			} catch (Throwable e) {}
			if (rule instanceof Map) {
				Map r = (Map) rule;
				type = MapUtil.getString(r, "type");
				if (BytesRuleField.switchRegexCommand.equals(type)) {
					String key = MapUtil.getString(r, "key");
					Object v = MapUtil.get(d, key);
					Map switch_m = (Map) r.get("case");
					Object tmp_rule = switch_m.get(v + "");
					decode(s, tmp_rule, d);
				} else if (BytesRuleField.loopRegexCommand.equals(type)) {
					String key = MapUtil.getString(r, "key");
					int loop = 0;
					try {
						loop = MapUtil.getInt(r, "count");
					} catch (Throwable e) {
						loop = MapUtil.getInt(d, key);
					}
					Object value = null;
					List data = new ArrayList<Map>();
					Object v = MapUtil.get(r, key);
					while (loop-- > 0) {
						Map loopdata = new HashMap();
						loopdata.putAll(d);
						decode(s, v, loopdata);
						index = (Integer) loopdata.remove(BytesRuleField.sys_packet_index);
						MapUtil.put(d, BytesRuleField.sys_packet_index, index);
						data.add(loopdata);
					}
					
					MapUtil.put(d, key, data);
				} else {
					if (r.size() == 0)
						return null;
					name = MapUtil.getString(r, "name");
					size = MapUtil.getInt(r, "length");
					
					this.decode(type, name, index, size, s, d);
				}
			} else if (rule instanceof Collection) {
				Collection r = (Collection) rule;
				for (Object object : r) {
					decode(s, object, d);
				}
			} else { // key,type,size|
				StringTokenizer st = new StringTokenizer((String) rule);
				name = st.nextToken(",");
				type = st.nextToken();
				String sizeStr = st.nextToken("|");
				sizeStr = sizeStr.substring(1);
				if (sizeStr.startsWith("g")) {
					size = MapUtil.getInt(d, sizeStr.substring(1));
				} else {
					size = Integer.valueOf(sizeStr);
				}
				
				if (size == -1) {
					if (s != null)
						size = s.length - index;
				}
				Object value = null;
				
				if (BytesRuleField.loopRegexCommand.equals(type)) {
					String regex = "";
					while (size-- > 0) {
						regex += "|" + st.nextToken("|");
					}
					int loop = (Integer) d.get(name);
					List data = new ArrayList<Map>();
					
					while (loop-- > 0) {
						Map loopdata = new HashMap();
						value = decode(s, regex.substring(1), loopdata);
						data.add(loopdata);
					}
					
					MapUtil.put(d, name, data);
					
					if (st.hasMoreTokens())
						st = new StringTokenizer(st.nextToken("").substring(1));
				} else
					this.decode(type, name, index, size, s, d);
				
				if (st.hasMoreTokens()) {
					String rules = st.nextToken("");
					rules = rules.substring(1);
					return decode(s, rules, d);
				}
				
			}
			
		} catch (Throwable e) {
			throw new RuntimeException("== rule=" + rule + "   d=" + d + "  index=" + index + "  source=" + ByteUtil.toHexString(s) + " " + ExceptionUtil.getCausedBySrcMsg(e), e);
		}
		
		sr.setResult(StatusResult._SUCCESS);
		return sr;
	}
	
	private void decode(String type, String name, int index, int size, byte[] s, Map d) {
		
		try {
			if (type != null && name != null) {
				BytesDecodeEncoded bpd = endecodes.get(type);
				
				if (bpd == null) {
					logger.error("the decode type[" + type + "] can not found!");
				} else {
					Object value = bpd.decode(s, index, size, d);
					
					MapUtil.put(d, name, value);
				}
			}
		} catch (Throwable e) {
			logger.error("decode error s[" + ByteUtil.toHexString(s) + "]key:" + name + ",type:" + type + ",index:" + index + ",size:" + size + "|" + e.getMessage(), e);
		}
		MapUtil.put(d, BytesRuleField.sys_packet_index, index + size);
	}
	
	@Override
	public StatusResult unRregisterDecodeEncoded(String type) {
		StatusResult sr = new StatusResult();
		return sr;
	}
	
}
