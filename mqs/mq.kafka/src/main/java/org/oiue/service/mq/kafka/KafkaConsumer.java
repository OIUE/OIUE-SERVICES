package org.oiue.service.mq.kafka;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.oiue.service.log.Logger;
import org.oiue.service.mq.Handler;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;

public class KafkaConsumer implements DefaultConsumer {
	private Properties prop;
	private Logger logger;
	private ConsumerConnector consumer;
	private static Map<String, Handler> listenerMap = new HashMap<>();
	private static Map<String, ExecutorService> threadMap = new HashMap<String, ExecutorService>();
	int topicCount = 2;
	int threadPoolSize = 2;
	
	KafkaConsumer(Logger logger){
		this.logger = logger;
	}

	@Override
	public ConsumerConnector createConsumer(Properties propT) {
		String topicCountStr = (String) propT.get("topicCount");
        String threadPoolSizeStr = (String) propT.get("threadPoolSize");
        propT.remove("topicCount");
        propT.remove("threadPoolSize");
		prop = propT;
		if(prop == null || prop.isEmpty()){
			logger.error("the properties is empty, return null");
			return null;
		}
		doStop();
		ConsumerConfig consumerConfig = new ConsumerConfig(prop);
		consumer = kafka.consumer.Consumer.createJavaConsumerConnector(consumerConfig);
		if(topicCountStr != null && !topicCountStr.isEmpty()){
			topicCount = Integer.parseInt(topicCountStr);
		}
		if(threadPoolSizeStr != null && !threadPoolSizeStr.isEmpty()){
			threadPoolSize = Integer.parseInt(threadPoolSizeStr);
		}
		doGet(topicCount, threadPoolSize);
		return consumer;
	}

	@Override
	public void doStop() {
		stopAllThread();
		if(consumer != null){
			consumer.shutdown();
		}
	}
	
	public void doGet(int topicCount, int threadPoolSize) {
		for(String topic : listenerMap.keySet()){
			doGet(topicCount,threadPoolSize,topic);
		}
	}

	public void doGet(int topicCount, int threadPoolSize, String topic) {
		ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
		Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, topicCount);
        StringDecoder keyDecoder = new StringDecoder(new VerifiableProperties());
        StringDecoder valueDecoder = new StringDecoder(new VerifiableProperties());
        Map<String, List<KafkaStream<String, String>>> consumerMap = consumer.createMessageStreams(topicCountMap, keyDecoder, valueDecoder);
        List<KafkaStream<String, String>> streams = consumerMap.get(topic);
        for (final KafkaStream<String, String> stream : streams) {
        	AutoCommitConsumerTask autoCommitConsumerTask = new AutoCommitConsumerTask(topic, stream);
        	executor.submit(autoCommitConsumerTask);
        	threadMap.put(topic, executor);
        }
	}
	
	@Override
	public void registerConsumerListener(String topic, Handler consumerListener) {
		listenerMap.put(topic, consumerListener);
		if(threadMap != null && !threadMap.isEmpty() && threadMap.get(topic) == null){
			doGet(topicCount, threadPoolSize, topic);
		}
	}

	@Override
	public void unregisterConsumerListener(String topic) {
		if(listenerMap != null && !listenerMap.isEmpty()){
			listenerMap.remove(topic);
		}
		stopThread(topic);
	}
	
	public void stopThread(String topic){
		if(threadMap != null && !threadMap.isEmpty()){
			ExecutorService thread = threadMap.get(topic);
			if(thread != null){
				thread.shutdown();
				threadMap.remove(topic);
			}
		}
	}
	
	public void stopAllThread(){
		if(threadMap != null && !threadMap.isEmpty()){
			for(String topic : threadMap.keySet()){
				stopThread(topic);
			}
		}
	}
	
	class AutoCommitConsumerTask implements Runnable {
        private final KafkaStream<String, String> stream;
        private final String topic;
        
        public AutoCommitConsumerTask(String topic, KafkaStream<String, String> stream) {
            this.stream = stream;
            this.topic = topic;
        }
        
        public void work(){
        	ConsumerIterator<String, String> it = stream.iterator();
        	 while (it.hasNext()) {
                 MessageAndMetadata<String, String> mm = it.next();
                 listenerMap.get(topic).receive(topic, mm.message());
             }
        	 consumer.commitOffsets();
        }
        
        public void run() {
			work();
        }
    }

}
