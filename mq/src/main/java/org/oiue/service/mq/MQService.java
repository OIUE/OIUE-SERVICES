package org.oiue.service.mq;

import java.io.Serializable;

public interface MQService extends Serializable {
	
	void send(String topic, String message);
	
	void registerConsumerListener(String topic, Handler consumerListener);
	
	void unregisterConsumerListener(String topic);
}
