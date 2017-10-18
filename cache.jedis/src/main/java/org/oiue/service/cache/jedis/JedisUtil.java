package org.oiue.service.cache.jedis;

import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.JedisPool;

/**
 * Jedis 操作工具类
 * @version 基于Jedis 2.9.0
 * @author lisuo
 *
 */
public class JedisUtil  {

	// jedis连接池
	private JedisPool jedisPool;

	// jedis集群
	private JedisCluster jedisCluster;

	// 是否为集群,默认不是集群
	private boolean isCluster = false;

	/**
	 * 连接池构建方式
	 * @param jedisPool  jedis连接池
	 */
	public JedisUtil(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	/**
	 * 集群构建方式
	 * @param jedisCluster jedis集群实例
	 */
	public JedisUtil(JedisCluster jedisCluster) {
		this.jedisCluster = jedisCluster;
		isCluster = true;
	}

	// -----------------------------------实现脚本命令-----------------------------------
	public Object eval(String script, int keyCount, String... params) {
		if (isCluster) {
			return jedisCluster.eval(script, keyCount, params);
		} else {
			Jedis jedis = jedisPool.getResource();
			try {
				return jedis.eval(script, keyCount, params);
			} finally {
				jedis.close();
			}
		}
	}

	public Object eval(String script, List<String> keys, List<String> args) {
		if (isCluster) {
			return jedisCluster.eval(script, keys, args);
		} else {
			Jedis jedis = jedisPool.getResource();
			try {
				return jedis.eval(script, keys, args);
			} finally {
				jedis.close();
			}
		}
	}

	public Object evalsha(String sha1, int keyCount, String... params) {
		if (isCluster) {
			return jedisCluster.evalsha(sha1, keyCount, params);
		} else {
			Jedis jedis = jedisPool.getResource();
			try {
				return jedis.evalsha(sha1, keyCount, params);
			} finally {
				jedis.close();
			}
		}
	}

	/**
	 * 获取JedisCommands实例
	 * @return JedisCommands
	 */
	private JedisCommands getJedisCommands() {
		if (isCluster) {
			return jedisCluster;
		} else {
			return jedisPool.getResource();
		}
	}

	/**
	 * Callback 回调接口
	 * @param <T>
	 */
	public interface Callback<T> {
		/**
		 * 回调函数
		 * @param commands
		 * @return
		 */
		public T call(JedisCommands commands);
	}

	/**
	 * 执行Redis 命令
	 * @param callback 回调接口
	 * @return
	 */
	public <T> T execute(Callback<T> callback) {
		JedisCommands jedisCommands = getJedisCommands();
		try {
			return callback.call(jedisCommands);
		} finally {
			// 非集群下释放资源,集群源码中已实现释放资源
			if (!isCluster) {
				((Jedis) jedisCommands).close();
			}
		}
	}

	// 实现JedisCommands,关于@Deprecated标记的方法参看Jedis API,如果报错可能是版本过高,一些方法被废除
	public String get(final String key) {
		return execute(new Callback<String>() {
			@Override
			public String call(JedisCommands commands) {
				return commands.get(key);
			}
		});
	}
	// 略过其他JedisCommands接口方法
}