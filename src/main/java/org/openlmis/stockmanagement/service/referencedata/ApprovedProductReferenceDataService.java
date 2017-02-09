package org.openlmis.stockmanagement.service.referencedata;

import org.openlmis.stockmanagement.dto.ApprovedProductDto;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ApprovedProductReferenceDataService extends
    BaseReferenceDataService<ApprovedProductDto> {
  @Override
  protected String getUrl() {
    return "/api/facilities/";
  }

  @Override
  protected Class<ApprovedProductDto> getResultClass() {
    return ApprovedProductDto.class;
  }

  @Override
  protected Class<ApprovedProductDto[]> getArrayResultClass() {
    return ApprovedProductDto[].class;
  }

  public Collection<ApprovedProductDto> getApprovedProducts(UUID facilityId, UUID programId,
                                                            boolean fullSupply) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("programId", programId);
    parameters.put("fullSupply", fullSupply);

    return findAll(facilityId + "/approvedProducts", parameters);
  }

}
