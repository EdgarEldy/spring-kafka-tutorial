package edgareldy.springkafkatutorial.mapper;

import edgareldy.springkafkatutorial.dto.order.OrderRequest;
import edgareldy.springkafkatutorial.dto.order.OrderResponse;
import edgareldy.springkafkatutorial.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper converting between {@link Order} and its DTOs. The
 * {@code customer} and {@code product} associations are resolved by the
 * service layer (which needs repository lookups and a stock check), and
 * {@code total} is always computed there too, so every mapping method that
 * touches an entity ignores those three fields; only {@code quantity} maps
 * directly by name.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.firstName", target = "customerFirstName")
    @Mapping(source = "customer.lastName", target = "customerLastName")
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.productName", target = "productName")
    OrderResponse toResponse(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "total", ignore = true)
    Order toEntity(OrderRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "total", ignore = true)
    void updateEntityFromRequest(OrderRequest request, @MappingTarget Order order);
}
