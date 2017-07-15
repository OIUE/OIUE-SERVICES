package org.oiue.service.action.http.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.oiue.service.action.api.ActionService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.ExceptionUtil;
import org.oiue.tools.json.JSONUtil;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

@SuppressWarnings({ "rawtypes" })
public class PostServlet extends HttpServlet {
	private static final long serialVersionUID = -6327347468651806863L;
	private ActionService actionService;
	private Logger logger;
	private Dictionary properties;
	private boolean refererGrant = false;

	public PostServlet(ActionService actionService, LogService logService) {
		super();
		this.logger = logService.getLogger(this.getClass());
		this.actionService = actionService;
	}

	public void updated(Dictionary props) {
		logger.info("updateConfigure");
		properties = props;
		String c_referer = properties.get("Referer") + "";
		if (!StringUtil.isEmptys(c_referer))
			refererGrant = true;
	}

	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Map per = null;
		Map<Object, Object> rtn = null;
		String perStr = null;
		String callBackFn = "";
		boolean authInHeader = false;
		try {
			try {
				String c_referer = properties.get("Referer") + "";
				if (refererGrant) {
					// 从 HTTP 头中取得 Referer 值
					String referer = req.getHeader("Referer");
					// 判断 Referer 是否以配置 开头
					if ((referer == null) || !(referer.trim().startsWith(c_referer))) {
						throw new RuntimeException("发起地址：" + referer + ",站点地址：" + c_referer);
					}
				}
			} catch (Throwable e) {
				throw new RuntimeException("非法的请求源地址！" + e.getMessage(), e);
			}
			try {
				perStr = req.getParameter("parameter");
				try {
					if (StringUtil.isEmptys(perStr)) {
						StringBuffer jb = new StringBuffer();
						String line = null;
						BufferedReader reader = req.getReader();
						while ((line = reader.readLine()) != null)
							jb.append(line);
						perStr = jb.toString();
					}
				} catch (Exception e) {
					//					logger.error(e.getMessage(), e);
				}
				if (!StringUtil.isEmptys(perStr)) {
					per = JSONUtil.parserStrToMap(perStr);
				}
				if (per == null || per.size() == 0) {
					Map data =  ParseHtml.parseRequest(req);
					if(data!=null&&data.size()>0){
						per = new HashMap<>();
						per.put("token", data.remove("token"));
						per.put("data", data);
					}
				}
				if(per==null){
					per = new HashMap<>();
					per.put("data", new HashMap<>());
				}
				String token = req.getHeader("Authorization");
				if(!StringUtil.isEmptys(token)){
					int index = token.indexOf(" ");
					if(index>0)
						token = token.substring(index+1);
					authInHeader = true;
					per.put("token", token);
				}
			} catch (Throwable e) {
				throw new RuntimeException("参数格式不正确！" + " /n " + perStr + " /n " + per, e);
			}

			String path = req.getPathInfo();
			String[] paths = path.split("/");
			if(paths.length<3)
				throw new RuntimeException("请求地址格式不正确（/version/modulename/operation）！" + " /n " + path );

			per.put("version", paths[1]);
			per.put("modulename", paths[2]);
			per.put("operation", paths[3]);

			try {
				per.put("client_ip", getIpAddr(req));
			} catch (Exception e) {
				logger.error("获取客户端IP异常："+e.getMessage(), e);
			}
			try {
				per.put("domain", req.getServerName());
			} catch (Exception e) {
				logger.error("获取访问域名异常："+e.getMessage(), e);
			}
			try {
				if ("login".equals(MapUtil.getString(per, "modulename"))) {
					HttpSession session = req.getSession(true);
					per.put("Login_Image_Code", session.getAttribute("Login_Image_Code"));
				}
			} catch (Throwable e) {
				logger.error("处理登录模块验证码异常："+e.getMessage(), e);
			}
			try {
				callBackFn = per.get("callback") + "";
			} catch (Throwable e) {
				logger.error("处理Jsonp回调异常："+e.getMessage(), e);
			}
			long header = req.getDateHeader("If-Modified-Since");
			String previousToken = req.getHeader("If-None-Match");

			rtn = actionService.request(per);
		} catch (Throwable ex) {
			logger.error(ExceptionUtil.getCausedBySrcMsg(ex), ex);
			rtn = new HashMap();
			rtn.put("status", StatusResult._permissionDenied);
			rtn.put("excepion", ExceptionUtil.getCausedBySrcMsg(ex));
		}
		try {
			resp.setContentType("application/json");
			resp.setHeader("Access-Control-Allow-Origin", "*");
			resp.setHeader("Access-Control-Allow-Methods", "PUT, GET, POST, DELETE, OPTIONS");
			if(authInHeader)
				resp.setHeader("Authorization", rtn.remove("token")+"");

			resp.setCharacterEncoding("UTF-8");

			byte bytes[] = (StringUtil.isEmptys(callBackFn) ? JSONUtil.parserToStr(rtn) : (callBackFn + "(" + JSONUtil.parserToStr(rtn) + ")")).getBytes();
			resp.setContentLength(bytes.length);

			OutputStream out = resp.getOutputStream();
			out.write(bytes);

			out.flush();
			out.close();
		} catch (Throwable e) {
			logger.error("返回数据写入异常：" + e.getMessage(), e);
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
