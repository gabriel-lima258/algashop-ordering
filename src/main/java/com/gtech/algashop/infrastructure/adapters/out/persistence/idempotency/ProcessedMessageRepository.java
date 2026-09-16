package com.gtech.algashop.infrastructure.adapters.out.persistence.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, UUID> {

    @Modifying
    @Query(value = """
			delete from processed_message
			 where idempotency_key in (
				   select idempotency_key
					 from processed_message
					where processed_at < :threshold
					order by processed_at
					limit :batchSize
			 )
			""", nativeQuery = true)
    Integer deleteBatchOlderThan(@Param("threshold") OffsetDateTime threshold,
                             @Param("batchSize") int batchSize);
}
