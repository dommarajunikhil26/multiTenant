package com.nikhil.multitenant.repository;

import com.nikhil.multitenant.model.Tenant;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TenantRepository extends CrudRepository<Tenant, UUID> {
}
