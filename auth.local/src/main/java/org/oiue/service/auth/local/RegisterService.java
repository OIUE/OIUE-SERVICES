package org.oiue.service.auth.local;

import java.io.Serializable;
import java.util.Map;

public interface RegisterService extends Serializable {
	Object register(Map data, Map event, String tokenid);
}
