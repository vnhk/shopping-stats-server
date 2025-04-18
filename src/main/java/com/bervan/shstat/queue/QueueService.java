package com.bervan.shstat.queue;

import com.bervan.common.service.ApiKeyService;
import com.bervan.shstat.response.ApiResponse;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
public class QueueService {
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

    @RabbitListener(queues = "PRODUCTS_QUEUE")
    public void receiveProductMessage(Message message) {
        QueueMessage queueMessage = (QueueMessage) messageConverter.fromMessage(message);
        if (apiKeyService.getUserByAPIKey(queueMessage.getApiKey()) == null) {
            System.err.println("NOT_API_KEY");
            return;
        }
        for (AbstractQueue<?> queueProcessor : queueProcessors) {
            if (queueProcessor.supports(queueMessage.getSupportClassName())) {
                queueProcessor.run(queueMessage.getBody());
            }
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
