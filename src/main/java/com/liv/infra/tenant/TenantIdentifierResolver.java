package com.liv.infra.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

/**
 * Diz ao Hibernate qual é o tenant (empresa) atual. Em conjunto com o campo
 * anotado com {@code @TenantId} nas entidades de domínio, o Hibernate passa a
 * adicionar automaticamente {@code WHERE empresa_id = ?} em toda consulta e a
 * preencher o {@code empresa_id} em toda inserção — isolando os dados por
 * empresa por baixo até do CRUD gerado pelo framework.
 */
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<Object> {

	@Override
	public Object resolveCurrentTenantIdentifier() {
		Long current = TenantContext.get();
		return current != null ? current : TenantContext.NO_TENANT;
	}

	@Override
	public boolean validateExistingCurrentSessions() {
		return false;
	}
}
