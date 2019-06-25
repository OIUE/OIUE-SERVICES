package com.stay4it.rx;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class FutureTest {
	public static class Task implements Runnable {
		@Override
		public void run() {
			System.out.println("run");
		}
	}

	public static class Task2 implements Callable<Integer> {
		@Override
		public Integer call() throws Exception {
			System.out.println("call");
			return fibc(30);
		}
	}

	/** * * runnable, 无返回值 */
	public static void testRunnable() {
		ExecutorService executorService = Executors.newCachedThreadPool();
		Future<String> future = (Future<String>) executorService.submit(new Task());
		try {
			System.out.println(future.get());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		executorService.shutdown();
	}

	/** * * Callable, 有返回值 */
	public static void testCallable() {
		ExecutorService executorService = Executors.newCachedThreadPool();
		Future<Integer> future = (Future<Integer>) executorService.submit(new Task2());
		try {
			System.out.println(future.get());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		executorService.shutdown();
	}

	/** * * FutureTask则是一个RunnableFuture<V>，即实现了Runnbale又实现了Futrue<V>这两个接口， * 另外它还可以包装Runnable(实际上会转换为Callable)和Callable * <V>，所以一般来讲是一个符合体了，它可以通过Thread包装来直接执行，也可以提交给ExecuteService来执行 * ，并且还可以通过v get()返回执行结果，在线程体没有执行完成的时候，主线程一直阻塞等待，执行完则直接返回结果。 */
	public static void testFutureTask() {
		ExecutorService executorService = Executors.newCachedThreadPool();
		FutureTask<Integer> futureTask = new FutureTask<Integer>(new Task2());
		executorService.submit(futureTask);
		try {
			System.out.println(futureTask.get());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		executorService.shutdown();
	}

	/** * * FutureTask则是一个RunnableFuture<V>，即实现了Runnbale又实现了Futrue<V>这两个接口， * 另外它还可以包装Runnable(实际上会转换为Callable)和Callable * <V>，所以一般来讲是一个符合体了，它可以通过Thread包装来直接执行，也可以提交给ExecuteService来执行 * ，并且还可以通过v get()返回执行结果，在线程体没有执行完成的时候，主线程一直阻塞等待，执行完则直接返回结果。 */
	public static void testFutureTask2() {
		ExecutorService executorService = Executors.newCachedThreadPool();
		FutureTask<Integer> futureTask = new FutureTask<Integer>(new Runnable() {
			@Override
			public void run() {
				System.out.println("testFutureTask2 run");
			}
		}, fibc(30));
		executorService.submit(futureTask);
		try {
			System.out.println(futureTask.get());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		executorService.shutdown();
	}

	public static void main(String[] args) {
		testCallable();
	}

	/** * * 效率低下的斐波那契数列, 耗时的操作 * * @param num * @return */
	static int fibc(int num) {
		if (num == 0) {
			return 0;
		}
		if (num == 1) {
			return 1;
		}
		return fibc(num - 1) + fibc(num - 2);
	}
}