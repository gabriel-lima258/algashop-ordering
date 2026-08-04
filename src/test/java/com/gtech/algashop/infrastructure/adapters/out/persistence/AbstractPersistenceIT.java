package com.gtech.algashop.infrastructure.adapters.out.persistence;

import com.gtech.algashop.infrastructure.config.auditing.SpringDataAuditingConfig;
import com.gtech.algashop.infrastructure.adapters.in.web.utils.TestContainerPostgresSQLConfig;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@Import({TestContainerPostgresSQLConfig.class, SpringDataAuditingConfig.class}) // importando o gerenciamento de bean do postgres
public class AbstractPersistenceIT {

}
