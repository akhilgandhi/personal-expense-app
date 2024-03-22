package com.akhil.microservices.core.expense.services;

import com.akhil.microservices.api.core.expense.Expense;
import com.akhil.microservices.core.expense.persistence.ExpenseEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true)
    })
    Expense entityToApi(ExpenseEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    ExpenseEntity apiToEntity(Expense api);

    List<Expense> entityListToApiList(List<ExpenseEntity> entity);

    List<ExpenseEntity> apiListToEntityList(List<Expense> api);
}
