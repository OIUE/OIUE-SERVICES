package com.plugins.geom;

import java.text.DecimalFormat;

/**
 * 描述：<出租车调度系统,数据加密> 编写日期：<2009-04-29>
 */
public class Converter {
	private static final DecimalFormat dfLngLat = new DecimalFormat("###.000000");// 保留6位小数
	double casm_rr;
	long casm_t1;
	long casm_t2;
	double casm_x1;
	double casm_y1;
	double casm_x2;
	double casm_y2;
	double casm_f;
	
	private double yj_sin2(double x) {
		double tt;
		double ss;
		int ff;
		double s2;
		int cc;
		
		ff = 0;
		if (x < 0) {
			x = -x;
			ff = 1;
		}
		cc = (int) (x / 6.28318530717959);
		tt = x - cc * 6.28318530717959;
		if (tt > 3.1415926535897932) {
			tt = tt - 3.1415926535897932;
			if (ff == 1)
				ff = 0;
			else if (ff == 0)
				ff = 1;
		}
		x = tt;
		ss = x;
		s2 = x;
		tt = tt * tt;
		s2 = s2 * tt;
		ss = ss - s2 * 0.166666666666667;
		s2 = s2 * tt;
		ss = ss + s2 * 8.33333333333333E-03;
		s2 = s2 * tt;
		ss = ss - s2 * 1.98412698412698E-04;
		s2 = s2 * tt;
		ss = ss + s2 * 2.75573192239859E-06;
		s2 = s2 * tt;
		ss = ss - s2 * 2.50521083854417E-08;
		if (ff == 1)
			ss = -ss;
		
		return ss;
	}
	
	private double Transform_yj5(double x, double y) {
		double tt;
		
		tt = 300 + 1 * x + 2 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.sqrt(x * x));
		tt = tt + (20 * yj_sin2(18.849555921538764 * x) + 20 * yj_sin2(6.283185307179588 * x)) * 0.6667;
		tt = tt + (20 * yj_sin2(3.141592653589794 * x) + 40 * yj_sin2(1.047197551196598 * x)) * 0.6667;
		tt = tt + (150 * yj_sin2(0.2617993877991495 * x) + 300 * yj_sin2(0.1047197551196598 * x)) * 0.6667;
		
