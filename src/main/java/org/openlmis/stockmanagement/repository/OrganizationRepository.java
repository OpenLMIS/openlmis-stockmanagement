package org.openlmis.stockmanagement.repository;

import org.openlmis.stockmanagement.domain.movement.Organization;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface OrganizationRepository extends
        PagingAndSortingRepository<Organization, UUID> {
}
