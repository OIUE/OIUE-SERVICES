package org.oiue.service.action.http.resource;

public class exp2 {
	// public static void main(String args[]) {
	// int i = 0;
	// math mymath = new math();
	// for (i = 100; i <= 999; i++)
	// if (mymath.shuixianhua(i) == true)
	// System.out.println(i);
	// }

	public exp2() {
	}

	public void fengjie(int n) {
		// System.out.println(">>>>>"+n);
		for (int i = 2; i <= n / 2; i++) {
			if (n % i == 0) {
				System.out.print(i + "*");
				fengjie(n / i);
				return;
			}
		}
	}

	public void wanshu() {
		int s;
		for (int i = 1; i <= 1000; i++) {
			s = 0;
			for (int j = 1; j < i; j++)
				if (i % j == 0)
					s = s + j;
			if (s == i)
				System.out.print(i + " ");
		}
		System.out.println();
	}

	public static void main(String[] args) {
		exp2 c = new exp2();
		int N;
		N = 300;
		System.out.print(N + "分解质因数：" + N + "=");
		c.fengjie(N);
	}
}

class math {
	public int f(int x) {
		if (x == 1 || x == 2)
			return 1;
		else
			return f(x - 1) + f(x - 2);
	}

	public boolean iszhishu(int x) {
		for (int i = 2; i <= x / 2; i++)
			if (x % i == 0)
				return false;
		return true;
	}

	public boolean shuixianhua(int x) {
		int i = 0, j = 0, k = 0;
		i = x / 100;
		j = (x % 100) / 10;
		k = x % 10;
		if (x == i * i * i + j * j * j + k * k * k)
			return true;
		else
			return false;
	}
}