package com.bervan.shstat.queue;

import com.bervan.common.service.JMSService;
import com.bervan.shstat.response.ApiResponse;
import jakarta.jms.JMSException;
import org.apache.activemq.command.ActiveMQObjectMessage;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

@Service
public class QueueService {
    private static final String PRODUCT_PROCESSING_QUEUE = "PRODUCT_PROCESSING_QUEUE";

    private final Set<AbstractQueue<?>> queueProcessors;

    private final JMSService queue;

    public QueueService(Set<AbstractQueue<?>> queueProcessors, JMSService queue) {
        this.queueProcessors = queueProcessors;
        this.queue = queue;
    }

    public ApiResponse addProductsAsync(AddProductsQueueParam products) {
        queue.convertAndSend(PRODUCT_PROCESSING_QUEUE, products, PRODUCT_PROCESSING_QUEUE);
        return new ApiResponse(Collections.singletonList("Processing products in progress..."));
    }

    public ApiResponse refreshMaterializedViews() {
        for (String viewName : RefreshViewQueue.views) {
            queue.convertAndSend(PRODUCT_PROCESSING_QUEUE, new RefreshViewQueueParam(viewName), PRODUCT_PROCESSING_QUEUE);
        }
        //create indexes for category and shop
        return new ApiResponse(Collections.singletonList("Views refreshing in progress..."));
    }

    //    @JmsListener(destination = PRODUCT_PROCESSING_QUEUE)
    public void productProcessingQueue(ActiveMQObjectMessage message) throws JMSException {
        for (AbstractQueue<?> queueProcessor : queueProcessors) {
            Object object = message.getObject();
            if (queueProcessor.supports(object.getClass())) {
                queueProcessor.run((Serializable) object);
            }
        }
    }

    public ApiResponse refreshTableForFavorites() {
        queue.convertAndSend(PRODUCT_PROCESSING_QUEUE, new RefreshFavoritesViewsQueueParam(), PRODUCT_PROCESSING_QUEUE);
        return new ApiResponse(Collections.singletonList("Favorites Views refreshing in progress..."));
    }
}
