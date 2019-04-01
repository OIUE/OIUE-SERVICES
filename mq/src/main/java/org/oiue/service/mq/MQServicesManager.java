package org.oiue.service.mq;

public interface MQServicesManager extends MQService {
	boolean registerMQService(String name,MQService mqService);
	
	MQService getMQService(String name);
	
	MQService getMQService();
	
}