package com.nikhil.multitenant.context;

import java.util.UUID;

public class TenantContext {
    private static final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();
    public static void setTenantId(UUID tenantId) {
        currentTenant.set(tenantId);
    }
    public static UUID getTenantId() {
        return currentTenant.get();
    }
    public static void removeTenantId() {
        currentTenant.remove();
    }
}
