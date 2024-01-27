package com.shstat;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ShoppingStatsServerApplication {

	@Value("${spring.activemq.broker-url}")
	private String brokerUrl;

	public static void main(String[] args) {
		SpringApplication.run(ShoppingStatsServerApplication.class, args);
	}

	@Bean
	public ConnectionFactory jmsConnectionFactory() {
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
		ActiveMQPrefetchPolicy policy = new ActiveMQPrefetchPolicy();
		policy.setQueuePrefetch(1);
		factory.setPrefetchPolicy(policy);
		factory.setTrustAllPackages(true);

		return factory;
	}
}
