package org.oiue.service.cache.script;

public class CacheScriptResult {
	public static String OK = "ok";
	public static String ERROR_COMMAND = "unknown command";
	public static String ERROR_BUFFER_TYPE = "buffer type";
	public static String ERROR_ARGUMENTS = "arguments count";
	public static String ERROR_ARGUMENT = "argument format";
	public static String ERROR_FORMAT = "value format";
	
	private String result;
	private Object data;
	
	public String getResult() {
		return result;
	}
	
	public void setResult(String result) {
		this.result = result;
	}
	
	public Object getData() {
		return data;
	}
	
	public void setData(Object data) {
		this.data = data;
	}
	
}
