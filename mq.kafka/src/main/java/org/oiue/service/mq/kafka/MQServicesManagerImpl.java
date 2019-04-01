package org.oiue.service.mq.kafka;

import org.oiue.service.mq.Handler;
import org.oiue.service.mq.MQService;
import org.oiue.service.mq.MQServicesManager;

public class MQServicesManagerImpl implements MQServicesManager {
	private static final long serialVersionUID = 1L;

	@Override
	public void send(String topic, String message) {
		
	}

	@Override
	public void registerConsumerListener(String topic, Handler consumerListener) {
		
	}

	@Override
	public void unregisterConsumerListener(String topic) {
		
	}

	@Override
	public boolean registerMQService(String name, MQService mqService) {
		return false;
	}

	@Override
	public MQService getMQService(String name) {
		return null;
	}

	@Override
	public MQService getMQService() {
		return null;
	}
	
}