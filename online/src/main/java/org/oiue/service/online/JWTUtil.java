package org.oiue.service.online;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;

import org.oiue.tools.StatusResult;
import org.oiue.tools.exception.OIUEException;
import org.oiue.tools.json.JSONUtil;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

public class JWTUtil {
	static String key = "secret";
	
	public static Map decode(String token) {
		JWTVerifier verifier;
		try {
			// DecodedJWT jwt = JWT.decode(token);
			verifier = JWT.require(Algorithm.HMAC256(key)).build();
			DecodedJWT jwt = verifier.verify(token);
			String dataStr = jwt.getClaim("data").asString();
			return JSONUtil.parserStrToMap(dataStr);
		} catch (TokenExpiredException e) {
			throw new OIUEException(StatusResult._pleaseReLogin, "登录超时，请重新登录！");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String decodeTokenId(String token) {
		JWTVerifier verifier;
		try {
			verifier = JWT.require(Algorithm.HMAC256(key)).build();
			DecodedJWT jwt = verifier.verify(token);
			return jwt.getId();
		} catch (TokenExpiredException e) {
			throw new OIUEException(StatusResult._pleaseReLogin, "登录超时，请重新登录！");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String encode(String tokenId, Date notBefore, Date expires, Map data) {
		Algorithm algorithm;
		String token = null;
		try {
			algorithm = Algorithm.HMAC256(key);
			String dataStr = data==null||data.size()==0?"":JSONUtil.parserToStr(data);
			token = JWT.create().withJWTId(tokenId).withNotBefore(notBefore).withExpiresAt(expires).withClaim("data", dataStr).sign(algorithm);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return token;
	}
}
