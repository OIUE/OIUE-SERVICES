package org.oiue.service.threadpool;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public interface ThreadPoolService extends Serializable {
	
	/**
	 * 
	 * @param name 线程池名称
	 * @param corePoolSize 线程池的基本大小
	 * @param maximumPoolSize 线程池最大大小
	 * @param keepAliveTime 线程活动保持时间
	 * @param unit 线程活动保持时间的单位
	 * @param workQueue 任务队列
	 */
	public void registerThreadPool(String name, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue);
	
	/**
	 * 
	 * @param name 线程池名称
	 * @param corePoolSize 线程池的基本大小
	 * @param maximumPoolSize 线程池最大大小
	 * @param keepAliveTime 线程活动保持时间
	 * @param unit 线程活动保持时间的单位
	 * @param workQueue 任务队列
	 * @param threadFactory 创建线程的工厂
	 * @param handler 饱和策略
	 */
	public void registerThreadPool(String name, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler);
	
	/**
	 * 
	 * @param name
	 */
	public void removeThreadPool(String name);
	
	/**
	 * 
	 * @param name 线程池名称
	 * @param task 任务
	 */
	public void addTask(String name, Runnable task);
	
	/**
	 * 
	 * @param name 线程池名称
	 * @param maximumPoolSize 线程池最大线程数量
	 */
	public void setMaximumPoolSize(String name, int maximumPoolSize);
	
	public int getMaximumPoolSize(String name);
	
	/**
	 * 
	 * @param name 线程池名称
	 * @param corePoolSize 线程池核心数量
	 */
	public void setCorePoolSize(String name, int corePoolSize);
	
	public int getCorePoolSize(String name);
	
	/**
	 * 
	 * @param name 线程池名称
	 */
	public int getActiveCount(String name);
	
	/**
	 * 
	 * @param name 线程池名称
	 * @return
	 */
	public List<Runnable> shutdownNow(String name);
	
	/**
	 * 
	 * @param name 线程池名称
	 * @return
	 */
	public BlockingQueue<Runnable> getQueue(String name);
	
	// /**
	// *
	// * @param name
	// * @return
	// */
	// public ThreadPoolExecutor getThreadPool(String name);
	
	/**
	 * 配置变更
	 * @param props
	 */
	public void updated(Map<?, ?> props);
}
