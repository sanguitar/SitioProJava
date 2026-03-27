package com.example.demo.Repository;

import com.example.demo.Model.FipeCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FipeCacheRepository extends JpaRepository<FipeCache, Integer> {
}