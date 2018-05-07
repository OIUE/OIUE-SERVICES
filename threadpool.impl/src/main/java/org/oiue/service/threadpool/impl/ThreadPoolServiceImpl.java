package org.oiue.service.threadpool.impl;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.threadpool.ThreadPoolService;

public class ThreadPoolServiceImpl implements ThreadPoolService, Serializable {
	private static final long serialVersionUID = 1L;
	static Map<String, ThreadPoolExecutor> threadPool = new ConcurrentHashMap<String, ThreadPoolExecutor>();
	Logger log;
	
	public ThreadPoolServiceImpl(LogService logService) {
		this.log = logService.getLogger(this.getClass());
	}
	
	@Override
	public void addTask(String name, Runnable task) {
		ThreadPoolExecutor tpe = threadPool.get(name);
		if (tpe == null)
			throw new RuntimeException("");
		tpe.execute(task);
	}
	
	@Override
	public void registerThreadPool(String name, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		ThreadPoolExecutor tpe = threadPool.get(name);
		if (tpe == null) {
			tpe = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
			threadPool.put(name, tpe);
		}
	}
	
	@Override
	public void registerThreadPool(String name, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
		ThreadPoolExecutor tpe = threadPool.get(name);
		if (tpe == null) {
			tpe = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
			threadPool.put(name, tpe);
		}
	}
	
	@Override
	public void removeThreadPool(String name) {
		ThreadPoolExecutor tpe = threadPool.remove(name);
		tpe.shutdown();
	}
	
	@Override
	public void updated(Dictionary<?, ?> props) {}
	
	@Override
	public int getActiveCount(String name) {
		ThreadPoolExecutor tpe = threadPool.get(name);
		if (tpe != null) {
			return tpe.getActiveCount();
		}
		return 0;
	}
	
	@Override
	public BlockingQueue<Runnable> getQueue(String name) {
		ThreadPoolExecutor tpe = threadPool.get(name);
		if (tpe != null) {
			return tpe.getQueue();
		}
		throw new RuntimeException("cont found Tread pool by name=" + name);
	}
	
	@Override
	public void setCorePoolSize(String name, int corePoolSize) {
		ThreadPoolExecutor tpe = threadPool.get(name);
		if (tpe != null) {
			tpe.setCorePoolSize(corePoolSize);
			return;
		}
		throw new RuntimeException("cont found Tread pool by name=" + name);
	}
	
	@Override
	public void setMaximumPoolSize(String name, int maximumPoolSize) {
		ThreadPoolExecutor tpe = threadPool.get(name);
		if (tpe != null) {
			tpe.setMaximumPoolSize(maximumPoolSize);
			return;
		}
		throw new RuntimeException("cont found Tread pool by name=" + name);
	}
	
	@Override
	public List<Runnable> shutdownNow(String name) {
		ThreadPoolExecutor tpe = threadPool.get(name);
		if (tpe != null) {
			return tpe.shutdownNow();
		}
		throw new RuntimeException("cont found Tread pool by name=" + name);
	}
	
	@Override
	public int getCorePoolSize(String name) {
		ThreadPoolExecutor tpe = threadPool.get(name);
		if (tpe != null) {
			return tpe.getCorePoolSize();
		}
		throw new RuntimeException("cont found Tread pool by name=" + name);
	}
	
	@Override
	public int getMaximumPoolSize(String name) {
		ThreadPoolExecutor tpe = threadPool.get(name);
		if (tpe != null) {
			return tpe.getMaximumPoolSize();
		}
		throw new RuntimeException("cont found Tread pool by name=" + name);
	}
}