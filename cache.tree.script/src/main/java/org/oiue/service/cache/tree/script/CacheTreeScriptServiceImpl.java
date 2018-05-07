package org.oiue.service.cache.tree.script;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.oiue.service.cache.tree.CacheTreeService;

public class CacheTreeScriptServiceImpl implements CacheTreeScriptService {
	
	private CacheTreeService buffer;
	
	public CacheTreeScriptServiceImpl(CacheTreeService buffer) {
		this.buffer = buffer;
	}
	
	@Override
	public Object eval(String script) {
		String line = script.toString().trim();
		BufferScriptResult result = new BufferScriptResult();
		result.setResult(BufferScriptResult.OK);
		result.setData("ok");
		
		if (line.startsWith("p")) {
			parsePut(line, result);
		} else if (line.startsWith("r")) {
			parseRemove(line, result);
		} else if (line.startsWith("g")) {
			try {
				parseGet(line, result);
			} catch (Exception e) {
				result.setResult(BufferScriptResult.ERROR_FORMAT);
				result.setData(e.getLocalizedMessage());
			}
		} else {
			result.setResult(BufferScriptResult.ERROR_COMMAND);
		}
		return result;
	}
	
	private void parsePut(String line, BufferScriptResult result) {
		// pc path,data
		// pt tempPath,data
		// pcc path,type,data
		// ptc tempPath,type,data
		String cmdArray[] = line.split(" ", 2);
		if (cmdArray.length == 2) {
			String cmd = cmdArray[0];
			if (cmd.equalsIgnoreCase("pc")) {
				// pc path,data
				String tmp[] = cmdArray[1].split(",", 2);
				buffer.create(tmp[0], tmp[1]);
			} else if (cmd.equalsIgnoreCase("pt")) {
				// pt tempPath,data
				String tmp[] = cmdArray[1].split(",", 2);
				buffer.createTemp(tmp[0], tmp[1]);
			} else if (cmd.equalsIgnoreCase("pcc")) {
				// pcc path,type,data
				String tmp[] = cmdArray[1].split(",", 3);
				if (tmp.length == 3) {
					
				} else {
					result.setResult(BufferScriptResult.ERROR_ARGUMENTS);
				}
			} else if (cmd.equalsIgnoreCase("ptc")) {
				// ptc tempPath,type,data
				String tmp[] = cmdArray[1].split(",", 3);
				if (tmp.length == 3) {
					
				} else {
					result.setResult(BufferScriptResult.ERROR_ARGUMENTS);
				}
			} else {
				result.setResult(BufferScriptResult.ERROR_COMMAND);
			}
		} else {
			result.setResult(BufferScriptResult.ERROR_ARGUMENTS);
		}
	}
	
	private void parseRemove(String line, BufferScriptResult result) {
		// r path
		String cmdArray[] = line.split(" ", 2);
		if (cmdArray.length == 2) {
			String cmd = cmdArray[0];
			if (cmd.equalsIgnoreCase("r")) {
				buffer.delete(cmdArray[1], 0);
			} else {
				result.setResult(BufferScriptResult.ERROR_COMMAND);
			}
		} else {
			result.setResult(BufferScriptResult.ERROR_COMMAND);
		}
	}
	
	private void parseGet(String line, BufferScriptResult result) throws JSONException {
		// gt
		// gd path
		// gt path
		// gm name,key
		// gs name,key
		// gs name,x1,x2,y1,y2
		// gs name,x1,x2,y1,y2,dx,dy
		// gr oneToManyName,key,name
		// gr oneToManyName,key,name,x1,x2,y1,y2
		// gr oneToManyName,key,name,x1,x2,y1,y2,dx,dy
		String cmdArray[] = line.split(" ", 2);
		if (cmdArray.length == 1) {
			if (cmdArray[0].equalsIgnoreCase("gt")) {
				result.setData(buffer.getChildren("/"));
			} else if (cmdArray[0].equalsIgnoreCase("gd")) {
				result.setData(buffer.getData("/"));
			} else {
				result.setResult(BufferScriptResult.ERROR_COMMAND);
			}
		} else if (cmdArray.length == 2) {
			String cmd = cmdArray[0];
			if (cmd.equalsIgnoreCase("gt")) {
				result.setData(buffer.getChildren(cmdArray[1]));
			} else if (cmd.equalsIgnoreCase("gd")) {
				result.setData(buffer.getData(cmdArray[1]));
			} else {
				result.setResult(BufferScriptResult.ERROR_COMMAND);
			}
		} else {
			result.setResult(BufferScriptResult.ERROR_ARGUMENTS);
		}
	}
	
	public Object getParamValue(String param, List<Object> list) {
		String temp[] = param.split("\\{|\\}");
		if (temp.length == 1) {
			return param;
		}
		if (temp.length == 2) {
			if (temp[0].length() == 0) {
				try {
					return list.get(Integer.parseInt(temp[1]) - 1);
				} catch (Exception e) {
					return param;
				}
			}
		}
		
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < temp.length; i = i + 2) {
			sb.append(temp[i]);
			try {
				sb.append(list.get(Integer.parseInt(temp[i + 1]) - 1));
			} catch (Exception e) {
				return param;
			}
		}
		return sb.toString();
	}
	
	// po name,key,value
	// pm name,key,value
	// ps name,key,x,y,value
	// pt naem,key,parentKey,value
	// ro name,key
	// rm name,key
	// rm name,key,value
	// rs name,key
	// go name,key
	// gm name,key
	// gs name,key
	// gt name,key
	// rr name,key,name
	// rr name,key,name,key
	@Override
	public BufferScriptResult eval(String script, List<Object> list) {
		script = script.trim();
		
		BufferScriptResult result = new BufferScriptResult();
		String tmp[] = script.split(" |,", 3);
		if (tmp.length < 3) {
			result.setResult(BufferScriptResult.ERROR_ARGUMENTS);
			return result;
		}
		
		if (tmp[0].startsWith("p")) {
			
			Map<String, Object> map = new HashMap<String, Object>();
			
			for (String e : tmp[2].split(",")) {
				e = e.trim();
				String kv[] = e.split(":", 2);
				if (kv.length < 2) {
					result.setResult(BufferScriptResult.ERROR_ARGUMENT);
					return result;
				}
				try {
					map.put(kv[0], getParamValue(kv[1], list));
				} catch (Exception e2) {
					result.setResult(BufferScriptResult.ERROR_ARGUMENT);
					result.setData(e2.getMessage());
					return result;
				}
			}
			
		} else if (tmp[0].startsWith("rr")) {} else if (tmp[0].startsWith("g")) {
			
		}
		result.setResult(BufferScriptResult.OK);
		return result;
	}
	
	// po name,key,value
	// pm name,key,value
	// ps name,key,x,y,value
	// pt name,key,parentKey,value
	// ro name,key
	// rm name,key
	// rs name,key
	public BufferScriptResult eval(String script, Map<String, Object> map) {
		BufferScriptResult result = new BufferScriptResult();
		String tmp[] = script.split(" |,", 2);
		if (tmp.length < 2) {
			result.setResult(BufferScriptResult.ERROR_ARGUMENTS);
			return result;
		}
		
		if (tmp[0].startsWith("p")) {
			
		} else if (tmp[0].startsWith("r")) {} else if (tmp[0].startsWith("g")) {
			
		}
		result.setResult(BufferScriptResult.OK);
		return result;
	}
	
	public static void main(String[] args) {
		System.out.println("start");
		for (String e : "a{3}{34}c".split("\\{|\\}")) {
			System.out.println(e);
		}
		System.out.println("end");
	}
	
}
