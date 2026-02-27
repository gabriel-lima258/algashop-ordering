package com.gtech.algashop.domain.model;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochRandomGenerator;
import io.hypersistence.tsid.TSID;

import java.util.UUID;

// classe util para gerar UUID V7
public class IdGenerator {

    private static final TimeBasedEpochRandomGenerator timeBasedEpochRandomGenerator
            = Generators.timeBasedEpochRandomGenerator();

    private static final TSID.Factory tsidFactory = TSID.Factory.INSTANCE;

    private IdGenerator() {
    }

    public static UUID generateTimeBasedUUID() {
        return timeBasedEpochRandomGenerator.generate();
    }

    /*
    * Em produção é preciso subir as seguintes variaveis de ambiente
    * TSID_NODE = numero do micro serviço = 0,1..
    * TSID_NODE_COUNT = numero de instancias = 1,2,3...
    */
    public static TSID generateTSID() {
        return tsidFactory.generate();
    }
}
