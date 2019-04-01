package org.oiue.service.mq.kafka;

import java.util.Map;
import java.util.Properties;

import org.oiue.service.log.LogService;
import org.oiue.service.log.Logger;
import org.oiue.service.mq.Handler;
import org.oiue.service.mq.MQService;

import kafka.serializer.StringEncoder;

public class KafkaServiceImpl implements MQService {
	private static final long serialVersionUID = 3175159751279658689L;
	private Logger logger = null;
	private DefaultProducer defaultProducer;
	private DefaultConsumer defaultConsumer;
	
	public KafkaServiceImpl(LogService logService) {
		logger = logService.getLogger(this.getClass());
		defaultConsumer = new KafkaConsumer(logger);
		defaultProducer = new KafkaProducer(logger);
	}
	
	@SuppressWarnings("rawtypes")
	public void updateConfigure(Map dict) {
		logger.info("update configure, properties = " + dict);
		Properties properties = new Properties();
		String zookeeperConnect = (String)dict.get("zookeeper.connect");
		String metadataBrokerList = (String)dict.get("metadata.broker.list");
		properties.put("zookeeper.connect", zookeeperConnect);
		properties.put("metadata.broker.list", metadataBrokerList);
		StringEncoder stringEncoder = new StringEncoder(null);
        properties.put("serializer.class", stringEncoder.getClass().getName());
        defaultProducer.createProducer(properties);
        
        Properties ConsumerProperties = new Properties();
        ConsumerProperties.put("zookeeper.connect", zookeeperConnect);
        String zookeeperSessionTimeout = (String) dict.get("zookeeper.session.timeout.ms");
        String zookeeperSyncTime = (String) dict.get("zookeeper.sync.time.ms");
        String autoCommitInterval = (String) dict.get("auto.commit.interval.ms");
        String autoOffsetReset = (String) dict.get("auto.offset.reset");
        String groupId = (String) dict.get("group.id");
        String serializerClass = (String) dict.get("serializer.class");
        String topicCount = (String) dict.get("topicCount");
        String threadPoolSize = (String) dict.get("threadPoolSize");
        if(topicCount == null || topicCount.isEmpty()){
        	topicCount = "2";
        }
        if(threadPoolSize == null || threadPoolSize.isEmpty()){
        	threadPoolSize = topicCount;
        }
        ConsumerProperties.put("zookeeper.session.timeout.ms", zookeeperSessionTimeout);
        ConsumerProperties.put("zookeeper.sync.time.ms", zookeeperSyncTime);
        ConsumerProperties.put("auto.commit.interval.ms", autoCommitInterval);
        ConsumerProperties.put("auto.offset.reset", autoOffsetReset);
        ConsumerProperties.put("group.id", groupId);
        ConsumerProperties.put("serializer.class", serializerClass);
        ConsumerProperties.put("topicCount", topicCount);
        ConsumerProperties.put("threadPoolSize", threadPoolSize);
        defaultConsumer.createConsumer(ConsumerProperties);
	}
	
	@Override
	public void send(String topic, String message) {
		defaultProducer.doSend(topic, message);
	}
	
	@Override
	public void registerConsumerListener(String topic, Handler consumerListener){
		defaultConsumer.registerConsumerListener(topic, consumerListener);
	}
	
	@Override
	public void unregisterConsumerListener(String topic){
		defaultConsumer.unregisterConsumerListener(topic);
	}
}
