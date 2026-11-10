package com.example.sitiopro.sync;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SyncChangeLogRepository extends JpaRepository<SyncChangeLog, Long> {
    @Query("select c from SyncChangeLog c where c.changeId > :cursor order by c.changeId asc")
    List<SyncChangeLog> buscarDepois(@Param("cursor") long cursor, Pageable pageable);

    @Query("select coalesce(max(c.changeId), 0) from SyncChangeLog c")
    long cursorAtual();
}
