package com.akhil.microservices.core.account.services;

import com.akhil.microservices.api.core.account.Account;
import com.akhil.microservices.core.account.persistence.AccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mappings({
            @Mapping(target = "serviceAddress", ignore = true)
    })
    Account entityToApi(AccountEntity entity);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "version", ignore = true)
    })
    AccountEntity apiToEntity(Account api);
}
