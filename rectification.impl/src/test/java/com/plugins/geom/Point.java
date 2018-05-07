package com.plugins.geom;

/**
 * 描述：<出租车调度系统,数据加密point实体类>
 */

public class Point {
	private long longitude;
	private long latitude;
	private double x;
	private double y;
	
	/**
	 * 设置以度为单位的经度
	 * @param x 以度为单位的经度
	 */
	public void setX(double x) {
		this.x = x;
	}
	
	/**
	 * 设置以度为单位的纬度
	 * @param y 以度为单位的纬度
	 */
	public void setY(double y) {
		this.y = y;
	}
	
	/**
	 * 获得以度为单位的经度
	 * @return 以度为单位的经度
	 */
	public double getX() {
		return x;
	}
	
	/**
	 * 获得以度为单位的纬度
	 * @return 以度为单位的纬度
	 */
	public double getY() {
		return y;
	}
	
	public void setLongitude(long longitude) {
		this.longitude = longitude;
	}
	
	public void setLatitude(long latitude) {
		this.latitude = latitude;
	}
	
	public long getLongitude() {
		return longitude;
	}
	
	public long getLatitude() {
		return latitude;
	}
}
