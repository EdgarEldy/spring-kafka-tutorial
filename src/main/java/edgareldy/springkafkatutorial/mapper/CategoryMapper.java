package edgareldy.springkafkatutorial.mapper;

import edgareldy.springkafkatutorial.dto.category.CategoryRequest;
import edgareldy.springkafkatutorial.dto.category.CategoryResponse;
import edgareldy.springkafkatutorial.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct mapper converting between {@link Category} and its DTOs. The
 * {@code id} and {@code products} fields are never set from a
 * {@link CategoryRequest}: {@code id} is database-generated, and
 * {@code products} is the reverse side of the relation, populated by
 * {@code Product}, not by a category write.
 * <p>
 * Created edgar.muhamyangabo on 7/7/26
 * Author : edgar.muhamyangabo
 * Date : 7/7/26
 * Project : spring-kafka-tutorial
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    Category toEntity(CategoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    void updateEntityFromRequest(CategoryRequest request, @MappingTarget Category category);
}
