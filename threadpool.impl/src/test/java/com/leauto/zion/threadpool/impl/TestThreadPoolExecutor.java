package com.leauto.zion.threadpool.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

@SuppressWarnings("rawtypes")
public class TestThreadPoolExecutor {
	
	@Test
	public void testThreadPool() {
		int minThreads = 5;
		int maxThreads = 500;
		int keepaliveSec = 300;
		int queueNum = 3;
		
		ThreadPoolExecutor pool = null;
		pool = new ThreadPoolExecutor(minThreads, maxThreads, // 线程池维护线程的最少数量,和最大数量
				keepaliveSec, TimeUnit.MILLISECONDS, // 线程池维护线程所允许的空闲时间和单位
				new LinkedBlockingQueue<Runnable>(queueNum), // 缓冲队列
				new ThreadPoolExecutor.CallerRunsPolicy()); // 重试添加当前的任务，他会自动重复调用execute()方法
		pool.prestartAllCoreThreads();
		pool.setMaximumPoolSize(maxThreads + maxThreads);
		//
		// for(int i = 0;i<100000;i++){
		// pool.execute(new testThread());
		// System.out.println(i+"|"+pool.getActiveCount());
		// if(pool.getActiveCount()==1000){
		// pool.setMaximumPoolSize(2000);
		// }
		// if(i>4000&&pool.getActiveCount()<500){
		// pool.setMaximumPoolSize(200);
		// }
		// }
		
		// new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler)
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testThreadPoolBlockingQueue() {
		int minThreads = 5;
		int maxThreads = 500;
		int keepaliveSec = 300;
		int queueNum = 500;
		
		// BlockingQueue bq = new LinkedBlockingQueue< Runnable >( queueNum ) ;
		BlockingQueue bq = new PriorityBlockingQueue<Runnable>(queueNum);
		ThreadPoolExecutor pool = null;
		pool = new ThreadPoolExecutor(minThreads, maxThreads, // 线程池维护线程的最少数量,和最大数量
				keepaliveSec, TimeUnit.MILLISECONDS, // 线程池维护线程所允许的空闲时间和单位
				bq, // 缓冲队列
				new ThreadPoolExecutor.CallerRunsPolicy()); // 重试添加当前的任务，他会自动重复调用execute()方法
		pool.prestartAllCoreThreads();
		pool.setMaximumPoolSize(maxThreads + maxThreads);
		
		// for(int i = 0;i<100000;i++){
		// pool.execute(new testThread());
		//// try {
		//// bq.put(new testThread());
		//// } catch (InterruptedException e) {
		//// }
		// System.out.println(i+"|"+pool.getActiveCount()+"|"+bq.size());
		//// if(pool.getActiveCount()==1000){
		//// pool.setMaximumPoolSize(2000);
		//// }
		//// if(i>4000&&pool.getActiveCount()<500){
		//// pool.setMaximumPoolSize(200);
		//// }
		// }
		
		while (bq.size() > 0 || pool.getActiveCount() > 0) {
			System.out.println(pool.getActiveCount() + "|" + bq.size());
			if (bq.size() > 5000) {
				pool.setCorePoolSize(500);
			} else if (bq.size() > 1000) {
				pool.setCorePoolSize(100);
			}
			
			if (bq.size() < 500) {
				pool.setCorePoolSize(100);
			} else if (bq.size() < 100) {
				pool.setCorePoolSize(5);
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
		// new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler)
	}
	
	public class testThread implements Runnable, Comparable<testThread> {
		
		@Override
		public void run() {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public int compareTo(testThread o) {
			return 0;
		}
		
	}
	
}
