package com.example.sitiopro.shared.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

    @CreatedDate
    @Column(name = "criado_em")
    private LocalDateTime criadoEm;

    @CreatedBy
    @Column(name = "criado_por", length = 100)
    private String criadoPor;

    @LastModifiedDate
    @Column(name = "alterado_em")
    private LocalDateTime alteradoEm;

    @LastModifiedBy
    @Column(name = "alterado_por", length = 100)
    private String alteradoPor;

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public String getCriadoPor() {
        return criadoPor;
    }

    public LocalDateTime getAlteradoEm() {
        return alteradoEm;
    }

    public String getAlteradoPor() {
        return alteradoPor;
    }
}
