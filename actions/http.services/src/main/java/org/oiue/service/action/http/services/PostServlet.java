package org.oiue.service.action.http.services;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.http.HttpStatus;
import org.oiue.service.action.api.ActionService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.ExceptionUtil;
import org.oiue.tools.exception.OIUEException;
import org.oiue.tools.json.JSONUtil;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

@SuppressWarnings({ "rawtypes" })
public class PostServlet extends HttpServlet {
	private static final long serialVersionUID = -6327347468651806863L;
	private static final ExecutorService service = Executors.newCachedThreadPool();
	private ActionService actionService;
	private Logger logger;
	private Map properties;
	private boolean refererGrant = false;
	private static long timeout = 5000;

	public PostServlet(ActionService actionService, LogService logService) {
		super();
		this.logger = logService.getLogger(this.getClass());
		this.actionService = actionService;
	}

	@SuppressWarnings("unchecked")
	public void updated(Map props) {
		logger.info("updateConfigure");
		properties = props;
		try {
			timeout = MapUtil.getLong(props, "timeout", timeout);
		} catch (Throwable e) {
		}
		String c_referer = properties.get("Referer") + "";
		if (!StringUtil.isEmptys(c_referer))
			refererGrant = true;
	}

	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setHeader( "Access-Control-Allow-Origin","*" ); 
		resp.setHeader( "Access-Control-Allow-Headers", "Origin, No-Cache, X-Requested-With, If-Modified-Since, Pragma, Last-Modified, Cache-Control, Expires, Content-Type"); 
		resp.setHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, OPTIONS, DELETE");
		resp.setStatus(HttpStatus.NO_CONTENT_204);
	}

	static int count = 0;

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		logger.debug("request:{}", ++count);
		Map per = null;
		Map<Object, Object> rtn = null;
		String callBackFn = "";
		boolean authInHeader = false;
		boolean download = false;
		PrintWriter writer = null;
		OutputStream stream = null;
		String token = null;

		FutureTask<Map<Object, Object>> future = null;
		TaskThread t = null;
		try {
			String c_referer = properties.get("Referer") + "";
			if (refererGrant) {
				// 从 HTTP 头中取得 Referer 值
				String referer = req.getHeader("Referer");
				// 判断 Referer 是否以配置 开头
				if ((referer == null) || !(referer.trim().startsWith(c_referer))) {
					throw new OIUEException(StatusResult._url_can_not_found, "发起地址：" + referer + ",站点地址：" + c_referer);
				}
			}

			per = ParseHtml.parseRequest(req);

			token = req.getHeader("Authorization");
			if (!StringUtil.isEmptys(token)) {
				authInHeader = true;
			}

			String path = req.getPathInfo();
			String[] paths = path.split("/");
			if (paths.length < 3)
				throw new OIUEException(StatusResult._url_can_not_found,
						"请求地址格式不正确（/version/modulename/operation）！" + " /n " + path);

			per.put("version", paths[1]);
			per.put("modulename", paths[2]);
			per.put("operation", paths[3]);

			try {
				per.put("client_ip", getIpAddr(req));
			} catch (Exception e) {
				logger.error("获取客户端IP异常：" + e.getMessage(), e);
			}
			try {
				per.put("domain", req.getServerName());
			} catch (Exception e) {
				logger.error("获取访问域名异常：" + e.getMessage(), e);
			}
			try {
				if ("login".equals(MapUtil.getString(per, "modulename"))) {
					HttpSession session = req.getSession(true);
					per.put("Login_Image_Code", session.getAttribute("Login_Image_Code"));
				}
			} catch (Throwable e) {
				logger.error("处理登录模块验证码异常：" + e.getMessage(), e);
			}
			try {
				callBackFn = per.get("callback") + "";
			} catch (Throwable e) {
				logger.error("处理Jsonp回调异常：" + e.getMessage(), e);
			}
			// long header = req.getDateHeader("If-Modified-Since");
			// String ETags = req.getHeader("If-None-Match");
			String export = MapUtil.getString(per, "export", "json");
			if ("csv".equals(export)) {
				resp.setCharacterEncoding("UTF-8");
				resp.setContentType("application/csv;charset=UTF-8");
				String fileName = MapUtil.getString(per, "data.exportFileName","export.csv");
				resp.setHeader("Content-Disposition", "inline; filename="+URLEncoder.encode(fileName,"UTF-8"));
				// writer = resp.getWriter();
				// per.put("PrintWriter", writer);
				stream = resp.getOutputStream();
				per.put("OutputStream", stream);
				download = true;
				rtn = actionService.request(per);
			} else if ("force".equals(export)) {
				rtn = actionService.request(per);
			} else {
				t = new TaskThread(per);
				future = new FutureTask<>(t);
				service.execute(future);
				rtn = future.get(timeout, TimeUnit.MILLISECONDS); // 取得结果，同时设置超时执行时间
				logger.debug("rtn >>>{}", rtn);
				future.cancel(true);
				logger.debug("rtn >>>>{}", rtn);
			}
		} catch (TimeoutException ex) {
			rtn = per;
			try {
				if (!future.isCancelled())
					future.cancel(true);
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
//			try {
//				if (!future.isCancelled())
//					t.forceStop();
//			} catch (Throwable e) {
//				logger.error(e.getMessage(), e);
//			}
//			logger.error("rtn:" + rtn, ex);
			rtn.put("status", StatusResult._time_out);
			rtn.put("excepion", "服务端响应超时，请稍后重试！");
			rtn.put("msg", "服务端响应超时，请稍后重试！");
//			if (t != null)
//				t.forceStop();
		} catch (Throwable ex) {
			logger.error(ExceptionUtil.getCausedBySrcMsg(ex), ex);
			rtn = per;
			rtn.put("status", StatusResult._permissionDenied);
			rtn.put("excepion", ExceptionUtil.getCausedBySrcMsg(ex));
		} finally {
			// try {
			// if (!future.isCancelled())
			// t.forceStop();
			// } catch (Throwable e) {
			// logger.error(e.getMessage(), e);
			// }
			// try {
			// if (!future.isCancelled())
			// future.cancel(true);
			// } catch (Throwable e) {
			// logger.error(e.getMessage(), e);
			// }
			if (writer != null) {
				writer.close();
				return;
			}
			if (stream != null) {
				stream.close();
				return;
			}
		}
//		logger.debug("rtn >>>>>{}", rtn);

		if (!download)
			try {
//				logger.debug("rtn >>>>>>{}", rtn);
				OutputStream out = resp.getOutputStream();

				resp.setContentType("application/json");
				resp.setHeader("Access-Control-Allow-Origin", "*");
				resp.setHeader("Access-Control-Allow-Methods", "PUT, GET, POST, DELETE, OPTIONS");
				resp.setCharacterEncoding("UTF-8");
				if (authInHeader)
					resp.setHeader("Authorization", rtn.remove("token") + "");
				byte bytes[] = (StringUtil.isEmptys(callBackFn) ? JSONUtil.parserToStr(rtn)
						: (callBackFn + "(" + JSONUtil.parserToStr(rtn) + ")")).getBytes();
				resp.setContentLength(bytes.length);
				try {
					out.write(bytes);
					out.flush();
				} finally {
					if (out != null)
						out.close();
				}
			} catch (Throwable e) {
				logger.error("返回数据写入异常：" + e.getMessage(), e);
			}
		logger.debug("no response:{}", --count);
//		logger.debug("rtn >>>>>>>{}", rtn);
//		if (t != null)
//			t.forceStop();
//		logger.debug("rtn >>>>>>>>{}", rtn);
	}

	class TaskThread implements Callable<Map<Object, Object>> {
		Map per = null;
		Thread t;

		public TaskThread(Map per) {
			this.per = per;
		}

		@SuppressWarnings("unchecked")
		public Map<Object, Object> call() {
			t = Thread.currentThread();
			logger.debug("THread Name:{}", t.getName());
			Map<Object, Object> rtn = null;
			try {
				logger.debug("start per:{}", per);
				rtn = actionService.request(per);
				logger.debug("end rtn:{}", rtn);
			} catch (ThreadDeath e) {
				rtn = new HashMap();
				rtn.put("status", StatusResult._permissionDenied);
				rtn.put("excepion", ExceptionUtil.getCausedBySrcMsg(e));
			} catch (Throwable ex) {
				logger.error(ExceptionUtil.getCausedBySrcMsg(ex), ex);
				rtn = new HashMap();
				rtn.put("status", StatusResult._permissionDenied);
				rtn.put("excepion", ExceptionUtil.getCausedBySrcMsg(ex));
			}
			logger.debug("rtn:{}", rtn);
			return rtn;
		}

		@SuppressWarnings("deprecation")
		public void forceStop() {
			if (t != null)
				try {
					t.interrupt();
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
			if (t != null)
				try {
					t.stop();
				} catch (ThreadDeath e) {
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
			t=null;

		}
	}

	public boolean setRespHeaderCache(long adddays, HttpServletRequest request, HttpServletResponse response) {
		request.setAttribute("myExpire", adddays);

		long adddaysM = adddays * 1000;
		String maxAgeDirective = "max-age=" + adddays;
		response.setHeader("Cache-Control", maxAgeDirective);
		response.setStatus(HttpServletResponse.SC_OK);
		response.addDateHeader("Last-Modified", System.currentTimeMillis());
		response.addDateHeader("Expires", System.currentTimeMillis() + adddaysM);
		return true;
	}

	public String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		String[] ips = ip.split(",");
		if (ips.length > 0)
			ip = ips[0];
		return ip;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doPost(req, resp);
	}
}
