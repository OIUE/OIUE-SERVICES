package org.oiue.service.action.http.services;

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

    @SuppressWarnings("unchecked")
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map per = null;
        Map<Object, Object> rtn = null;
        String perStr = null;
        String callBackFn = "";
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
            // StringBuffer jb = new StringBuffer();
            // String line = null;
            // try {
            // BufferedReader reader = req.getReader();
            // while ((line = reader.readLine()) != null)
            // jb.append(line);
            // } catch (Exception e) {
            // }
            // System.out.println(jb);
            try {
                perStr = req.getParameter("parameter");
                if (!StringUtil.isEmptys(perStr)) {
                    per = (Map) JSONUtil.parserStrToMap(perStr);
                }
                if (per == null || per.size() == 0) {
                    per = ParseHtml.parseRequest(req);
                }
            } catch (Throwable e) {
                throw new RuntimeException("参数格式不正确！" + " /n " + perStr + " /n " + per, e);
            }
            String path = req.getPathInfo();
            String[] paths = path.split("/");

            per.put("version", paths[1]);
            per.put("modulename", paths[2]);
            per.put("operation", paths[3]);
            if (logger.isDebugEnabled())
                logger.debug("per:" + per);
            try {
                per.put("client_ip", getIpAddr(req));
                if ("login".equals(MapUtil.getString(per, "modulename"))) {

                    HttpSession session = req.getSession(true);
                    per.put("Login_Image_Code", session.getAttribute("Login_Image_Code"));
                }
                // per.put("client_OutputStream", resp.getOutputStream());
            } catch (Throwable e) {
                e.printStackTrace();
            }
            try {
                callBackFn = per.get("callback") + "";
            } catch (Throwable e) {

            }
            long start = 0;
            if (logger.isDebugEnabled()) {
                start = System.currentTimeMillis();
            }
            rtn = actionService.request(per);
            if (logger.isDebugEnabled()) {
                long end = System.currentTimeMillis();
                logger.debug("action response time:" + (end - start));
            }
            // resp.getWriter().write(JSONObject.fromObject(rtn).toString());
        } catch (Throwable ex) {
            logger.error("", ex);
            rtn = new HashMap();
            rtn.put("status", StatusResult._permissionDenied);
            rtn.put("excepion", ExceptionUtil.getCausedBySrcMsg(ex));
        }
        try {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");


            byte bytes[] = (StringUtil.isEmptys(callBackFn) ? JSONUtil.parserToStr(rtn) : (callBackFn + "(" + JSONUtil.parserToStr(rtn) + ")")).getBytes();
            resp.setContentLength(bytes.length);

            OutputStream out = resp.getOutputStream();
            out.write(bytes);

            // PrintWriter out = resp.getWriter();
            out.flush();
            out.close();
        } catch (Throwable e) {
            logger.error("" + e.getMessage(), e);
        }
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
