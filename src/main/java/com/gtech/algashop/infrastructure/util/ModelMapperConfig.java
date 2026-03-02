package com.gtech.algashop.infrastructure.util;

import com.gtech.algashop.application.customer.query.CustomerOutput;
import com.gtech.algashop.application.order.query.OrderDetailOutput;
import com.gtech.algashop.application.order.query.OrderItemOutput;
import com.gtech.algashop.application.shoppingcart.query.ShoppingCartItemOutput;
import com.gtech.algashop.application.util.Mapper;
import com.gtech.algashop.domain.model.commons.FullName;
import com.gtech.algashop.domain.model.costumer.BirthDate;
import com.gtech.algashop.domain.model.costumer.Customer;
import com.gtech.algashop.infrastructure.persistence.order.OrderItemPersistenceEntity;
import com.gtech.algashop.infrastructure.persistence.order.OrderPersistenceEntity;
import com.gtech.algashop.infrastructure.persistence.shoppingcart.ShoppingCartItemPersistenceEntity;
import io.hypersistence.tsid.TSID;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.convention.NamingConventions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class ModelMapperConfig {

    // conversões personalizadas de values objects para objetos
    private static final Converter<FullName, String> fullNameToFirstNameConverter =
            mappingContext -> {
                FullName fullName = mappingContext.getSource();
                if (fullName == null) {
                    return null;
                }
                return fullName.firstName();
            };

    private static final Converter<FullName, String> fullNameToLastNameConverter =
            mappingContext -> {
                FullName fullName = mappingContext.getSource();
                if (fullName == null) {
                    return null;
                }
                return fullName.lastName();
            };

    private static final Converter<BirthDate, LocalDate> birthDateToLocalDateConverter =
            mappingContext -> {
                BirthDate birthDate = mappingContext.getSource();
                if (birthDate == null) {
                    return null;
                }
                return birthDate.birthDate();
            };

    private static final Converter<Long, String> longToStringTSIDConverter =
            mappingContext -> {
                Long longTSID = mappingContext.getSource();
                if (longTSID == null) {
                    return null;
                }
                return new TSID(longTSID).toString();
            };

    // instanciamos a implementação da interface Mapper na camada do infra
    @Bean
    public Mapper mapper() {
        ModelMapper modelMapper = new ModelMapper();

        configuration(modelMapper);

        return new Mapper() {
            @Override
            public <T> T convert(Object object, Class<T> destinationType) {
                return modelMapper.map(object, destinationType);
            }
        };
    }

    // configurando o mapper para entender a mapear o objeto sem precisar ser exatamente como
    // o padrão java Bean como get e setter, strategy indica como deve ser mapeado os objetos
    // ou seja, STRICT indica que o nome dos dois DEVEM ser iguais
    private void configuration(ModelMapper modelMapper) {
        modelMapper.getConfiguration()
                .setSourceNamingConvention(NamingConventions.NONE)
                .setDestinationNamingConvention(NamingConventions.NONE)
                .setMatchingStrategy(MatchingStrategies.STRICT);

        // criando um mapper personalizado de values
        modelMapper.createTypeMap(Customer.class, CustomerOutput.class)
                .addMappings(mapping ->
                        mapping.using(fullNameToFirstNameConverter).map(Customer::fullName, CustomerOutput::setFirstName))
                .addMappings(mapping ->
                        mapping.using(fullNameToLastNameConverter).map(Customer::fullName, CustomerOutput::setLastName))
                .addMappings(mapping ->
                        mapping.using(birthDateToLocalDateConverter).map(Customer::birthDate, CustomerOutput::setBirthDate));

        // mapper para mapear order id de Long de TSID para String
        modelMapper.createTypeMap(OrderPersistenceEntity.class, OrderDetailOutput.class)
                .addMappings(mapping ->
                        mapping.using(longToStringTSIDConverter).map(OrderPersistenceEntity::getId, OrderDetailOutput::setId)
                );

        // mapper para mapear o order item de Long de TSID para String
        modelMapper.createTypeMap(OrderItemPersistenceEntity.class, OrderItemOutput.class)
                .addMappings(mapping ->
                        mapping.using(longToStringTSIDConverter).map(OrderItemPersistenceEntity::getId, OrderItemOutput::setId)
                )
                .addMappings(mapping ->
                        mapping.using(longToStringTSIDConverter).map(OrderItemPersistenceEntity::getOrderId, OrderItemOutput::setOrderId)
                );

        // mapper para mapear o shopping cart item de Long de TSID para String e productName para name
        modelMapper.createTypeMap(ShoppingCartItemPersistenceEntity.class, ShoppingCartItemOutput.class)
                .addMappings(mapping ->
                        mapping.using(longToStringTSIDConverter).map(ShoppingCartItemPersistenceEntity::getId, ShoppingCartItemOutput::setId)
                )
                .addMappings(mapping ->
                        mapping.map(ShoppingCartItemPersistenceEntity::getProductName, ShoppingCartItemOutput::setName)
                );
    }
}
