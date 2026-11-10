package com.example.sitiopro.sync;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApiIdempotencyRepository extends JpaRepository<ApiIdempotency, Long> {
    Optional<ApiIdempotency> findByUsuarioIdAndDeviceIdAndOperationId(
            Long usuarioId, UUID deviceId, UUID operationId);
}
