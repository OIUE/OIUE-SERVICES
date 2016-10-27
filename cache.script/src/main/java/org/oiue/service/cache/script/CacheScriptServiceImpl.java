package org.oiue.service.cache.script;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.cache.Type;
import org.oiue.tools.map.MapUtil;

public class CacheScriptServiceImpl implements CacheScriptService {

	private CacheServiceManager buffer;

	public CacheScriptServiceImpl(CacheServiceManager buffer) {
		this.buffer = buffer;
	}

	@Override
	public CacheScriptResult eval(String script) {
		String line = script.toString().trim();
		CacheScriptResult result = new CacheScriptResult();
		result.setResult(CacheScriptResult.OK);
		result.setData("ok");

		if (line.startsWith("p")) {
			parsePut(line, result);
		} else if (line.startsWith("r")) {
			parseRemove(line, result);
		} else if (line.startsWith("g")) {
			try {
				parseGet(line, result);
			} catch (Exception e) {
				result.setResult(CacheScriptResult.ERROR_FORMAT);
				result.setData(e.getLocalizedMessage());
			}
		} else {
			result.setResult(CacheScriptResult.ERROR_COMMAND);
		}
		return result;
	}

	private void parsePut(String line, CacheScriptResult result) {
		// po cache,name,key,value
	    // poj cache,name,key,value
		// pm cache,name,key,value
	    // pmj cache,name,key,value
		// ps cache,name,key,x,y,value
        // pt cache,name,key,parentKey,value
		String cmdArray[] = line.split(" ", 2);
		if (cmdArray.length == 2) {
			String cmd = cmdArray[0];
			if (cmd.equalsIgnoreCase("po")) {
				// po name,key,value
				String tmp[] = cmdArray[1].split(",", 4);
				if (tmp.length == 4) {
					buffer.getCacheService(tmp[0]).put(tmp[1], tmp[2], (Object) tmp[3], Type.ONE);
				} else {
					result.setResult(CacheScriptResult.ERROR_ARGUMENTS);
				}
			} else if (cmd.equalsIgnoreCase("poj")) {
				// po name,key,value
				String tmp[] = cmdArray[1].split(",", 4);
				if (tmp.length == 4) {
					Object value=(Object) tmp[3];
					if(value instanceof String){
						value=MapUtil.fromString(value+"");
					}
					buffer.getCacheService(tmp[0]).put(tmp[1], tmp[2], value , Type.ONE);
				} else {
					result.setResult(CacheScriptResult.ERROR_ARGUMENTS);
				}
			} else if (cmd.equalsIgnoreCase("pm")) {
				// pm name,key,value
				String tmp[] = cmdArray[1].split(",", 4);
				if (tmp.length == 4) {
					buffer.getCacheService(tmp[0]).put(tmp[1], tmp[2], (Object) tmp[3], Type.MANY);
				} else {
					result.setResult(CacheScriptResult.ERROR_ARGUMENTS);
				}
			} else if (cmd.equalsIgnoreCase("pmj")) {
				// pm name,key,value
				String tmp[] = cmdArray[1].split(",", 4);
				if (tmp.length == 4) {
					Object value=(Object) tmp[3];
					if(value instanceof String){
						value=MapUtil.fromString(value+"");
					}
					buffer.getCacheService(tmp[0]).put(tmp[1], tmp[2], value, Type.MANY);
				} else {
					result.setResult(CacheScriptResult.ERROR_ARGUMENTS);
				}
			} else if (cmd.equalsIgnoreCase("ps")) {
				// ps name,key,x,y,value
				String tmp[] = cmdArray[1].split(",", 6);
				if (tmp.length == 6) {
					try {
					//	buffer.getCacheService(tmp[0]).put(tmp[0], tmp[1], Double.valueOf(tmp[2]), Double.valueOf(tmp[3]), tmp[4]);
					} catch (NumberFormatException ex) {
						result.setResult(CacheScriptResult.ERROR_ARGUMENT);
					}
				} else {
					result.setResult(CacheScriptResult.ERROR_ARGUMENTS);
				}
			} else if (cmd.equalsIgnoreCase("pt")) {
				// pt name,key,parentKey,value
				String tmp[] = cmdArray[1].split(",", 5);
				if (tmp.length == 5) {
					try {
						//buffer.put(tmp[0], (String) tmp[1], (String) tmp[2], (Object) tmp[3]);
					} catch (NumberFormatException ex) {
						result.setResult(CacheScriptResult.ERROR_ARGUMENT);
					}
				} else {
					result.setResult(CacheScriptResult.ERROR_ARGUMENTS);
				}
			} else {
				result.setResult(CacheScriptResult.ERROR_COMMAND);
			}
		} else {
			result.setResult(CacheScriptResult.ERROR_ARGUMENTS);
		}
	}

