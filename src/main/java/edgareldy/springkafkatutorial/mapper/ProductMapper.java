package edgareldy.springkafkatutorial.mapper;

import edgareldy.springkafkatutorial.dto.product.ProductRequest;
import edgareldy.springkafkatutorial.dto.product.ProductResponse;
import edgareldy.springkafkatutorial.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper converting between {@link Product} and its DTOs. The
 * {@code category} association itself is resolved by the service layer
 * (which needs a repository lookup), not by this mapper: every mapping
 * method that touches an entity ignores the {@code category} field.
 * {@code stockQuantity} maps by name like every other scalar field; the
 * decrement logic triggered by Kafka lives in {@code StockService}
 * (feature/messaging), not here.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.categoryName", target = "categoryName")
    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateEntityFromRequest(ProductRequest request, @MappingTarget Product product);
}
