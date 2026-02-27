package com.gtech.algashop.infrastructure.util;

import com.gtech.algashop.application.customer.management.CustomerOutput;
import com.gtech.algashop.application.util.Mapper;
import com.gtech.algashop.domain.model.commons.FullName;
import com.gtech.algashop.domain.model.costumer.BirthDate;
import com.gtech.algashop.domain.model.costumer.Customer;
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
    }
}
