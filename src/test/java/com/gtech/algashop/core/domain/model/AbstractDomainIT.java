package com.gtech.algashop.core.domain.model;

import com.gtech.algashop.utils.MockJwtDecoderConfig;
import com.gtech.algashop.utils.TestContainerPostgresSQLConfig;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;


@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import({TestContainerPostgresSQLConfig.class, MockJwtDecoderConfig.class}) // importando o gerenciamento de bean do postgres
public class AbstractDomainIT {

}
