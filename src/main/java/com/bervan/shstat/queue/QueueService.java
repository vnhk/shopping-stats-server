package com.bervan.shstat.queue;

import com.bervan.common.service.ApiKeyService;
import com.bervan.shstat.ScrapContext;
import com.bervan.shstat.response.ApiResponse;
import com.rabbitmq.client.Channel;
import org.jboss.logging.Logger;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

@Service
public class QueueService {
    Logger logger = Logger.getLogger(QueueService.class);
    private final Set<AbstractQueue<?>> queueProcessors;

    private final Jackson2JsonMessageConverter messageConverter;
    private final ApiKeyService apiKeyService;

    public QueueService(Jackson2JsonMessageConverter messageConverter, Set<AbstractQueue<?>> queueProcessors, ApiKeyService apiKeyService) {
        this.queueProcessors = queueProcessors;
        this.messageConverter = messageConverter;
        this.apiKeyService = apiKeyService;
    }

    @Autowired
    private AmqpTemplate amqpTemplate;

    public ApiResponse sendProductMessage(QueueMessage productMessage) {
        amqpTemplate.convertAndSend("DIRECT_EXCHANGE", "PRODUCTS_ROUTING_KEY", productMessage);
        return new ApiResponse(Collections.singletonList("Processing product message in progress..."));
    }

    public void addScrapingToQueue(ScrapContext scrapContext) {
        amqpTemplate.convertAndSend("SCRAPER_DIRECT_EXCHANGE", "SCRAPER_ROUTING_KEY", scrapContext);
    }

    @RabbitListener(queues = "PRODUCTS_QUEUE", ackMode = "MANUAL")
    public void receiveProductMessage(Message message, Channel channel) throws IOException {
        QueueMessage queueMessage = (QueueMessage) messageConverter.fromMessage(message);
        boolean ackEarly = "RefreshViewQueueParam".equals(queueMessage.getSupportClassName());

        try {
            if (queueMessage.getApiKey() == null || queueMessage.getApiKey().isBlank() ||
                    apiKeyService.getUserByAPIKey(queueMessage.getApiKey()) == null) {
                logger.error("NOT_API_KEY for PRODUCTS_QUEUE message");
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }

            if (ackEarly) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }

            for (AbstractQueue<?> queueProcessor : queueProcessors) {
                if (queueProcessor.supports(queueMessage.getSupportClassName())) {
                    queueProcessor.run(queueMessage.getBody());
                }
            }

            if (!ackEarly) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }

        } catch (Exception e) {
            logger.error(e);
        }
    }

//    public ApiResponse refreshMaterializedViews() {
//        for (String viewName : RefreshViewQueue.views) {
//            queue.convertAndSend(PRODUCT_PROCESSING_QUEUE, new RefreshViewQueueParam(viewName), PRODUCT_PROCESSING_QUEUE);
//        }
//        //create indexes for category and shop
//        return new ApiResponse(Collections.singletonList("Views refreshing in progress..."));
//    }


//    public ApiResponse refreshTableForFavorites() {
//        queue.convertAndSend(PRODUCT_PROCESSING_QUEUE, new RefreshFavoritesViewsQueueParam(), PRODUCT_PROCESSING_QUEUE);
//        return new ApiResponse(Collections.singletonList("Favorites Views refreshing in progress..."));
//    }
//}
}
