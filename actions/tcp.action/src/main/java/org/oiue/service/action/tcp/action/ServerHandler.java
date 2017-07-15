package org.oiue.service.action.tcp.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.oiue.service.action.api.ActionService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.online.Online;
import org.oiue.service.online.OnlineDataField;
import org.oiue.service.online.OnlineService;
import org.oiue.service.online.Type;
import org.oiue.service.tcp.Handler;
import org.oiue.service.tcp.Session;
import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.ExceptionUtil;
import org.oiue.tools.json.JSONUtil;
import org.oiue.tools.string.StringUtil;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ServerHandler implements Handler {
	private ActionService actionService;
	private Logger logger;
	private OnlineService onlineService;

	public ServerHandler(LogService logService, ActionService actionService, OnlineService onlineService) {
		this.actionService = actionService;
		this.onlineService = onlineService;
		this.logger = logService.getLogger(this.getClass());
	}

	@Override
	public void received(Session session, String line, byte[] bytes) throws Exception {
		Map<Object, Object> per = null;
		Map<Object, Object> rtn = null;
		try {
			if (line == null||StringUtil.isEmptys(line))
				return;
			if (null == session.getAttribute("binary"))
				session.setAttribute("binary", false);
			try {
				// if (line.getBytes()[0] == -17) {
				// StringBuffer sb = new StringBuffer(line);
				// line = sb.deleteCharAt(0).toString();
				// }
				if (!StringUtil.isEmptys(line))
					per = (Map<Object, Object>) JSONUtil.parserStrToMap(line);

				String tag = per.get("tag") + "";
				Object stag = session.getAttribute("tag");
				if (stag != null && !tag.equals(stag)) {
					throw new RuntimeException("session tag can not change!");
				}
			} catch (Throwable e) {
				throw new RuntimeException("参数格式不正确！[" + ExceptionUtil.getCausedBySrcMsg(e) + "]" + " /n " + line + " /n " + per, e);
			}
			new Thread(new asynchronismction(session, per),asynchronismction.class.getName()).start();
			// session.write("{\"modulename\":\"systime\",\"tag\":\"exttag\",\"operation\":\"systime\",\"data\":{}}");
		} catch (Throwable ex) {
			logger.error("received error：" + ExceptionUtil.getCausedBySrcMsg(ex), ex);
			rtn = new HashMap();
			rtn.put("status", StatusResult._permissionDenied);
			rtn.put("excepion", ExceptionUtil.getCausedBySrcMsg(ex));
			session.write(JSONUtil.parserToStr(rtn));
		}
	}

	class asynchronismction implements Runnable {

		Map<Object, Object> rtn = null;
		Map<Object, Object> per = null;
		Session session = null;

		public asynchronismction(Session session, Map<Object, Object> per) {
			this.session = session;
			this.per = per;
		}

		@Override
		public void run() {
			try {
				long startTime = 0l;
				if (logger.isDebugEnabled()) {
					startTime = System.currentTimeMillis();
					logger.debug(Thread.currentThread().getName() + "| per:" + per);
				}
				per.put("client_type", Type.tcp);

				rtn = actionService.request(per);

				// /**
				// * 为适配调度端对状态的特定处理
				// */
				// if(Integer.valueOf(rtn.get("status")+"")>1){
				// rtn.put("status", 1);
				// }

				if (logger.isDebugEnabled()) {
					logger.debug(Thread.currentThread().getName() + "," + (System.currentTimeMillis() - startTime) + "|per:" + per + ", rtn:" + rtn);
				}
				if (StringUtil.isEmptys(session.getAttribute("tokenid") + "")) {
					String token = rtn.get("token") + "";
					if (!StringUtil.isEmptys(token)) {
						Online online = onlineService.getOnlineByToken(token);
						if (online != null) {
							Object o = online.getO();
							Map po = null;
							if (o instanceof Map) {
								po = (Map) o;
							} else {
								po = new ConcurrentHashMap<>();
								online.setO(po);
							}
							List<Session> list = (List<Session>) po.get(OnlineDataField._online_cs_session);
							if (list == null) {
								list = new ArrayList<>();
								po.put(OnlineDataField._online_cs_session, list);
							}
							session.setAttribute("tokenid", online.getTokenId());
							session.setAttribute("tag", per.get("tag"));
							list.add(session);
							online.setStatus(Type.tcp);
						}
					}
				}
			} catch (Throwable ex) {
				logger.error("received error：" + ExceptionUtil.getCausedBySrcMsg(ex), ex);
				rtn = new HashMap();
				rtn.put("status", StatusResult._permissionDenied);
				rtn.put("excepion", ExceptionUtil.getCausedBySrcMsg(ex));
			}
			try {
				session.write(JSONUtil.parserToStr(rtn));
			} catch (Throwable e) {
				logger.error("received write to session error：" + ExceptionUtil.getCausedBySrcMsg(e), e);
			}

		}

	}

	@Override
	public void closed(Session session) throws Exception {
		this.logger.info("tcp closed " + session);
		try {
			if (session != null) {
				String tokenid = session.getAttribute("tokenid") + "";
				if (!StringUtil.isEmptys(tokenid)) {
					// this.onlineService.removeOnlineByToken(token);

					Online online = onlineService.getOnlineByTokenId(tokenid);
					if (online != null) {
						Object o = online.getO();
						Map po = null;
						if (o instanceof Map) {
							po = (Map) o;
							List<Session> list = (List<Session>) po.get(OnlineDataField._online_cs_session);
							if (list != null) {
								list.remove(session);
							}
							if(list==null||list.size()==0)
								online.resetStatus(Type.tcp);
						}
					}
				}
			}
		} catch (Throwable e) {
			logger.error("closed session error：" + ExceptionUtil.getCausedBySrcMsg(e), e);
		}
	}

	@Override
	public void opened(Session session) throws Exception {
		this.logger.info("tcp opened " + session);
		if (session == null)
			return;
		if (null == session.getAttribute("binary"))
			session.setAttribute("binary", false);
		try {
			Map<Object, Object> rtn = new HashMap<Object, Object>();
			rtn.put("status", StatusResult._SUCCESS);
			rtn.put("name", "massplat_leliao");
			rtn.put("version", "1.0.1");
			rtn.put("type", 1);
			rtn.put("binary", false);

			// Map<String, Object> redirect = new HashMap<>();
			// rtn.put("redirect",redirect);
			// redirect.put("domain", "leliao1.leauto.com");
			// redirect.put("port", 8060);

			session.write(JSONUtil.parserToStr(rtn).toString());
		} catch (Throwable e) {
			logger.error("opened session error：" + ExceptionUtil.getCausedBySrcMsg(e), e);
		}
	}

	@Override
	public void idled(Session session) throws Exception {
		if (session != null)
			session.close();
	}

	@Override
	public void sent(Session session) throws Exception {
		// this.logger.info("tcp sent");
		// Thread.sleep(5000);
		// Map<Object,Object> rtn =new HashMap<Object, Object>();
		// rtn.put("status",1);
		//
		// session.write(JsonUtil.parserToStr(rtn).toString());
	}

	@Override
	public int getReaderIdleCount() {
		return 0;
	}
}
