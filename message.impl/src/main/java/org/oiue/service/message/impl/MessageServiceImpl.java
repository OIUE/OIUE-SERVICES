/**
 *
 */
package org.oiue.service.message.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.oiue.service.bytes.api.BytesRuleField;
import org.oiue.service.bytes.api.BytesService;
import org.oiue.service.cache.CacheServiceManager;
import org.oiue.service.io.Session;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.message.MessageService;
import org.oiue.service.online.OfflineHandler;
import org.oiue.service.online.Online;
import org.oiue.service.online.OnlineDataField;
import org.oiue.service.online.OnlineHandler;
import org.oiue.service.online.OnlineService;
import org.oiue.tools.StatusResult;
import org.oiue.tools.bytes.ByteUtil;
import org.oiue.tools.exception.ExceptionUtil;
import org.oiue.tools.json.JSONUtil;
import org.oiue.tools.map.MapUtil;

/**
 * @author Every
 *
 */
@SuppressWarnings({ "rawtypes", "serial", "unchecked" })
public class MessageServiceImpl implements MessageService, OnlineHandler, OfflineHandler {
	
	public static boolean setDataByUserIDS = false;
	public static boolean setDataByUserID = false;
	
	public static int maxMessage = 20;
	
	public Logger logger;
	public CacheServiceManager cache;
	public LogService logService;
	public OnlineService onlineService;
	private BytesService bytesService;
	private Map props;
	
	@Override
	public void updated(Map props) {
		this.props = props;
		try {
			maxMessage = Integer.valueOf(props.get("message.maxMessage") + "");
		} catch (Throwable e) {
			logger.error("updateConfigure is error:" + e.getMessage(), e);
		}
	}
	
	public MessageServiceImpl(LogService logService, CacheServiceManager cache, OnlineService onlineService, BytesService bytesService) {
		try {
			logger = logService.getLogger(this.getClass());
			this.logService = logService;
			this.cache = cache;
			this.onlineService = onlineService;
			this.bytesService = bytesService;
		} catch (Throwable e) {
			logger.error("MessageServiceImpl is error:" + e.getMessage(), e);
		}
	}
	
