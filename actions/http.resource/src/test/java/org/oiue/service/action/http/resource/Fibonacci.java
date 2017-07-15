package org.oiue.service.action.http.resource;
import org.junit.Test;

/*
    斐波那契数列：0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, ...
    如果设F(n）为该数列的第n项（n∈N*），那么这句话可以写成如下形式：:F(n)=F(n-1)+F(n-2)
    显然这是一个线性递推数列。
 */
public class Fibonacci {

	// 使用递归方法
	//	private static void recursion(int n) {
	//		int j = n;
	//		System.out.println();
	//		System.out.println("斐波那契数列的前" + j + "项为：");
	//		for (int i = 1; i <= j; i++) {
	//			System.out.print(getFibo(i) + "\t");
	//			if (i % 5 == 0)
	//				System.out.println();
	//		}
	//
	//	}

	private static int getFibo(int i) {
		if (i == 1 || i == 2)
			return i - 1;
		else
			return getFibo(i - 1) + getFibo(i - 2);
	}

	// 使用三个变量
	private static void ThreeVariable(int n) {
		int a = 0, b = 1, c = 1;
		int j = n;
		System.out.println();
		System.out.println("斐波那契数列的前" + j + "项为：");
		System.out.print(a + "\t" + b + "\t");
		for (int i = 1; i <= j - 2; i++) {
			c = a + b;
			a = b;
			b = c;
			System.out.print(c + "\t");
			if ((i + 2) % 5 == 0)
				System.out.println();
		}
	}

	@Test
	public void testFibonacci(){
		int n = 0;
		System.out.println(Integer.MAX_VALUE);
		System.out.println(getFibo(n));
		ThreeVariable(n);// 使用数组
		//		recursion(n);// 使用递归
		//		System.out.println(1134903170l+1836311903);
	}

}