	private void parseRemove(String line, CacheScriptResult result) {
		// rc cache
		// rb cache,name
		// ro cache,name,key
		// rm cache,name,key
		// rm cache,name,key,value
		// rs cache,name,key
		// rr cache,oneToManyName,key,name
		// rr cache,oneToManyName,key,removeOneToManyName,reomveKey
		String cmdArray[] = line.split(" ", 2);
		if (cmdArray.length == 2) {
			String cmd = cmdArray[0];
			if (cmd.equalsIgnoreCase("rb")) {
                String tmp[] = cmdArray[1].split(",", 2);
				buffer.getCacheService(tmp[0]).delete(tmp[1]);
			} else if (cmd.equalsIgnoreCase("ro") || cmd.equalsIgnoreCase("rs") || cmd.equalsIgnoreCase("rm") || cmd.equalsIgnoreCase("rt")) {
				String tmp[] = cmdArray[1].split(",", 4);
				if (tmp.length == 3) {
					// ro name,key
					// rs name,key
					// rm name,key
					// rt name,key
					buffer.getCacheService(tmp[0]).delete(tmp[1], tmp[2]);
				} else if (cmd.equalsIgnoreCase("rm") && (tmp.length == 4)) {
					// rm name,key,value
					buffer.getCacheService(tmp[0]).delete(tmp[1], tmp[2], tmp[3]);
				} else {
					result.setResult(CacheScriptResult.ERROR_ARGUMENTS);
				}
			} else if (cmd.equalsIgnoreCase("rr")) {
				String tmp[] = cmdArray[1].split(",");
				if (tmp.length == 4) {
					// rr oneToManyName,key,name
//					buffer.removeRelation(tmp[0], tmp[1], tmp[2]);
				} else if (tmp.length == 5) {
					// rr oneToManyName,key,removeOneToManyName,reomveKey
//					buffer.removeRelation(tmp[0], tmp[1], tmp[2], tmp[3]);
				} else {
					result.setResult(CacheScriptResult.ERROR_ARGUMENTS);
				}
            } else if (cmd.equalsIgnoreCase("rc")) {
//                buffer.getCacheService(cmdArray[1]).
			} else {
				result.setResult(CacheScriptResult.ERROR_COMMAND);
			}
		} else {
			result.setResult(CacheScriptResult.ERROR_COMMAND);
		}
	}

	@SuppressWarnings("unused")
    private double[][] createSpaticalArgument(String x1, String x2, String y1, String y2) {
		String ax1[] = x1.split(";");
		String ax2[] = x2.split(";");
		String ay1[] = y1.split(";");
		String ay2[] = y2.split(";");
		if ((ax1.length != ax2.length) || (ax1.length != ay1.length) || (ax1.length != ay2.length)) {
			return null;
		}
		try {
			double args[][] = new double[4][];
			for (int i = 0; i < 4; i++) {
				args[i] = new double[ax1.length];
			}

			for (int j = 0; j < ax1.length; j++) {
				args[0][j] = Double.parseDouble(ax1[j]);
			}

			for (int j = 0; j < ax1.length; j++) {
				args[1][j] = Double.parseDouble(ax2[j]);
			}

			for (int j = 0; j < ax1.length; j++) {
				args[2][j] = Double.parseDouble(ay1[j]);
			}

			for (int j = 0; j < ax1.length; j++) {
				args[3][j] = Double.parseDouble(ay2[j]);
			}

			return args;
		} catch (Exception ex) {
			return null;
		}
	}

