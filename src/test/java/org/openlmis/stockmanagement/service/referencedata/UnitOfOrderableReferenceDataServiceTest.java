package org.openlmis.stockmanagement.service.referencedata;

import org.openlmis.stockmanagement.dto.referencedata.UnitOfOrderableDto;
import org.openlmis.stockmanagement.service.BaseCommunicationService;

public class UnitOfOrderableReferenceDataServiceTest extends BaseReferenceDataServiceTest<UnitOfOrderableDto> {

  private UnitOfOrderableReferenceDataService service;

  @Override
  protected BaseCommunicationService<UnitOfOrderableDto> getService() {
    return new UnitOfOrderableReferenceDataService();
  }

  @Override
  protected UnitOfOrderableDto generateInstance() {
    return new UnitOfOrderableDto();
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();

    service = (UnitOfOrderableReferenceDataService) prepareService();
  }
}
