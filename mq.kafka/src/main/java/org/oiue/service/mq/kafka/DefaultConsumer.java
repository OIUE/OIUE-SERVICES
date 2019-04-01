package org.oiue.service.mq.kafka;

import java.util.Properties;

import org.oiue.service.mq.Handler;

import kafka.javaapi.consumer.ConsumerConnector;

public interface DefaultConsumer {
	
	public ConsumerConnector createConsumer(Properties prop);
	
	public void doStop();
	
	public void registerConsumerListener(String topic, Handler consumerListener);

	public void unregisterConsumerListener(String topic);
}
