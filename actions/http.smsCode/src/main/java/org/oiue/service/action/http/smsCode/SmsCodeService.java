package org.oiue.service.action.http.smsCode;

import java.io.Serializable;
import java.util.Map;

public interface SmsCodeService extends Serializable {
	Object sendSmsCode(Map data,Map event,String token);
}
