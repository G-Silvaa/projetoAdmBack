package com.liv.infra.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.liv.domain.Cobranca;

public interface CobrancaRepository extends JpaRepository<Cobranca, Long> {

	Optional<Cobranca> findByIdAndEmpresaId(Long id, Long empresaId);

	Optional<Cobranca> findByProviderId(String providerId);
}
