package com.gtech.algashop.core.domain.model;

import com.gtech.algashop.infrastructure.adapters.in.web.utils.TestContainerPostgresSQLConfig;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;


@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(TestContainerPostgresSQLConfig.class) // importando o gerenciamento de bean do postgres
public class AbstractDomainIT {

}
