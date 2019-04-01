package org.oiue.service.message;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.oiue.service.online.Online;
import org.oiue.tools.StatusResult;

@SuppressWarnings("rawtypes")
public interface MessageService extends Serializable {
	/**
	 * 根据token推送消息
	 * @param tokenid
	 * @param data
	 * @return
	 */
	public StatusResult setDataBytokenId(String tokenid, Map<String, Object> data);
	
	/**
	 * 向用户推送数据
	 * @param userId
	 * @param data
	 * @return
	 */
	public StatusResult setDataByUserID(String userId, Map<String, Object> data);
	
	/**
	 * 向用户集推送数据
	 * @param userIDS
	 * @param data
	 * @return
	 */
	public StatusResult setDataByUserIDS(Set userIDS, Map<String, Object> data);
	
	public StatusResult setDataByOnline(Online online, Map<String, Object> data);
	
	public StatusResult setDataByOnline(Online online, byte[] data);
	
	/**
	 * 配置文件变更
	 * @param props
	 */
	public void updated(Map<?, ?> props);
}
