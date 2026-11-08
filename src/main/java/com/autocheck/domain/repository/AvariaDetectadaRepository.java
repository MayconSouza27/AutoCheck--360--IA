package com.autocheck.domain.repository;

import com.autocheck.domain.entity.AvariaDetectada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvariaDetectadaRepository extends JpaRepository<AvariaDetectada, Long> {
    List<AvariaDetectada> findByVistoriaId(Long vistoriaId);
}
