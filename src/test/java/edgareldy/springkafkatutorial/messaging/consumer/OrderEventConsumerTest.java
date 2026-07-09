package edgareldy.springkafkatutorial.messaging.consumer;

import static org.mockito.Mockito.verify;

import edgareldy.springkafkatutorial.dto.event.OrderCreatedEvent;
import edgareldy.springkafkatutorial.service.StockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit test for {@link OrderEventConsumer}, with {@link StockService}
 * mocked: verifies the listener delegates to the service instead of
 * touching a repository directly.
 * <p>
 * Created edgar.muhamyangabo on 7/8/26
 * Author : edgar.muhamyangabo
 * Date : 7/8/26
 * Project : spring-kafka-tutorial
 */
@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock
    private StockService stockService;

    @InjectMocks
    private OrderEventConsumer orderEventConsumer;

    @Test
    void consumeDelegatesToStockService() {
        OrderCreatedEvent event = new OrderCreatedEvent(1L, 2L, 3);

        orderEventConsumer.consume(event);

        verify(stockService).decrementStock(2L, 3);
    }
}
