package org.oiue.service.permission.verify;

import java.util.Date;

import org.junit.Test;
import org.oiue.service.online.JWTUtil;

public class testJWT {
	private String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJuYmYiOjE0OTc0OTA2NzMsImRhdGEiOiJ7XCJnZW5kZXJcIjoyLFwidXNlcl9pZFwiOlwiZm1fc3lzdGVtX3VzZXJfcm9vdFwiLFwiY29ycF9pZFwiOlwiZm1fc3lzdGVtX2NvcnBcIixcInVzZXJfbmFtZVwiOlwicm9vdFwifSIsImV4cCI6NjAwMCwianRpIjoiMGM1ZTIwNDI1ZjU1NGI2NTgyNDA4NmY3ZjE1Y2ZlYTIifQ.uO1sOFykh9CP6jEqzvSBu2dXaN3M6iRCATP6wcmfpAk";
	
	@Test
	public void decode() {
		System.out.println(System.currentTimeMillis());
		System.out.println(new Date(System.currentTimeMillis()));
		System.out.println(new Date(System.currentTimeMillis() / 1000));
		
		System.out.println(JWTUtil.decode(token));
	}
}
