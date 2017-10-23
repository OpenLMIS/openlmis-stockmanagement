/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.stockmanagement.validators;

import static java.util.Collections.singletonList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_DESTINATION_FREE_TEXT_NOT_ALLOWED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_REASON_FREE_TEXT_NOT_ALLOWED;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_DESTINATION_FREE_TEXT_BOTH_PRESENT;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_SOURCE_FREE_TEXT_NOT_ALLOWED;
import static org.openlmis.stockmanagement.testutils.StockEventDtoBuilder.createNoSourceDestinationStockEventDto;
import static org.openlmis.stockmanagement.testutils.StockEventDtoBuilder.createStockEventDto;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.domain.sourcedestination.Node;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.exception.ValidationMessageException;

import java.util.UUID;

public class FreeTextValidatorTest extends BaseValidatorTest {

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @InjectMocks
  private FreeTextValidator freeTextValidator;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    when(nodeRepository.findOne(null)).thenThrow(new IllegalArgumentException());
  }

  @Test
  public void should_fail_when_source_not_exist_but_source_free_text_exist() throws Exception {
    StockEventDto eventDto = createNoSourceDestinationStockEventDto();
    eventDto.getLineItems().get(0).setSourceId(null);
    eventDto.getLineItems().get(0).setSourceFreeText("source free text");
    setContext(eventDto);

    exception.expect(ValidationMessageException.class);
    exception.expectMessage(containsString(ERROR_SOURCE_FREE_TEXT_NOT_ALLOWED));

    freeTextValidator.validate(eventDto);
  }

  @Test
  public void should_fail_when_destination_not_exist_but_destination_free_text_exist()
      throws Exception {
    StockEventDto eventDto = createNoSourceDestinationStockEventDto();
    eventDto.getLineItems().get(0).setDestinationFreeText("destination free text");
    setContext(eventDto);

    exception.expect(ValidationMessageException.class);
    exception.expectMessage(containsString(ERROR_DESTINATION_FREE_TEXT_NOT_ALLOWED));

    freeTextValidator.validate(eventDto);
  }

  @Test
  public void should_fail_when_reason_not_exist_but_reason_free_text_exist() throws Exception {
    StockEventDto eventDto = createNoSourceDestinationStockEventDto();
    eventDto.getLineItems().get(0).setReasonId(null);
    eventDto.getLineItems().get(0).setReasonFreeText("reason free text");
    setContext(eventDto);

    exception.expect(ValidationMessageException.class);
    exception.expectMessage(containsString(ERROR_REASON_FREE_TEXT_NOT_ALLOWED));

    freeTextValidator.validate(eventDto);
  }

  @Test
  public void should_fail_when_event_has_both_source_and_destination_free_text() throws Exception {
    StockEventDto eventDto = createStockEventDto();
    eventDto.getLineItems().get(0).setReasonId(randomUUID());
    eventDto.getLineItems().get(0).setDestinationId(randomUUID());
    eventDto.getLineItems().get(0).setSourceFreeText("source free text");
    eventDto.getLineItems().get(0).setDestinationFreeText("destination free text");
    setContext(eventDto);

    exception.expect(ValidationMessageException.class);
    exception.expectMessage(containsString(ERROR_SOURCE_DESTINATION_FREE_TEXT_BOTH_PRESENT));

    freeTextValidator.validate(eventDto);
  }

  @Test
  public void should_fail_when_source_node_is_refdata_with_free_text() throws Exception {
    UUID sourceId = fromString("0bd28568-43f1-4836-934d-ec5fb11398e8");

    StockEventDto eventDto = createNoSourceDestinationStockEventDto();
    eventDto.getLineItems().get(0).setSourceId(sourceId);
    eventDto.getLineItems().get(0).setSourceFreeText("source free text");
    setContext(eventDto);

    mockNode(sourceId, eventDto);

    exception.expect(ValidationMessageException.class);
    exception.expectMessage(containsString(ERROR_SOURCE_FREE_TEXT_NOT_ALLOWED));

    freeTextValidator.validate(eventDto);
  }

  @Test
  public void should_fail_when_destination_node_is_refdata_with_free_text() throws Exception {
    UUID destinationId = fromString("0bd28568-43f1-4836-934d-ec5fb11398e8");

    StockEventDto eventDto = createNoSourceDestinationStockEventDto();
    eventDto.getLineItems().get(0).setDestinationId(destinationId);
    eventDto.getLineItems().get(0).setDestinationFreeText("destination free text");
    setContext(eventDto);

    mockNode(destinationId, eventDto);

    exception.expect(ValidationMessageException.class);
    exception.expectMessage(containsString(ERROR_DESTINATION_FREE_TEXT_NOT_ALLOWED));

    freeTextValidator.validate(eventDto);
  }

  @Test
  public void should_fail_when_reason_free_text_not_allowed_but_exist() throws Exception {
    StockCardLineItemReason mockReason = mock(StockCardLineItemReason.class);
    when(reasonRepository.findOne(any(UUID.class))).thenReturn(mockReason);
    when(mockReason.getIsFreeTextAllowed()).thenReturn(false);

    StockEventDto eventDto = createNoSourceDestinationStockEventDto();
    eventDto.getLineItems().get(0).setReasonId(fromString("e3fc3cf3-da18-44b0-a220-77c985202e06"));
    eventDto.getLineItems().get(0).setReasonFreeText("reason free text");
    setContext(eventDto);

    exception.expect(ValidationMessageException.class);
    exception.expectMessage(containsString(ERROR_REASON_FREE_TEXT_NOT_ALLOWED));

    freeTextValidator.validate(eventDto);
  }

  private void mockNode(UUID destinationId, StockEventDto eventDto) {
    Node mockNode = mock(Node.class);

    when(mockNode.getId()).thenReturn(destinationId);
    when(mockNode.isRefDataFacility()).thenReturn(true);
    when(nodeRepository.findByIdIn(eventDto.getNodeIds())).thenReturn(singletonList(mockNode));
  }
}