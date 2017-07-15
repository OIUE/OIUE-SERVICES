/**
 *
 */
package org.oiue.service.online;

import java.io.Serializable;
import java.util.Collection;
import java.util.Dictionary;

/**
 * @author Every
 * 在线管理服务
 */
public interface OnlineService extends Serializable {
	/**
	 * 获取当前在线的操作员集合
	 * @return Collection&lt;Online&gt; 该服务器当前在线的用户对象
	 */
	public Collection<Online> getOnlines();
	/**
	 * 获取使用指定用户ID登陆的操作员列表
	 * @param user_id 用户ID
	 * @return Collection&lt;Online&gt; 指定用户在线的用户对象
	 */
	public Collection<Online> getOnlinesByUserID(String user_id);
	/**
	 * 根据用户标识获取指定操作员
	 * @param token 用户令牌
	 * @return 用户对象
	 */
	public Online getOnlineByToken(String token);
	public Online getOnlineByTokenId(String tokenId);
	/**
	 * 根据用户标识判断用户在线
	 * @param token 用户令牌
	 * @return boolean 是否在线
	 */
	public boolean isOnlineByToken(String token);
	/**
	 * 添加在线用户
	 * @param tokenId 令牌id
	 * @param online 在线对象
	 * @return boolean 添加成功与否
	 */
	public boolean putOnline(String tokenId,Online online);
	/**
	 * 根据用户token移除用户
	 * @param token 用户令牌
	 * @return boolean 下线成功与否
	 */
	public boolean removeOnlineByToken(String token);
	public boolean removeOnlineByTokenId(String tokenId);
	/**
	 * 根据用户id移除用户
	 * @param user_id 用户id
	 * @return boolean 踢出成功与否
	 */
	public boolean removeOnlineByUserId(String user_id);
	/**
	 * 注册离线回调
	 * @param name 注册名
	 * @param handler 回调对象
	 * @param index 顺序
	 * @return boolean 是否注册成功
	 */
	public boolean registerOfflineHandler(String name,OfflineHandler handler,int index);
	/**
	 * 取消离线监听
	 * @param name 注册名
	 * @return boolean 是否取消成功
	 */
	public boolean unRegisterOfflineHandler(String name);
	/**
	 * 注册上线回调
	 * @param name 注册名
	 * @param handler 回调对象
	 * @param index 顺序
	 * @return boolean 是否注册成功
	 */
	public boolean registerOnlineHandler(String name,OnlineHandler handler,int index);
	/**
	 * 取消上线监听
	 * @param name 注册名
	 * @return boolean 是否取消成功
	 */
	public boolean unRegisterOnlineHandler(String name);
	/**
	 * 配置文件变更
	 * @param props 配置
	 */
	public void updated(Dictionary<?, ?> props);
}
