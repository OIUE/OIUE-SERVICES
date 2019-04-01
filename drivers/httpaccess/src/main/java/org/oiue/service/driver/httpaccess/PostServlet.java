package org.oiue.service.driver.httpaccess;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.oiue.service.driver.api.Driver;
import org.oiue.service.driver.api.DriverDataField;
import org.oiue.service.driver.api.DriverListener;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.Application;
import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.ExceptionUtil;
import org.oiue.tools.exception.OIUEException;
import org.oiue.tools.json.JSONUtil;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.serializ.SerializObject;
import org.oiue.tools.string.StringUtil;

@SuppressWarnings({ "rawtypes" })
public class PostServlet extends HttpServlet implements Driver {
	private static final long serialVersionUID = -6327347468651806863L;
	private Logger logger;
	public static final String DriverName = "HttpAccessDriver";
	
	private LogService logService = null;
	private static LinkedBlockingQueue<Map> queue = new LinkedBlockingQueue<>();
	private static receiveData rd = null;
	
	public PostServlet(LogService logService) {
		super();
		this.logService = logService;
		this.logger = logService.getLogger(this.getClass());
	}
	
	public void updated(Map props) {
		logger.info("updateConfigure");
	}
	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setHeader( "Access-Control-Allow-Origin","*" ); 
		resp.setHeader( "Access-Control-Allow-Headers", "Origin, No-Cache, X-Requested-With, If-Modified-Since, Pragma, Last-Modified, Cache-Control, Expires, Content-Type"); 
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			Map per = new HashMap<>();
			{
				String perStr = null;
				StringBuffer jb = new StringBuffer();
				String line = null;
				BufferedReader reader = req.getReader();
				while ((line = reader.readLine()) != null)
					jb.append(line);
				perStr = jb.toString();
				
				if (!StringUtil.isEmptys(perStr)) {
					per = JSONUtil.parserStrToMap(perStr, false);
				}
			}
			String path = req.getPathInfo();
			String[] paths = path.split("/");
			if (paths.length < 3)
				throw new OIUEException(StatusResult._url_can_not_found,
						"请求地址格式不正确（/version/DriverName/Type）！" + " /n " + path);

			per.put("version", paths[1]);
			String DriverName = paths[2];
			if(StringUtil.isEmptys(DriverName))
				DriverName=PostServlet.DriverName;
			String DriverType = paths[3];
			if(StringUtil.isEmptys(DriverType))
				DriverType="track";
			per.put(DriverDataField.driverName,MapUtil.getString(per, DriverDataField.driverName,DriverName));
			per.put(DriverDataField.type,MapUtil.getString(per, DriverDataField.type,DriverType));
			queue.offer(per);
			OutputStream out = resp.getOutputStream();

			try {
				resp.setContentType("application/json");
				resp.setHeader("Access-Control-Allow-Origin", "*");
				resp.setHeader("Access-Control-Allow-Methods", "PUT, GET, POST, DELETE, OPTIONS");
				resp.setCharacterEncoding("UTF-8");
				byte bytes[] =  ("{\"status\":1}").getBytes();
				resp.setContentLength(bytes.length);
				out.write(bytes);
				out.flush();
			} finally {
				if (out != null)
					out.close();
			}
		} catch (Throwable ex) {
			logger.error(ExceptionUtil.getCausedBySrcMsg(ex), ex);
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}
	
	@Override
	public void registered(DriverListener listener) {
		if (rd == null) {
			rd = new receiveData(listener, this.logService);
		}
		synchronized (rd) {
			new Thread(rd).start();
		}
	}
	
	@Override
	public void unregistered() {
		rd.stop();
	}
	
	@Override
	public StatusResult send(Map sendData) {
		return null;
	}
	
	static class receiveData implements Runnable {
		private DriverListener listener = null;
		private Logger logger;
		
		public receiveData(DriverListener listener, LogService logService) {
			this.listener = listener;
			logger = logService.getLogger(receiveData.class);
		}
		
		private boolean isRunning = true;
		
		public void stop() {
			logger.error("队列中还有" + queue.size() + "条数数据未处理！");
			this.isRunning = false;
		}
		
		@Override
		public void run() {
			while (this.isRunning) {
				try {
					if (!PostServlet.queue.isEmpty()) {
						long startUtc = System.currentTimeMillis();
						Map driverData = PostServlet.queue.take();
						if (driverData != null) {
							this.listener.receive(driverData);
						}
						if (logger.isDebugEnabled()) {
							long endUtc = System.currentTimeMillis();
							logger.debug("发送给driverService处理报文共耗时:" + (endUtc - startUtc) + "毫秒|" + PostServlet.queue.size());
						}
					} else {
						try {
							Thread.sleep(5);
						} catch (InterruptedException e) {
							this.logger.error(e.getMessage(), e);
						}
					}
					if (queue.size() > 10000) {
						logger.warn("send data queue is stoppage ：" + queue.size());
						LinkedBlockingQueue<Map> queue_tmp = queue;
						queue = null;
						queue = new LinkedBlockingQueue<>();
						try {
							SerializObject.serializObj2File(queue_tmp, Application.getRootPath() + File.separator + "JTTaxi_new_sendData" + System.currentTimeMillis() + ".dat");
						} catch (Throwable e1) {
							logger.info(" serializObj2File is error " + e1.getMessage(), e1);
						}
						queue_tmp.clear();
					}
				} catch (Throwable t) {
					this.logger.error("throws error!", t);
				}
				
			}
		}
	}
}
