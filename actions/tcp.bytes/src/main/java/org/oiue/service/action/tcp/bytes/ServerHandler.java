package org.oiue.service.action.tcp.bytes;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.oiue.service.action.api.ActionService;
import org.oiue.service.bytes.api.BytesRuleField;
import org.oiue.service.bytes.api.BytesService;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.online.Online;
import org.oiue.service.online.OnlineDataField;
import org.oiue.service.online.OnlineService;
import org.oiue.service.online.Type;
import org.oiue.service.tcp.Handler;
import org.oiue.service.tcp.Session;
import org.oiue.tools.StatusResult;
import org.oiue.tools.bytes.ByteUtil;
import org.oiue.tools.bytes.Crc;
import org.oiue.tools.exception.ExceptionUtil;
import org.oiue.tools.exception.OIUEException;
import org.oiue.tools.json.JSONUtil;
import org.oiue.tools.map.MapUtil;
import org.oiue.tools.string.StringUtil;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ServerHandler implements Handler {
	private ActionService actionService;
	private Logger logger;
	private OnlineService onlineService;
	private BytesService bytesService;

	private String KEY_READ_BUFFER = "key_read_buffer";
	private String KEY_READ_Queue = "key_read_Queue";
	private String KEY_READ_THREAD = "key_read_thread";
	private String KEY_SENT_INFO = "key_sent_info";

	private boolean runReceived = true;
	private String bf_charset = "ISO-8859-1";
	private String charset = "UTF-8";
	private Dictionary props;

	private int anonymous_count = 3;

	private String universal_answer = "";
	private String universal_reply = "";
	private String universal_head = "";


	public ServerHandler(LogService logService, ActionService actionService, OnlineService onlineService, BytesService bytesService) {
		this.actionService = actionService;
		this.onlineService = onlineService;
		this.logger = logService.getLogger(this.getClass());
		this.bytesService = bytesService;
	}

	public void updated(Dictionary props) {
		this.props = props;
		try {
			anonymous_count = Integer.valueOf(this.props.get("anonymousCount") + "");
		} catch (Exception e) {
		}
		universal_answer = props.get("msg.universal.answer") + "";
		universal_reply = props.get("msg.universal.reply") + "";
		universal_head = props.get("msg.universal.head") + "";
	}

	@Override
	public void received(Session session, String line, byte[] bytes)  {
		Map<Object, Object> per = null;
		Map<Object, Object> rtn = null;
		if (logger.isDebugEnabled()) {
			logger.debug("[" + session + "]received :" + ByteUtil.toHexString(bytes));
		}
		try {
			if (bytes == null || bytes.length == 0)
				return;
			if (null == session.getAttribute("binary"))
				session.setAttribute("binary", true);
			try {
				StringBuffer readBuffer = (StringBuffer) session.getAttribute(KEY_READ_BUFFER);
				readBuffer.append(new String(bytes, bf_charset));

				LinkedBlockingQueue<Object> receivedQueue = (LinkedBlockingQueue<Object>) session.getAttribute(KEY_READ_Queue);
				receivedQueue.add(new Object());

				if (session.getAttribute(KEY_READ_THREAD) == null) {
					Thread receivedTh = new Thread(new receivedRunable(readBuffer, receivedQueue, session),receivedRunable.class.getName());
					session.setAttribute(KEY_READ_THREAD, receivedTh);
					receivedTh.start();
				}
			} catch (Throwable e) {
				throw new OIUEException(StatusResult._format_error,"参数格式不正确！[" + ExceptionUtil.getCausedBySrcMsg(e) + "]" + " /n " + line + " /n " + per, e);
			}
		} catch (Throwable ex) {
			logger.error("received error：" + ExceptionUtil.getCausedBySrcMsg(ex), ex);
			rtn = new HashMap();
			rtn.put("status", StatusResult._permissionDenied);
			rtn.put("excepion", ExceptionUtil.getCausedBySrcMsg(ex));
			rtn.put("return_info", ExceptionUtil.getCausedBySrcMsg(ex));
			rtn.put("command_id", 0);
			rtn.put("command_serial", 0);
			byte[] rtn1 = bytesService.encoded(null, universal_reply, rtn);
			byte[] command = ByteUtil.int2bytes(0x9001, 2);
			send(session, rtn1, command);
		}
	}

	class receivedRunable implements Runnable {
		StringBuffer receiveSb;
		LinkedBlockingQueue<Object> receivedQueue;
		Session session;
		char start = 0x7E;
		char end = 0x7E;
		int start_i = -1;

		public receivedRunable(StringBuffer receiveSb, LinkedBlockingQueue<Object> receivedQueue, Session session) {
			this.receiveSb = receiveSb;
			this.receivedQueue = receivedQueue;
			this.session = session;
		}

		@Override
		public void run() {
			if (logger.isDebugEnabled()) {
				logger.debug("running receivedTh ....runReceived=" + runReceived);
			}
			try {
				while (runReceived) {
					if (logger.isDebugEnabled()) {
						logger.debug(" receivedTh running");
					}
					start_i = receiveSb.indexOf(start + "");
					if (start_i == -1) {
						receivedQueue.take();
						continue;
					}

					int end_i = receiveSb.indexOf(end + "", start_i + 1);
					if (end_i == -1) {
						receivedQueue.take();
						continue;
					} else {
						end_i += 1;
					}
					try {
						Map d = new HashMap();
						byte[] t_packet = receiveSb.substring(start_i, end_i).getBytes(bf_charset);
						if (end_i == 2) {
							receiveSb.deleteCharAt(0);
							continue;
						}
						if (logger.isDebugEnabled() && start_i > 1)
							logger.debug("received package error [0," + start_i + "] :" + ByteUtil.toHexString(receiveSb.substring(0, start_i).getBytes(bf_charset)));
						if (logger.isDebugEnabled())
							logger.debug("received package[" + start_i + "," + end_i + "] :" + ByteUtil.toHexString(t_packet));

						byte[] packet = new byte[t_packet.length - 2];
						System.arraycopy(t_packet, 1, packet, 0, packet.length);

						String ts = new String(packet, bf_charset);

						ts = ts.replace(new String(new byte[] { 0x7d, 0x02 }, bf_charset), new String(new byte[] { 0x7e }, bf_charset));
						ts = ts.replace(new String(new byte[] { 0x7d, 0x01 }, bf_charset), new String(new byte[] { 0x7d }, bf_charset));

						packet = ts.getBytes(bf_charset);

						if (Crc.crc(packet, 0, packet.length - 1) != packet[packet.length - 1]) {
							receiveSb.delete(0, end_i - 1);
							logger.error("CRC error,error data:" + ByteUtil.toHexString(packet));
							continue;
						}

						StatusResult sr = bytesService.decode(packet, universal_head, d);
						d.remove(BytesRuleField.sys_packet_index);
						if (logger.isDebugEnabled()) {
							logger.debug("[" + d.get("command_id") + "] decode head:" + d + ", sr=" + sr);
							logger.debug("[" + d.get("command_id") + "] data:" + ByteUtil.toHexString((byte[]) d.get("package_data")));
						}
						String msg_type_regex = props.get("msg.type." + d.get("command_id")) + "";
						if (StringUtil.isEmptys(msg_type_regex)) {
							throw new OIUEException(StatusResult._url_can_not_found,"not found command ID [" + d.get("command_id") + "] ");
						}
						Object regex;
						if (msg_type_regex.startsWith("{")) {
							regex = JSONUtil.parserStrToMap(msg_type_regex);
						} else if (msg_type_regex.startsWith("[")) {
							regex = JSONUtil.parserStrToList(msg_type_regex);
						} else {
							regex = msg_type_regex;
						}
						byte[] data = (byte[]) d.get("package_data");

						sr = bytesService.decode(data, regex, d);
						d.remove(BytesRuleField.sys_packet_index);
						if (logger.isDebugEnabled()) {
							logger.debug("[" + d.get("command_id") + "] decode data:" + d + ", sr=" + sr);
						}

						if (MapUtil.getInt(d, "command_id") == 0x0100) {
							String token = MapUtil.getString(d, "token");
							String tag = MapUtil.getString(d, "tag");
							if (StringUtil.isEmptys(session.getAttribute("tokenid") + "")) {
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
										session.setAttribute("token", token);
										session.setAttribute("tag", tag);
										list.add(session);
										online.setStatus(Type.tcp);
									} else {
										session.close();
										return;
									}
								} else {
									session.close();
									return;
								}
							} else if (!session.getAttribute("tokenid").equals(token)) {
								session.close();
								return;
							}
						} else if (MapUtil.getInt(d, "command_id") == 0x0001) {
							receiveSb.delete(0, end_i);
							continue;
						}

						Map per = new HashMap<>();
						per.put("tokenid", session.getAttribute("tokenid"));
						per.put("tag", session.getAttribute("tag"));
						per.put("modulename", props.get("msg.modulename." + d.get("command_id")));
						per.put("operation", props.get("msg.operation." + d.get("command_id")));
						per.put("data", d);
						if (logger.isDebugEnabled()) {
							logger.debug("call :" + per);
						}
						if (MapUtil.getInt(d, "command_id") != 0x0100)
							new Thread(new asynchronismction(session, per),asynchronismction.class.getName()).start();

						byte[] rtn1 = bytesService.encoded(null, universal_answer, d);
						d.remove(BytesRuleField.sys_packet_index);

						byte[] command = ByteUtil.int2bytes(0x9001, 2);
						send(session, rtn1, command);

						receiveSb.delete(0, end_i);
					} catch (Throwable e) {
						logger.error(e.getMessage(), e);
						receiveSb.delete(0, end_i - 1);
					}

				}

				if (logger.isDebugEnabled()) {
					logger.debug("stop receivedTh ....runReceived=" + runReceived);
				}

			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}

		}

	}

	private void send(Session session, byte[] source, byte[] command) {
		try {
			byte[] pack = new byte[source.length + 10];
			System.arraycopy(source, 0, pack, 8, source.length);

			System.arraycopy(command, 0, pack, 1, 2);

			System.arraycopy(ByteUtil.int2bytes(source.length, 2), 0, pack, 4, 2);

			int serial = 0;
			if (session.getAttribute("serial") != null) {
				serial = (int) session.getAttribute("serial");
			}
			System.arraycopy(ByteUtil.int2bytes(serial, 2), 0, pack, 6, 2);
			session.setAttribute("serial", serial++);

			pack[pack.length - 2] = Crc.crc(pack, 1, pack.length - 3);

			String ts = new String(pack, bf_charset);

			ts = ts.replace(new String(new byte[] { 0x7d }, bf_charset), new String(new byte[] { 0x7d, 0x01 }, bf_charset));
			ts = ts.replace(new String(new byte[] { 0x7e }, bf_charset), new String(new byte[] { 0x7d, 0x02 }, bf_charset));

			byte[] tb = ts.getBytes(bf_charset);

			tb[0] = 0x7e;
			tb[tb.length - 1] = 0x7e;
			if (logger.isDebugEnabled()) {
				logger.debug("[" + session.getAttribute("token") + "] send package :" + ByteUtil.toHexString(tb));
			}
			session.write(tb);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
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
			Object command_id = MapUtil.get(per, "data.command_id");
			Object command_serial = MapUtil.get(per, "data.command_serial");
			try {
				long startTime = 0l;
				if (logger.isDebugEnabled()) {
					startTime = System.currentTimeMillis();
					try {
						logger.debug(Thread.currentThread().getName() + "| per:" + per);
					} catch (Throwable e) {
					}
				}
				if (per == null)
					per = new HashMap<>();

				per.put("client_type", Type.tcp);

				rtn = actionService.request(per);

				if (logger.isDebugEnabled()) {
					logger.debug(Thread.currentThread().getName() + "," + (System.currentTimeMillis() - startTime) + "|per:" + per + ", rtn:" + rtn);
				}

			} catch (Throwable ex) {
				logger.error("received error：" + ExceptionUtil.getCausedBySrcMsg(ex), ex);
				rtn = new HashMap();
				rtn.put("status", StatusResult._permissionDenied);
				rtn.put("excepion", ExceptionUtil.getCausedBySrcMsg(ex));
			}
			Object data = rtn.get("data");
			if (data instanceof Map) {
				rtn.put("return_info", JSONUtil.parserToStr((Map<Object, Object>) data));
			} else if (data instanceof List) {
				rtn.put("return_info", JSONUtil.parserToStr((List) data));
			} else {
				rtn.put("return_info", "{}");
			}
			rtn.put("command_id", command_id);
			rtn.put("command_serial", command_serial);

			byte[] rtn1 = bytesService.encoded(null, universal_reply, rtn);
			byte[] command = ByteUtil.int2bytes(0x9000, 2);
			send(session, rtn1, command);
		}

	}

	@Override
	public void closed(Session session)  {
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
		} finally {
			session.close();
		}
	}

	@Override
	public void opened(Session session)  {
		this.logger.info("tcp opened " + session);
		if (session == null)
			return;
		if (null == session.getAttribute("binary"))
			session.setAttribute("binary", true);
		try {
			session.setAttribute(KEY_READ_BUFFER, new StringBuffer());
			session.setAttribute(KEY_READ_Queue, new LinkedBlockingQueue<Object>());
			Map<Object, Object> rtn = new HashMap<Object, Object>();
			rtn.put("status", StatusResult._SUCCESS);
			rtn.put("name", "massplat_leliao");
			rtn.put("version", "1.0.1");
			rtn.put("type", 1);
			rtn.put("binary", true);

			byte[] rtn1 = JSONUtil.parserToStr(rtn).toString().getBytes(charset);
			byte[] command = ByteUtil.int2bytes(0x9000, 2);
			send(session, rtn1, command);
		} catch (Throwable e) {
			logger.error("opened session error：" + ExceptionUtil.getCausedBySrcMsg(e), e);
		}
	}

	@Override
	public void idled(Session session)  {
		if (session != null)
			if (StringUtil.isEmptys(session.getAttribute("token") + "")) {
				logger.info("Close invalid connection:" + session);
				session.close();
			}
	}

	@Override
	public void sent(Session session)  {
		if (logger.isInfoEnabled())
			logger.info("tcp sent");
		if (session.getAttribute(KEY_SENT_INFO) == null) {
			session.setAttribute(KEY_SENT_INFO, 0);
		} else {
			int info = (Integer) session.getAttribute(KEY_SENT_INFO);
			if (info > anonymous_count) {
				if (StringUtil.isEmptys(session.getAttribute("token") + "")) {
					logger.info("Close invalid connection:" + session);
					session.close();
				}
			}
			if (info > 900000000)
				info = 0;
			session.setAttribute(KEY_SENT_INFO, 1 + info);
		}
	}

	@Override
	public int getReaderIdleCount() {
		return 0;
	}
}
