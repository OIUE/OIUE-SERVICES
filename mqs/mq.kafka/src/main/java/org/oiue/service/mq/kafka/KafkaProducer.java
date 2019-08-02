package org.oiue.service.mq.kafka;

import java.util.Properties;

import org.oiue.service.log.Logger;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

@SuppressWarnings("rawtypes")
public class KafkaProducer implements DefaultProducer{

	private Properties prop;
	private Logger logger;
	private Producer producer;
	
	KafkaProducer(Logger logger){
		this.logger = logger;
	}
	
	@Override
	public Producer<?, ?> createProducer(Properties propT) {
		prop = propT;
		if(prop == null || prop.isEmpty()){
			logger.error("the Properties is empty, return null");
			return null;
		}
		doStop();
		producer = new Producer<Integer, String>(new ProducerConfig(prop));
		return producer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doSend(String topic, String msg){
		if(producer == null){
			logger.error("producer is null, please check the config");
			throw new RuntimeException("producer is null, please check the config");
		}else{
			producer.send(new KeyedMessage<Integer, String>(topic, msg));
		}
	}
	
	@Override
	public void doStop() {
		if(producer != null){
			producer.close();
		}
		producer = null;
	}
}
