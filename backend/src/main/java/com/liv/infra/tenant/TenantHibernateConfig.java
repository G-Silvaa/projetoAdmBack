package com.liv.infra.tenant;

import java.util.Map;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * Registra o {@link TenantIdentifierResolver} no Hibernate. A presença de um
 * campo {@code @TenantId} nas entidades + este resolver habilita a
 * multi-tenancy por discriminador (uma única base, coluna empresa_id).
 */
@Configuration
public class TenantHibernateConfig implements HibernatePropertiesCustomizer {

	private final TenantIdentifierResolver tenantIdentifierResolver;

	public TenantHibernateConfig(TenantIdentifierResolver tenantIdentifierResolver) {
		this.tenantIdentifierResolver = tenantIdentifierResolver;
	}

	@Override
	public void customize(Map<String, Object> hibernateProperties) {
		hibernateProperties.put("hibernate.tenant_identifier_resolver", tenantIdentifierResolver);
	}
}
