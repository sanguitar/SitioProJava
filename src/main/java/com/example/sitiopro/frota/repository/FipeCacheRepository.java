package com.example.sitiopro.frota.repository;

import com.example.sitiopro.frota.model.FipeCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FipeCacheRepository extends JpaRepository<FipeCache, Integer> {
}
