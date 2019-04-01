package org.oiue.service.debug.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oiue.service.cache.script.CacheScriptResult;
import org.oiue.service.cache.script.CacheScriptService;
import org.oiue.service.io.Handler;
import org.oiue.service.io.Session;
import org.oiue.tools.list.ListUtil;
import org.oiue.tools.map.MapUtil;

public class ServerHandler implements Handler {
	private CacheScriptService cacheScript = null;
	
	public ServerHandler(CacheScriptService cacheScript) {
		this.cacheScript = cacheScript;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void received(Session session, String line, byte[] bytes) {
		System.out.println("line :"+line);
		if ("help".equals(line)) {
			session.write("po cache,name,key,value");
			session.write("poj cache,name,key,value");
			session.write("pm cache,name,key,value");
			session.write("pmj cache,name,key,value");
			session.write("ps cache,name,key,x,y,value");
			session.write("pt cache,name,key,parentKey,value");
			
			session.write("rc cache");
			session.write("rb cache,name");
			session.write("ro cache,name,key");
			session.write("rm cache,name,key");
			session.write("rm cache,name,key,value");
			session.write("rs cache,name,key");
			session.write("rr cache,oneToManyName,key,name");
			session.write("rr cache,oneToManyName,key,removeOneToManyName,reomveKey");
			
			session.write("gb cache,name");
			session.write("go cache,name,key");
			session.write("gm cache,name,key");
			session.write("gs cache,name,key");
			session.write("gs cache,name,x1,x2,y1,y2");
			session.write("gs cache,name,x1,x2,y1,y2,dx,dy");
			session.write("gr cache,oneToManyName,key,name");
			session.write("gr cache,oneToManyName,key,name,x1,x2,y1,y2");
			session.write("gr cache,oneToManyName,key,name,x1,x2,y1,y2,dx,dy");
			
		} else if (line.startsWith("o")) {
			session.write("o");
			session.close();
		} else if (line.startsWith("k")) {
			session.write("k");
		} else if (line.startsWith("w")) {
			session.write("w");
			String[] s = line.split(" ");
			if(s.length==3) {
				try{
					System.out.println("System.getProperty(“file.encoding”):"+System.getProperty("file.encoding"));
					System.out.println("System.getProperty(“sun.jnu.encoding”):"+System.getProperty("sun.jnu.encoding"));
//					System.setProperty("sun.jnu.encoding","UTF-8");
					File file = new File(s[1]);
					java.io.FileOutputStream fout = new FileOutputStream(file);
					fout.write(s[2].getBytes());
					fout.close();
					session.write(file.getAbsolutePath());
					File tf = new File(file.getAbsolutePath());
					File f = new File(tf.getParent());
					File[] listFiles = f.listFiles();
					
					for (File file2 : listFiles) {
						session.write(file2.getName());
					}
				}catch(Throwable e){
					e.printStackTrace();
				}
			}else if(s.length==2) {
				File file = new File(s[1]);
				session.write(file.canRead()+"");
				
			}
			
		} else {
			CacheScriptResult result = cacheScript.eval(line);
			if (result.getResult() != CacheScriptResult.OK) {
				session.write(result.getResult());
			} else {
				Object data = result.getData();
				if (data instanceof List) {
					session.write(ListUtil.toString((List) data));
				} else if (data instanceof Map) {
					session.write(MapUtil.toString((Map) data));
				} else {
					session.write(data + "");
				}
			}
		}
	}
	
	@Override
	public void closed(Session session) {
		
	}
	
	@Override
	public void opened(Session session) {
		
	}
	
	@Override
	public void idled(Session session) {
		session.close();
	}
	
	@Override
	public void sent(Session session) {
		
	}
	
	@Override
	public int getReaderIdleCount() {
		return 0;
	}
}
