package org.oiue.service.consume;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.oiue.service.online.Online;
import org.oiue.tools.StatusResult;

@SuppressWarnings("rawtypes")
public interface ConsumeService extends Serializable {
	/**
	 * 订阅/取消指令调用入口
	 * @param data 数据参数
	 * @param permission 权限
	 * @param tokenid 用户令牌id
	 * @return 返回数据
	 */
	public Object consume(Map data, Map permission, String tokenid);
	
	public Object unConsume(Map data, Map permission, String tokenid);
	
	/**
	 * 订阅/取消注册
	 * @param online 用户在线对象
	 * @param consume 订阅
	 * @return 状态
	 */
	public StatusResult consume(Online online, Map<String, Object> consume);
	
	public StatusResult unConsume(Online online, Map<String, Object> consume);
	
	/**
	 * 取消订阅
	 * @param tokenid 用户令牌id
	 * @return 状态
	 */
	public StatusResult unConsume(String tokenid);
	
	/**
	 * 根据token推送消息
	 * @param tokenid 用户令牌id
	 * @param data 推送数据
	 * @return 状态
	 */
	public StatusResult setDataBytoken(String tokenid, Map<String, Object> data);
	
	/**
	 * 向用户推送数据
	 * @param userId
	 * @param data
	 * @return 状态
	 */
	public StatusResult setDataByUserID(String userId, Map<String, Object> data);
	
	/**
	 * 向用户集推送数据
	 * @param userIDS 用户id列表
	 * @param data 数据
	 * @return 状态
	 */
	public StatusResult setDataByUserIDS(Set userIDS, Map<String, Object> data);
	
	public StatusResult setDataByOnline(Online online, Map<String, Object> data);
	
	public StatusResult setDataByOnline(Online online, byte[] data);
	
	/**
	 * 推送数据
	 * @param data 数据
	 * @return 状态
	 */
	public StatusResult setData(Map<String, Object> data);
	
	/**
	 * 配置文件变更
	 * @param props 配置信息
	 */
	public void updated(Map<?, ?> props);
}