	@Override
	public StatusResult setDataByUserID(String user_id, Map<String, Object> data) {
		if (logger.isDebugEnabled() && setDataByUserID)
			this.logger.debug("send data by userids:" + user_id + ",data:" + data);
		Collection<Online> users = this.onlineService.getOnlinesByUserID(user_id);
		if (users != null) {
			try {
				for (Online online : users) {
					try {
						this.setDataByOnline(online, data);
					} catch (Throwable e) {
						logger.error(e.getMessage(), e);
					}
				}
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
		} else {
			if (logger.isInfoEnabled() && setDataByUserID)
				this.logger.info("The user is not online, message discard,userid=" + user_id + ",message=" + data);
		}
		StatusResult sr = new StatusResult();
		return sr;
	}
	
	@Override
	public StatusResult setDataByUserIDS(Set userIds, Map<String, Object> data) {
		if (logger.isDebugEnabled() && setDataByUserIDS)
			this.logger.debug("send data by userids:" + userIds + ",data:" + data);
		StatusResult sr = new StatusResult();
		List<StatusResult> lsr = new ArrayList<>();
		if (userIds != null) {
			for (Iterator iterator = userIds.iterator(); iterator.hasNext();) {
				String user_id = (String) iterator.next();
				sr = this.setDataByUserID(user_id, data);
				if (sr.getResult() <= StatusResult._ncriticalAbnormal) {
					lsr.add(sr);
				}
			}
		}
		sr.setResult(lsr.size() == 0 ? StatusResult._SUCCESS : StatusResult._ncriticalAbnormal);
		return sr;
	}
	
	@Override
	public StatusResult setDataBytokenId(String tokenId, Map<String, Object> data) {
		Online online = this.onlineService.getOnlineByTokenId(tokenId);
		return this.setDataByOnline(online, data);
	}
	
	@Override
	public StatusResult setDataByOnline(Online online, Map<String, Object> data) {
		StatusResult sr = new StatusResult();
		if (online == null) {
			sr.setResult(StatusResult._ncriticalAbnormal);
			sr.setDescription("incept user offline!");
			return sr;
		}
		Map sysmap = null;
		sysmap = (Map) online.getO();
		if (sysmap != null) {
			switch (online.getBestType()) {
				case tcp:
					List<Session> sessionList = (List<Session>) sysmap.get(OnlineDataField._online_cs_session);
					if (sessionList != null) {
						for (Iterator iterator = sessionList.iterator(); iterator.hasNext();) {
							Session session = (Session) iterator.next();
							if (session == null) {
								iterator.remove();
								sr.setDescription("incept user[" + online.getTokenId() + "] offline !");
							} else
								try {
									if ((boolean) session.getAttribute("binary")) {
										String rule = this.props.get("message." + MapUtil.get(data, "system_msg_type")) + "";
										if (logger.isDebugEnabled()) {
											logger.debug("send msg:" + data + ",rule:" + rule);
										}
										int serial = 0;
										if (session.getAttribute("serial") != null) {
											serial = (int) session.getAttribute("serial");
										}
										data.put("serial", serial);
										session.setAttribute("serial", ++serial);
										data.remove(BytesRuleField.sys_packet_index);
										byte[] bs = bytesService.encoded(null, rule, data);
										if (logger.isDebugEnabled()) {
											logger.debug("send msg:" + ByteUtil.toHexString(bs) + "|" + session);
										}
										session.write(bs);
									} else {
										session.write(JSONUtil.getJSONString(data));
									}
									sr.setResult(StatusResult._SUCCESS);
									return sr;
								} catch (Throwable e) {
									String msg = "send data by session is error!" + ExceptionUtil.getCausedBySrcMsg(e);
									logger.error(msg, e);
									sr.setDescription("incept user[" + online.getToken() + "] " + msg);
								}
						}
					} else {
						sr.setDescription("incept user[" + online.getToken() + "] offline or no gateway!2");
					}
					break;
				
				case webSocket:
					List<Session> list = (List<Session>) sysmap.get(OnlineDataField._online_cs_session);
					if (list != null) {
						for (Iterator iterator = list.iterator(); iterator.hasNext();) {
							Session session = (Session) iterator.next();
							if (session == null) {
								iterator.remove();
								sr.setDescription("incept user[" + online.getToken() + "] offline !");
							} else
								try {
									if ((boolean) session.getAttribute("binary")) {
										String rule = this.props.get("message." + MapUtil.get(data, "system_msg_type")) + "";
										if (logger.isDebugEnabled()) {
											logger.debug("send msg:" + data + ",rule:" + rule);
										}
										int serial = 0;
										if (session.getAttribute("serial") != null) {
											serial = (int) session.getAttribute("serial");
										}
										data.put("serial", serial);
										session.setAttribute("serial", ++serial);
										data.remove(BytesRuleField.sys_packet_index);
										byte[] bs = bytesService.encoded(null, rule, data);
										if (logger.isDebugEnabled()) {
											logger.debug("send msg:" + ByteUtil.toHexString(bs) + "|" + session);
										}
										session.write(bs);
									} else {
										session.write(JSONUtil.getJSONString(data));
									}
									sr.setResult(StatusResult._SUCCESS);
									return sr;
								} catch (Throwable e) {
									String msg = "send data by session is error!" + ExceptionUtil.getCausedBySrcMsg(e);
									logger.error(msg, e);
									sr.setDescription("incept user[" + online.getToken() + "] " + msg);
								}
						}
					} else {
						sr.setDescription("incept user[" + online.getToken() + "] offline or no gateway!2");
					}
					break;
				
				case socketIo:
				
				case udp:
				
				case http:
				default:
					break;
			};
		} else {
			sr.setDescription("incept user[" + online.getToken() + "] offline or no gateway!1 " + online);
		}
		
		sr.setResult(StatusResult._ncriticalAbnormal);
		return sr;
	}
	
	@Override
	public StatusResult setDataByOnline(Online online, byte[] data) {
		StatusResult sr = new StatusResult();
		if (online == null) {
			sr.setResult(StatusResult._ncriticalAbnormal);
			sr.setDescription("online can not null!");
			return sr;
		}
		Map sysmap = (Map) online.getO();
		List<Session> list = (List<Session>) sysmap.get(OnlineDataField._online_cs_session);
		if (list != null) {
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Session session = (Session) iterator.next();
				if (session == null)
					iterator.remove();
				try {
					if ((boolean) session.getAttribute("binary")) {
						session.write(JSONUtil.getJSONString(data));
						sr.setResult(StatusResult._SUCCESS);
						return sr;
					}
				} catch (Throwable e) {
					logger.error("send data by session is error!" + ExceptionUtil.getCausedBySrcMsg(e), e);
				}
			}
		}
		sr.setResult(StatusResult._ncriticalAbnormal);
		return sr;
	}
	
	@Override
	public void logout(Online online) {
		
	}
	
	@Override
	public void login(Online online) {
		
	}
}
