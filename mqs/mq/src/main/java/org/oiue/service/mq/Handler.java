package org.oiue.service.mq;

import java.io.Serializable;

public interface Handler extends Serializable {
	public void receive(String topic, String message);
}