	private void parseGet(String line, CacheScriptResult result) throws JSONException {
		// gb cache,name
		// go cache,name,key
		// gm cache,name,key
		// gs cache,name,key
		// gs cache,name,x1,x2,y1,y2
		// gs cache,name,x1,x2,y1,y2,dx,dy
		// gr cache,oneToManyName,key,name
		// gr cache,oneToManyName,key,name,x1,x2,y1,y2
		// gr cache,oneToManyName,key,name,x1,x2,y1,y2,dx,dy
		String cmdArray[] = line.split(" ", 2);
		if (cmdArray.length == 2) {
			String cmd = cmdArray[0];
			if (cmd.equalsIgnoreCase("gb")) {
                String tmp[] = cmdArray[1].split(",", 2);
				result.setData(buffer.getCacheService(tmp[0]).get(tmp[1]));
			} else if (cmd.equalsIgnoreCase("go") || cmd.equalsIgnoreCase("gm") || cmd.equalsIgnoreCase("gt")) {
				String tmp[] = cmdArray[1].split(",");
				if (tmp.length == 3) {
					result.setData(buffer.getCacheService(tmp[0]).get(tmp[1], tmp[2]));
				} else {
					result.setResult(CacheScriptResult.ERROR_ARGUMENTS);
				}
			} else if (cmd.equalsIgnoreCase("gs")) {
//				// gs name,key
//				// gs name,x1,x2,y1,y2
//				// gs name,x1,x2,y1,y2,dx,dy
//				String tmp[] = cmdArray[1].split(",");
//				if (tmp.length == 2) {
//					result.setData(buffer.get(tmp[0], tmp[1]));
//				} else if (tmp.length == 5) {
//					double args[][] = createSpaticalArgument(tmp[1], tmp[2], tmp[3], tmp[4]);
//					if (args != null) {
//						if (args[0].length == 1) {
//							result.setData(buffer.get(tmp[0], args[0][0], args[1][0], args[2][0], args[3][0]));
//						} else {
//							result.setData(buffer.get(tmp[0], args[0], args[1], args[2], args[3]));
//						}
//					} else {
//						result.setResult(BufferScriptResult.ERROR_ARGUMENT);
//					}
//				} else if (tmp.length == 7) {
//					double args[][] = createSpaticalArgument(tmp[1], tmp[2], tmp[3], tmp[4]);
//					if (args != null) {
//						if (args[0].length == 1) {
//							result.setData(buffer.get(tmp[0], args[0][0], args[1][0], args[2][0], args[3][0], Double.parseDouble(tmp[5]), Double.parseDouble(tmp[6])));
//						} else {
//							result.setData(buffer.get(tmp[0], args[0], args[1], args[2], args[3], Double.parseDouble(tmp[5]), Double.parseDouble(tmp[6])));
//						}
//					} else {
//						result.setResult(BufferScriptResult.ERROR_ARGUMENT);
//					}
//
//				} else {
//					result.setResult(BufferScriptResult.ERROR_ARGUMENTS);
//				}
			} else if (cmd.equalsIgnoreCase("gr")) {
//				// gr oneToManyName,key,name
//				// gr oneToManyName,key,name,x1,x2,y1,y2
//				// gr oneToManyName,key,name,x1,x2,y1,y2,dx,dy
//				String tmp[] = cmdArray[1].split(",");
//				if (tmp.length == 3) {
//					result.setData(buffer.getRelation(tmp[0], tmp[1], tmp[2]));
//				} else if (tmp.length == 7) {
//					double args[][] = createSpaticalArgument(tmp[3], tmp[4], tmp[5], tmp[6]);
//					if (args != null) {
//						if (args[0].length == 1) {
//							result.setData(buffer.getRelation(tmp[0], tmp[1], tmp[2], args[0][0], args[1][0], args[2][0], args[3][0]));
//						} else {
//							result.setData(buffer.getRelation(tmp[0], tmp[1], tmp[2], args[0], args[1], args[2], args[3]));
//						}
//					} else {
//						result.setResult(BufferScriptResult.ERROR_ARGUMENT);
//					}
//				} else if (tmp.length == 9) {
//					double args[][] = createSpaticalArgument(tmp[3], tmp[4], tmp[5], tmp[6]);
//					if (args != null) {
//						if (args[0].length == 1) {
//							result.setData(buffer.getRelation(tmp[0], tmp[1], tmp[2], args[0][0], args[1][0], args[2][0], args[3][0], Double.parseDouble(tmp[7]), Double.parseDouble(tmp[8])));
//						} else {
//							result.setData(buffer.getRelation(tmp[0], tmp[1], tmp[2], args[0], args[1], args[2], args[3], Double.parseDouble(tmp[7]), Double.parseDouble(tmp[8])));
//						}
//					} else {
//						result.setResult(BufferScriptResult.ERROR_ARGUMENT);
//					}
//				} else {
//					result.setResult(BufferScriptResult.ERROR_ARGUMENTS);
//				}
			} else {
				result.setResult(CacheScriptResult.ERROR_COMMAND);
			}
		} else {
			result.setResult(CacheScriptResult.ERROR_ARGUMENTS);
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

	// po cache,name,key,value
	// pm cache,name,key,value
	// ps cache,name,key,x,y,value
	// pt cache,naem,key,parentKey,value
	// ro cache,name,key
	// rm cache,name,key
	// rm cache,name,key,value
	// rs cache,name,key
	// go cache,name,key
	// gm cache,name,key
	// gs cache,name,key
	// gt cache,name,key
	// rr cache,name,key,name
	// rr cache,name,key,name,key
	@Override
	public CacheScriptResult eval(String script, List<Object> list) {
		script = script.trim();

		CacheScriptResult result = new CacheScriptResult();
		String tmp[] = script.split(" |,", 4);
		if (tmp.length < 4) {
			result.setResult(CacheScriptResult.ERROR_ARGUMENTS);
			return result;
		}

		if (tmp[0].startsWith("p")) {
			Type type;
			switch (tmp[0].charAt(1)) {
			case 'o':
				type = Type.ONE;
				break;
			case 'm':
				type = Type.MANY;
				break;
			case 's':
				type = Type.SPATIAL;
				break;
			case 't':
				type = Type.TREE;
				break;
			default:
				result.setResult(CacheScriptResult.ERROR_ARGUMENT);
				return result;
			}

			Map<String, Object> map = new HashMap<String, Object>();

			for (String e : tmp[2].split(",")) {
				e = e.trim();
				String kv[] = e.split(":", 2);
				if (kv.length < 2) {
					result.setResult(CacheScriptResult.ERROR_ARGUMENT);
					return result;
				}
				try {
					map.put(kv[0], getParamValue(kv[1], list));
				} catch (Exception e2) {
					result.setResult(CacheScriptResult.ERROR_ARGUMENT);
					result.setData(e2.getMessage());
					return result;
				}
			}

			String valueKey = (tmp[0].length() >= 3 ? tmp[0].substring(2) : null);
			try {
				if (type == Type.SPATIAL) {
//					if (valueKey != null) {
//						buffer.put(tmp[1], MapUtil.getString(map, "k"), MapUtil.getDouble(map, "x"), MapUtil.getDouble(map, "y"), map.get(valueKey));
//					} else {
//						buffer.put(tmp[1], MapUtil.getString(map, "k"), MapUtil.getDouble(map, "x"), MapUtil.getDouble(map, "y"), map);
//					}
				} else if (type == Type.TREE) {
//					if (valueKey != null) {
//						buffer.put(tmp[1], MapUtil.getString(map, "k"), MapUtil.getString(map, "p"), map.get(valueKey));
//					} else {
//						buffer.put(tmp[1], MapUtil.getString(map, "k"), MapUtil.getString(map, "p"), map);
//					}
				} else {
					if (valueKey != null) {
						buffer.put(tmp[1], MapUtil.getString(map, "k"), map.get(valueKey), type);
					} else {
						buffer.put(tmp[1], MapUtil.getString(map, "k"), map, type);
					}
				}
			} catch (Exception ex) {
				result.setResult(CacheScriptResult.ERROR_ARGUMENT);
				result.setData(ex.getMessage());
				return result;
			}
		} else if (tmp[0].startsWith("rr")) {
//			String kn[] = tmp[2].split(",", 2);
//			if (kn.length == 2) {
//				String k;
//				String v;
//
//				String kv[] = kn[0].split(":", 2);
//				if (kv.length > 1) {
//					k = getParamValue(kv[1], list).toString();
//				} else {
//					k = getParamValue(kv[0], list).toString();
//				}
//
//				kv = kn[1].split(":", 2);
//				if (kv.length > 1) {
//					v = getParamValue(kv[1], list).toString();
//				} else {
//					v = getParamValue(kv[0], list).toString();
//				}
//				buffer.removeRelation(tmp[1], k, v);
//			} else if (kn.length == 3) {
//				String k;
//				String v;
//				String rk;
//
//				String kv[] = kn[0].split(":", 2);
//				if (kv.length > 1) {
//					k = getParamValue(kv[1], list).toString();
//				} else {
//					k = getParamValue(kv[0], list).toString();
//				}
//
//				kv = kn[1].split(":", 2);
//				if (kv.length > 1) {
//					v = getParamValue(kv[1], list).toString();
//				} else {
//					v = getParamValue(kv[0], list).toString();
//				}
//
//				kv = kn[2].split(":", 2);
//				if (kv.length > 1) {
//					rk = getParamValue(kv[1], list).toString();
//				} else {
//					rk = getParamValue(kv[0], list).toString();
//				}
//				buffer.removeRelation(tmp[1], k, v, rk);
//			} else {
//				result.setResult(BufferScriptResult.ERROR_ARGUMENT);
//				return result;
//			}
		} else if (tmp[0].startsWith("r")) {
//			String kn[] = tmp[2].split(",", 2);
//			if (kn.length == 1) {
//				String kv[] = tmp[2].split(":", 2);
//				if (kv.length > 1) {
//					buffer.remove(tmp[1], getParamValue(kv[1], list).toString());
//				} else {
//					buffer.remove(tmp[1], getParamValue(tmp[2], list).toString());
//				}
//			} else {
//				String kv[] = kn[0].split(":", 2);
//				String k;
//				String v;
//				if (kv.length > 1) {
//					k = getParamValue(kv[1], list).toString();
//				} else {
//					k = getParamValue(kv[0], list).toString();
//				}
//
//				kv = kn[1].split(":", 2);
//				if (kv.length > 1) {
//					v = getParamValue(kv[1], list).toString();
//				} else {
//					v = getParamValue(kv[0], list).toString();
//				}
//				buffer.remove(tmp[1], k, v);
//			}
		} else if (tmp[0].startsWith("g")) {

		}
		result.setResult(CacheScriptResult.OK);
		return result;
	}

	// po name,key,value
	// pm name,key,value
	// ps name,key,x,y,value
	// pt name,key,parentKey,value
	// ro name,key
	// rm name,key
	// rs name,key
	public CacheScriptResult eval(String script, Map<String, Object> map) {
		CacheScriptResult result = new CacheScriptResult();
		String tmp[] = script.split(" |,", 2);
		if (tmp.length < 2) {
			result.setResult(CacheScriptResult.ERROR_ARGUMENTS);
			return result;
		}

		if (tmp[0].startsWith("p")) {
			Type type;
			switch (tmp[0].charAt(1)) {
			case 'o':
				type = Type.ONE;
				break;
			case 'm':
				type = Type.MANY;
				break;
			case 's':
				type = Type.SPATIAL;
				break;
			case 't':
				type = Type.TREE;
				break;
			default:
				result.setResult(CacheScriptResult.ERROR_ARGUMENT);
				return result;
			}

			String valueKey = (tmp[0].length() >= 3 ? tmp[0].substring(2) : null);

			try {
				if (type == Type.SPATIAL) {
//					if (valueKey != null) {
//						buffer.put(tmp[1], MapUtil.getString(map, "k"), MapUtil.getDouble(map, "x"), MapUtil.getDouble(map, "y"), map.get(valueKey));
//					} else {
//						buffer.put(tmp[1], MapUtil.getString(map, "k"), MapUtil.getDouble(map, "x"), MapUtil.getDouble(map, "y"), map);
//					}
				} else if (type == Type.TREE) {
//					if (valueKey != null) {
//						buffer.put(tmp[1], MapUtil.getString(map, "k"), MapUtil.getString(map, "p"), map.get(valueKey));
//					} else {
//						buffer.put(tmp[1], MapUtil.getString(map, "k"), MapUtil.getString(map, "p"), map);
//					}
				} else {
					if (valueKey != null) {
						buffer.put(tmp[1], MapUtil.getString(map, "k"), map.get(valueKey), type);
					} else {
						buffer.put(tmp[1], MapUtil.getString(map, "k"), map, type);
					}
				}
			} catch (Exception ex) {
				result.setResult(CacheScriptResult.ERROR_ARGUMENT);
				result.setData(ex.getMessage());
				return result;
			}
		} else if (tmp[0].startsWith("r")) {
//			String valueKey = (tmp[0].length() >= 3 ? tmp[0].substring(2) : null);
//			if (valueKey == null) {
//				buffer.remove(tmp[1], MapUtil.getString(map, "k"));
//			} else {
//				buffer.remove(tmp[1], MapUtil.getString(map, "k"), MapUtil.getString(map, valueKey));
//			}
		} else if (tmp[0].startsWith("g")) {

		}
		result.setResult(CacheScriptResult.OK);
		return result;
	}

//	public static void main(String[] args) {
//		System.out.println("start");
//		for (String e : "a{3}{34}c".split("\\{|\\}")) {
//			System.out.println(e);
//		}
//		System.out.println("end");
//	}

}
