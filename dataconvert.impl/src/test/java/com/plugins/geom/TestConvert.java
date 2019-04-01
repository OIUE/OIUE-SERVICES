package com.plugins.geom;

import org.junit.Test;

public class TestConvert {
	
	@Test
	public void test() {
		
		double lng = 116.561194;
		
		double lat = 39.762089;
		
		Converter conver = new Converter();
		
		Point p = conver.getEncryPoint(lng, lat);
		
		String lnglat = p.getX() + "," + p.getY();
		System.out.println(lnglat);
	}
}
