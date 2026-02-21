package com.gtech.algashop.domain.model.entity.VO;

import com.gtech.algashop.domain.model.entity.VO.Document;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class DocumentTest {

    @Test
    void shouldCreateValidDocument() {
        Document document = new Document("255-08-0578");
        Assertions.assertThat(document.document()).isEqualTo("255-08-0578");
    }

    @Test
    void shouldNotAllowNullDocument() {
        Assertions.assertThatThrownBy(() -> new Document(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotAllowBlankDocument() {
        Assertions.assertThatThrownBy(() -> new Document("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnDocumentAsString() {
        Document document = new Document("255-08-0578");
        Assertions.assertThat(document.toString()).hasToString("255-08-0578");
    }
}