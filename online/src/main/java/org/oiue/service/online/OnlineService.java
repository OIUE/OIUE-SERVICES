/**
 * 
 */
package org.oiue.service.online;

import java.io.Serializable;
import java.util.Collection;
import java.util.Dictionary;
import java.util.List;

/**
 * @author Every
 * 在线管理服务
 */
public interface OnlineService extends Serializable {
	/**
	 * 获取当前在线的操作员集合
	 * @return
	 */
	public Collection<Online> getOnlines();
	/**
	 * 获取使用指定用户ID登陆的操作员列表
	 * @param user_id 用户ID
	 * @return
	 */
	public List<Online> getOnlinesByUserID(String user_id);
	/**
	 * 根据用户标识获取指定操作员
	 * @param token
	 * @return
	 */
	public Online getOnlineByToken(String token);
	/**
	 * 根据用户标识判断用户在线
	 * @param token
	 * @return
	 */
	public boolean isOnlineByToken(String token);
	/**
	 * 添加在线用户
	 * @param token
	 * @param online
	 * @return
	 */
	public boolean putOnline(String token,Online online);
	/**
	 * 根据用户token移除用户
	 * @param token
	 * @return
	 */
	public boolean removeOnlineByToken(String token);
	/**
	 * 根据用户id移除用户
	 * @param user_id
	 * @return
	 */
	public boolean removeOnlineByUserId(String user_id);
	/**
	 * 注册离线回调
	 * @param name
	 * @param handler
	 * @param index
	 * @return
	 */
	public boolean registerOfflineHandler(String name,OfflineHandler handler,int index);
	/**
	 * 取消离线监听
	 * @param name
	 * @return
	 */
	public boolean unRegisterOfflineHandler(String name);
	/**
	 * 注册上线回调
	 * @param name
	 * @param handler
	 * @param index
	 * @return
	 */
	public boolean registerOnlineHandler(String name,OnlineHandler handler,int index);
	/**
	 * 取消上线监听
	 * @param name
	 * @return
	 */
	public boolean unRegisterOnlineHandler(String name);
	/**
	 * 配置文件变更
	 * @param props
	 */
	public void updated(Dictionary<?, ?> props);
}
