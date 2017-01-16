package org.openlmis.stockmanagement.service.referencedata;

import org.openlmis.stockmanagement.dto.FacilityTypeDto;
import org.springframework.stereotype.Service;

@Service
public class FacilityTypeReferenceDataService extends BaseReferenceDataService<FacilityTypeDto> {

  @Override
  protected String getUrl() {
    return "/api/facilityTypes/";
  }

  @Override
  protected Class<FacilityTypeDto> getResultClass() {
    return FacilityTypeDto.class;
  }

  @Override
  protected Class<FacilityTypeDto[]> getArrayResultClass() {
    return FacilityTypeDto[].class;
  }
}
