package com.liv.infra.tenant;

/**
 * Guarda o tenant (empresa) da requisição atual em um ThreadLocal.
 * É preenchido pelo AuthFilter a partir do usuário autenticado e lido pelo
 * Hibernate (via TenantIdentifierResolver) para isolar os dados.
 */
public final class TenantContext {

	/** Tenant "vazio" usado fora de uma requisição autenticada. Nenhuma empresa tem id 0. */
	public static final Long NO_TENANT = 0L;

	private static final ThreadLocal<Long> CURRENT = new ThreadLocal<>();

	private TenantContext() {
	}

	public static void set(Long empresaId) {
		CURRENT.set(empresaId);
	}

	public static Long get() {
		return CURRENT.get();
	}

	public static void clear() {
		CURRENT.remove();
	}
}
