package org.oiue.service.mq.kafka;

import java.util.Properties;

import kafka.javaapi.producer.Producer;

public interface DefaultProducer {
	
	@SuppressWarnings("rawtypes")
	public Producer createProducer(Properties prop);
	
	public void doStop();
	
	public void doSend(String topic, String msg);
}