		return tt;
	}
	
	private double Transform_yjy5(double x, double y) {
		double tt;
		
		tt = -100 + 2 * x + 3 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.sqrt(x * x));
		tt = tt + (20 * yj_sin2(18.849555921538764 * x) + 20 * yj_sin2(6.283185307179588 * x)) * 0.6667;
		tt = tt + (20 * yj_sin2(3.141592653589794 * y) + 40 * yj_sin2(1.047197551196598 * y)) * 0.6667;
		tt = tt + (160 * yj_sin2(0.2617993877991495 * y) + 320 * yj_sin2(0.1047197551196598 * y)) * 0.6667;
		
		return tt;
	}
	
	private double Transform_jy5(double x, double xx) {
		double n;
		double a;
		double e;
		
		a = 6378245;
		e = 0.00669342;
		n = Math.sqrt(1 - e * yj_sin2(x * 0.0174532925199433) * yj_sin2(x * 0.0174532925199433));
		n = (xx * 180) / (a / n * Math.cos(x * 0.0174532925199433) * 3.1415926);
		
		return n;
	}
	
	private double Transform_jyj5(double x, double yy) {
		double m;
		double a;
		double e;
		double mm;
		
		a = 6378245;
		e = 0.00669342;
		
		mm = 1 - e * yj_sin2(x * 0.0174532925199433) * yj_sin2(x * 0.0174532925199433);
		m = (a * (1 - e)) / (mm * Math.sqrt(mm));
		
		return (yy * 180) / (m * 3.1415926);
	}
	
	@SuppressWarnings("unused")
	private double r_yj() {
		int casm_a;
		int casm_c;
		casm_a = 314159269;
		casm_c = 453806245;
		return 0;
	}
	
	private double random_yj() {
		int t;
		int casm_a;
		int casm_c;
		casm_a = 314159269;
		casm_c = 453806245;
		casm_rr = casm_a * casm_rr + casm_c;
		t = (int) (casm_rr / 2);
		casm_rr = casm_rr - t * 2;
		casm_rr = casm_rr / 2;
		
		return (casm_rr);
	}
	
	private void IniCasm(long w_time, long w_lng, long w_lat) {
		int tt;
		casm_t1 = w_time;
		casm_t2 = w_time;
		tt = (int) (w_time / 0.357);
		casm_rr = w_time - tt * 0.357;
		if (w_time == 0)
			casm_rr = 0.3;
		casm_x1 = w_lng;
		casm_y1 = w_lat;
		casm_x2 = w_lng;
		casm_y2 = w_lat;
		casm_f = 3;
	}
	
	/**
	 *
	 * @param wg_flag
	 * @param wg_lng
	 * @param wg_lat
	 * @param wg_heit
	 * @param wg_week
	 * @param wg_time
	 * @return If the method succeeds, the return value is the point, otherwise null
	 */
	public Point wgtochina_lb(int wg_flag, long wg_lng, long wg_lat, int wg_heit, int wg_week, long wg_time) {
		double x_add;
		double y_add;
		double h_add;
		double x_l;
		double y_l;
		double casm_v;
		double t1_t2;
		double x1_x2;
		double y1_y2;
		
		Point point = null;
		// wg_heit已经定死在 getEncryPoint(double x, double y)
		// 这个函数为0,所以在这里没有用,留着为以后方便
		if (wg_heit > 5000) {
			return point;
		}
		x_l = wg_lng;
		x_l = x_l / 3686400.0;
		y_l = wg_lat;
		y_l = y_l / 3686400.0;
		
		// 不限制经纬度范围
		// if (x_l < 72.004) {
		// return point;
		// }
		// if (x_l > 137.8347) {
		// return point;
		// }
		// if (y_l < 0.8293) {
		// return point;
		// }
		// if (y_l > 55.8271) {
		// return point;
		// }
		// wg_flag下面这个也没有用在getEncryPoint(double x, double y) 这个函数为1
		if (wg_flag == 0) {
			IniCasm(wg_time, wg_lng, wg_lat);
			
			point = new Point();
			point.setLatitude(wg_lng);
			point.setLongitude(wg_lat);
			
			return point;
		}
		
		casm_t2 = wg_time;
		t1_t2 = (casm_t2 - casm_t1) / 1000.0;
		if (t1_t2 <= 0) {
			casm_t1 = casm_t2;
			casm_f = casm_f + 1;
			casm_x1 = casm_x2;
			casm_f = casm_f + 1;
			casm_y1 = casm_y2;
			casm_f = casm_f + 1;
		} else {
			if (t1_t2 > 120) {
				if (casm_f == 3) {
					casm_f = 0;
					casm_x2 = wg_lng;
					casm_y2 = wg_lat;
					x1_x2 = casm_x2 - casm_x1;
					y1_y2 = casm_y2 - casm_y1;
					casm_v = Math.sqrt(x1_x2 * x1_x2 + y1_y2 * y1_y2) / t1_t2;
					if (casm_v > 3185) {
						return (point);
					}
					
				}
				casm_t1 = casm_t2;
				casm_f = casm_f + 1;
				casm_x1 = casm_x2;
				casm_f = casm_f + 1;
				casm_y1 = casm_y2;
				casm_f = casm_f + 1;
			}
		}
		x_add = Transform_yj5(x_l - 105, y_l - 35);
		y_add = Transform_yjy5(x_l - 105, y_l - 35);
		h_add = wg_heit;
		
		x_add = x_add + h_add * 0.001 + yj_sin2(wg_time * 0.0174532925199433) + random_yj();
		y_add = y_add + h_add * 0.001 + yj_sin2(wg_time * 0.0174532925199433) + random_yj();
		
		point = new Point();
		// point.setLongitude((long)((x_l + Transform_jy5(y_l, x_add)) *
		// 3686400));
		// point.setLatitude((long)((y_l + Transform_jyj5(y_l, y_add)) *
		// 3686400));
		point.setX((long) ((x_l + Transform_jy5(y_l, x_add)) * 3686400));
		point.setY((long) ((y_l + Transform_jyj5(y_l, y_add)) * 3686400));
		
		return (point);
	}
	
	/**
	 * 加密经纬度
	 *
	 * @param x 坐标点的经度
	 * @param y 坐标点的纬度
	 * @return 返回一个point对象
	 */
	public Point getEncryPoint(double x, double y) {
		Point point = new Point();
		
		double x1, tempx;
		double y1, tempy;
		
		x1 = x * 3686400.0;
		y1 = y * 3686400.0;
		
		int gpsWeek = 0;
		double gpsWeekTime = 0;
		
		int gpsHeight = 0;
		point = wgtochina_lb(1, (int) x1, (int) y1, gpsHeight, gpsWeek, (int) gpsWeekTime);
		
		if (point != null) {
			
			tempx = point.getX();
			tempy = point.getY();
			tempx = tempx / 3686400.0;
			tempy = tempy / 3686400.0;
			
			point = new Point();
			point.setX(Double.parseDouble(dfLngLat.format(tempx)));
			point.setY(Double.parseDouble(dfLngLat.format(tempy)));
		}
		
		return point;
	}
	
	/**
	 * 解密经纬度
	 * @param x 经度
	 * @param y 纬度
	 * @return 解密后的经纬度点
	 */
	public Point getDecryptPoint(double x, double y) {
		Point point = new Point();
		
		double x1, tempx;
		double y1, tempy;
		
		x1 = x * 3686400.0;
		y1 = y * 3686400.0;
		
		int gpsWeek = 0;
		double gpsWeekTime = 0;
		
		int gpsHeight = 0;
		point = wgtochina_lb(1, (int) x1, (int) y1, gpsHeight, gpsWeek, (int) gpsWeekTime);
		
		if (point != null) {
			
			tempx = point.getX();
			tempy = point.getY();
			tempx = tempx / 3686400.0;
			tempy = tempy / 3686400.0;
			
			point = new Point();
			point.setX(tempx);
			point.setY(tempy);
		}
		
		double xx = point.getX();
		double yy = point.getY();
		point.setX(Double.parseDouble(dfLngLat.format(2 * x - xx)));
		point.setY(Double.parseDouble(dfLngLat.format(2 * y - yy)));
		return point;
	}
	
	public static void main(String[] args) {
		Converter conver = new Converter();
		// Point point = conver.getEncryPoint(123.521132,41.209556);
		// System.out.println(point.getLatitude() + "," + point.getLongitude());
		// System.out.println(point.getX() + "," + point.getY());
		String xy = "-118.219473,-43.12197,76.10966,16.211155,93.908348,51.614208,126.630929,45.300351,74.924028,49.02108,94.70931,27.40759,124.117264,30.309754,123.521132,41.209556,128.920297,52.020041,99.222947,52.716233,99.616577,53.109085,115.219805,51.230647,85.21005,42.925389";
		String[] a = xy.split(",");
		// Point tp= null;
		for (int i = 0; i < a.length; i = i + 2) {
			// System.out.println("gg:"+a[i]+","+a[i+1]);
			Point tp = conver.getEncryPoint(Double.parseDouble(a[i]), Double.parseDouble(a[i + 1]));
			System.out.println(tp.getX() + "	" + tp.getY());
			
		}
		/*
		 * String xy = " 118.219473 43.12197 118.226134 43.124261 118.226134 43.124261 76.10966 16.211155 76.1124 16.209454 76.1124 16.209455 93.908348 51.614208 93.909999 51.614578 93.909998 51.614578 126.630929 45.300351 126.636802 45.302322 126.636801 45.302322 74.924028 49.02108 74.927087 49.021465 74.927086 49.021464 94.70931 27.40759 94.710179 27.404068 94.710179 27.404069 124.117264 30.309754 124.122078 30.30 123.521132 41.209556 123.526913 41.211523 123.526909 41.211522 128.920297 52.020041 128.92828 52.021484 128.928278 52.021481 99.222947 52.716233 99.224806 52.716919 99.224804 52.716916 99.616577 53.109085 99.618479 53.109832 99.618474 53.109831 115.219805 51.230647 115.22804 51.231908 115.228035 51.231907 85.21005 42.925389 85.212741 42.926802 85.21274 42.926799
		 */
		
		/*
		 * 123.521132,41.209556
		 * 
		 * 123.526913 41.211523 123.526909 41.211522 123.526919 41.211529
		 * 
		 */
		System.out.println("===============");
		Point rp = new Point();
		rp.setX(123.526909);
		rp.setY(34.211523);
		Point p = conver.getEncryPoint(rp.getX(), rp.getY());
		System.out.println(p.getX() + "\t" + p.getY());
		Point p2 = conver.getDecryptPoint(p.getX(), p.getY());
		System.out.println(p2.getX() + "\t" + p2.getY() + "\t" + (p2.getX() - rp.getX()) + "\t" + (p2.getY() - rp.getY()));
		
	}
	
}
