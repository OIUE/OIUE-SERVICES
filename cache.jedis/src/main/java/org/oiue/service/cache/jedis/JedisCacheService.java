package org.oiue.service.cache.jedis;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.oiue.service.cache.CacheService;
import org.oiue.service.cache.Type;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.tools.string.StringUtil;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@SuppressWarnings({"serial" })
public class JedisCacheService implements CacheService, Serializable {

	private Logger logger;

	public JedisCacheService(LogService logService) {
		logger = logService.getLogger(this.getClass());
	}

	@Override
	public void put(String name, Object object, Type type) {
		if (type == Type.ONE) {
			if (isCluster) {
				getJedis().set(name, object == null ? null : object.toString());
			}else{
				Jedis jedis = (Jedis) getJedis();
				try {
					jedis.set(name, object == null ? null : object.toString());
				} finally {
					jedis.close();
				}
			}
		}
	}

	@Override
	public void put(String name, String key, Object object, Type type) {
		if (type == Type.ONE) {
			try {
				if (isCluster) {
					synchronized (getJedis()) {
						getJedis().hset(name,key, object == null ? null : object.toString());
					}
				}else{
					Jedis jedis = (Jedis) getJedis();
					try {
						jedis.hset(name,key, object == null ? null : object.toString());
					} finally {
						jedis.close();
					}
				}
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
			}
		}else if (type == Type.MANY){
			if (isCluster) {
				synchronized (getJedis()) {
					getJedis().hset(name,key, object == null ? null : object.toString());
				}
			}else{
				Jedis jedis = (Jedis) getJedis();
				try {
					int db = 0;
					try {
						db = Integer.valueOf(name);
					} catch (Throwable e) {}
					jedis.select(db);
					jedis.sadd(key, object == null ? null : object.toString());
				} finally {
					jedis.close();
				}
			}
		}
	}

	@Override
	public void put(String name, Object object, Type type, int expire) {

	}

	@Override
	public void put(String name, String key, Object object, Type type, int expire) {

	}

	@Override
	public Object get(String name) {
		if (isCluster) {
			return getJedis().get(name);
		}else{
			Jedis jedis = (Jedis) getJedis();
			try {
				return jedis.get(name);
			} finally {
				jedis.close();
			}
		}
	}

	@Override
	public Object get(String name, String key) {
		if (isCluster) {
			return getJedis().hget(name, key);
		}else{
			Jedis jedis = (Jedis) getJedis();
			try {
				return jedis.hget(name, key);
			} finally {
				jedis.close();
			}
		}
	}

	@Override
	public long delete(String name) {
		if (isCluster) {
			return getJedis().del(name);
		}else{
			Jedis jedis = (Jedis) getJedis();
			try {
				return jedis.del(name);
			} finally {
				jedis.close();
			}
		}
	}

	@Override
	public long delete(String name, String... keys) {
		if (isCluster) {
			return getJedis().hdel(name, keys);
		}else{
			Jedis jedis = (Jedis) getJedis();
			try {
				return jedis.hdel(name, keys);
			} finally {
				jedis.close();
			}
		}
	}

	@Override
	public boolean exists(String name) {
		if (isCluster) {
			return getJedis().exists(name);
		}else{
			Jedis jedis = (Jedis) getJedis();
			try {
				return jedis.exists(name);
			} finally {
				jedis.close();
			}
		}
	}

	public void updated(Dictionary<String, ?> dict) {
		try {
			isCluster = StringUtil.isTrue(dict.get("redis.isCluster") + "");
		} catch (Throwable e) {
			logger.error("redis.isCluster config error:" + e.getMessage(), e);
		}
		try {
			if(pool!=null)
				pool.close();
		} catch (Throwable e) {
			logger.error("close redis pool error:" + e.getMessage(), e);
		}
		try {
			if(jedisCluster!=null)
				jedisCluster.close();
		} catch (Throwable e) {
			logger.error("close redis pool error:" + e.getMessage(), e);
		}
		try {
			// 建立连接池配置参数
			GenericObjectPoolConfig config = new JedisPoolConfig();
			// 连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
			config.setBlockWhenExhausted(true);
			// 设置的逐出策略类名, 默认DefaultEvictionPolicy(当连接超过最大空闲时间,或连接数超过最大空闲连接数)
			config.setEvictionPolicyClassName("org.apache.commons.pool2.impl.DefaultEvictionPolicy");
			// 是否启用pool的jmx管理功能, 默认true
			config.setJmxEnabled(true);
			// MBean ObjectName = new ObjectName("org.apache.commons.pool2:type=GenericObjectPool,name=" + "pool" + i); 默 认为"pool", JMX不熟,具体不知道是干啥的...默认就好.
			config.setJmxNamePrefix("pool");
			// 是否启用后进先出, 默认true
			config.setLifo(true);
			// 最大空闲连接数, 默认8个
			config.setMaxIdle(Integer.valueOf(dict.get("redis.maxIdle") + ""));
			// 最小空闲连接数, 默认0
			config.setMinIdle(Integer.valueOf(dict.get("redis.minIdle") + ""));
			// 最大连接数, 默认8个
			config.setMaxTotal(Integer.valueOf(dict.get("redis.maxTotal") + ""));
			// 获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,默认-1
			config.setMaxWaitMillis(Integer.valueOf(dict.get("redis.maxWaitMillis") + ""));
			// 逐出连接的最小空闲时间 默认1800000毫秒(30分钟)
			config.setMinEvictableIdleTimeMillis(1800000);
			// 每次逐出检查时 逐出的最大数目 如果为负数就是 : 1/abs(n), 默认3
			config.setNumTestsPerEvictionRun(3);
			// 对象空闲多久后逐出, 当空闲时间>该值 且 空闲连接>最大空闲数时直接逐出,不再根据MinEvictableIdleTimeMillis判断 (默认逐出策略)
			config.setSoftMinEvictableIdleTimeMillis(1800000);
			// 在获取连接的时候检查有效性, 默认false
			config.setTestOnBorrow(StringUtil.isFalse(dict.get("redis.testOnBorrow") + ""));
			// 在空闲时检查有效性, 默认false
			config.setTestWhileIdle(true);
			// 逐出扫描的时间间隔(毫秒) 如果为负数,则不运行逐出线程, 默认-1
			config.setTimeBetweenEvictionRunsMillis(5000);

			int maxRedirections = Integer.valueOf(dict.get("redis.maxRedirections") + "");
			int timeout = Integer.valueOf(dict.get("redis.timeout") + "");
			String password = dict.get("redis.password") + "";
			String nodes = dict.get("redis.nodes") + "";

			if (isCluster) {
				String[] strs = nodes.split(",");
				Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
				for (String node : strs) {
					String[] tnode = node.split(":");
					jedisClusterNodes.add(new HostAndPort(tnode[0], Integer.parseInt(tnode[1])));
				}
				jedisCluster = new JedisCluster(jedisClusterNodes, timeout, maxRedirections, config);
			} else {
				String[] tnode = nodes.split(":");
				// 创建连接池
				pool = new JedisPool(config, tnode[0], Integer.parseInt(tnode[1]), timeout, password);
			}

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}

	private boolean isCluster = true;
	private JedisPool pool;
	private JedisCluster jedisCluster;
	private JedisCommands getJedis() {
		if(isCluster){
			return jedisCluster;
		}else{
			return pool.getResource();
		}
	}

	@Override
	public boolean contains(String name, String... keys) {
		return false;
	}

	@Override
	public void put(String name, String key, Type type, Object... objects) {

	}

	@Override
	public void swap(String nameA, String nameB) {

	}

}
