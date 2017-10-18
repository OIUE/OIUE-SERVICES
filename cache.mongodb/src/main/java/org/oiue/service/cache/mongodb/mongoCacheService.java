package org.oiue.service.cache.mongodb;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Dictionary;

import org.oiue.service.cache.CacheService;
import org.oiue.service.cache.Type;
import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

@SuppressWarnings({"serial" })
public class mongoCacheService implements CacheService, Serializable {

	private Logger logger;

	public mongoCacheService(LogService logService) {
		logger = logService.getLogger(this.getClass());
	}

	@Override
	public void put(String name, Object object, Type type) {
	}

	@Override
	public void put(String name, String key, Object object, Type type) {
	}

	@Override
	public void put(String name, Object object, Type type, int expire) {

	}

	@Override
	public void put(String name, String key, Object object, Type type, int expire) {

	}

	@Override
	public Object get(String name) {
		return null;
	}

	@Override
	public Object get(String name, String key) {
		return null;
	}

	@Override
	public long delete(String name) {
		return 0l;
	}

	@Override
	public long delete(String name, String... keys) {
		return 0l;
	}

	@Override
	public boolean exists(String name) {
		return false;
	}

	@SuppressWarnings({ "resource", "unused" })
	public void updated(Dictionary<String, ?> dict) {
		try {
			String user = "admin";
			String pwd = "111111";
			String authDb = "admin";
			String host = "192.168.0.8";
			int port = 27017;
			// 1. 直接连接
			MongoCredential credential = MongoCredential.createCredential(user, authDb, pwd.toCharArray());
			MongoClient mongoClient = new MongoClient(new ServerAddress(host , port), Arrays.asList(credential));

			// 2. 使用连接字符串连接
			// mongodb://user:pwd@host:port/?authSource=db
			//String conString = "mongodb://{0}:{1}@{2}:{3}/?authSource={4}";
			//MongoClientURI uri = new MongoClientURI(MessageFormat.format(conString, user, pwd, host, port+"", authDb)); //注意port为字符串
			//MongoClient mongoClient = new MongoClient(uri);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
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
		// TODO Auto-generated method stub

	}

}
