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

package org.openlmis.stockmanagement.service;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_CANCELLATION_REASON_INVALID;
import static org.openlmis.stockmanagement.i18n.MessageKeys.ERROR_EVENT_CANCELLATION_REASON_REQUIRED;
import static org.openlmis.stockmanagement.service.StockEventCancelValidationService.CANCEL_TAG;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.domain.reason.ReasonCategory;
import org.openlmis.stockmanagement.domain.reason.ReasonType;
import org.openlmis.stockmanagement.domain.reason.StockCardLineItemReason;
import org.openlmis.stockmanagement.dto.StockEventCancelLineItemDto;
import org.openlmis.stockmanagement.dto.StockEventCancellationLineErrorDto;
import org.openlmis.stockmanagement.exception.StockEventCancellationException;
import org.openlmis.stockmanagement.repository.StockCardLineItemReasonRepository;

@RunWith(MockitoJUnitRunner.class)
public class CancellationReasonResolverTest {

  @Mock
  private StockCardLineItemReasonRepository reasonRepository;

  @InjectMocks
  private CancellationReasonResolver resolver;

  @Test
  public void shouldResolveReasonThatCountersIssue() {
    StockEventLineItem issue = issueLineItem();
    StockCardLineItemReason reason = reason(ReasonType.CREDIT, CANCEL_TAG);
    when(reasonRepository.findByIdIn(anyCollection())).thenReturn(singletonList(reason));

    Map<UUID, StockCardLineItemReason> resolved =
        resolver.resolve(singletonList(requested(issue.getId(), reason.getId())),
            singletonMap(issue.getId(), issue));

    assertEquals(reason, resolved.get(issue.getId()));
  }

  @Test(expected = StockEventCancellationException.class)
  public void shouldThrowWhenReasonIsMissing() {
    StockEventLineItem issue = issueLineItem();

    resolver.resolve(singletonList(requested(issue.getId(), null)),
        singletonMap(issue.getId(), issue));
  }

  @Test(expected = StockEventCancellationException.class)
  public void shouldThrowWhenReasonIsNotCancelTagged() {
    StockEventLineItem issue = issueLineItem();
    StockCardLineItemReason reason = reason(ReasonType.CREDIT);
    when(reasonRepository.findByIdIn(anyCollection())).thenReturn(singletonList(reason));

    resolver.resolve(singletonList(requested(issue.getId(), reason.getId())),
        singletonMap(issue.getId(), issue));
  }

  @Test(expected = StockEventCancellationException.class)
  public void shouldThrowWhenReasonTypeDoesNotCounterMovement() {
    StockEventLineItem issue = issueLineItem();
    StockCardLineItemReason reason = reason(ReasonType.DEBIT, CANCEL_TAG);
    when(reasonRepository.findByIdIn(anyCollection())).thenReturn(singletonList(reason));

    resolver.resolve(singletonList(requested(issue.getId(), reason.getId())),
        singletonMap(issue.getId(), issue));
  }

  @Test
  public void shouldCollectEveryBadReasonAsAPerLineError() {
    StockEventLineItem missingReason = issueLineItem();
    StockEventLineItem wrongType = issueLineItem();
    StockCardLineItemReason debit = reason(ReasonType.DEBIT, CANCEL_TAG);
    when(reasonRepository.findByIdIn(anyCollection())).thenReturn(singletonList(debit));

    Map<UUID, StockEventLineItem> originals = new HashMap<>();
    originals.put(missingReason.getId(), missingReason);
    originals.put(wrongType.getId(), wrongType);

    try {
      resolver.resolve(
          asList(requested(missingReason.getId(), null),
              requested(wrongType.getId(), debit.getId())),
          originals);
      fail("expected StockEventCancellationException");
    } catch (StockEventCancellationException ex) {
      assertEquals(2, ex.getLineErrors().size());
      Map<UUID, String> messageKeyByLine = ex.getLineErrors().stream()
          .collect(toMap(StockEventCancellationLineErrorDto::getStockEventLineItemId,
              StockEventCancellationLineErrorDto::getMessageKey));
      assertEquals(ERROR_EVENT_CANCELLATION_REASON_REQUIRED,
          messageKeyByLine.get(missingReason.getId()));
      assertEquals(ERROR_EVENT_CANCELLATION_REASON_INVALID,
          messageKeyByLine.get(wrongType.getId()));
    }
  }

  private StockEventLineItem issueLineItem() {
    StockEventLineItem lineItem = StockEventLineItem.builder()
        .orderableId(UUID.randomUUID())
        .destinationId(UUID.randomUUID())
        .build();
    lineItem.setId(UUID.randomUUID());
    return lineItem;
  }

  private StockCardLineItemReason reason(ReasonType type, String... tags) {
    StockCardLineItemReason reason = StockCardLineItemReason.builder()
        .name("Cancellation " + type)
        .reasonType(type)
        .reasonCategory(ReasonCategory.ADJUSTMENT)
        .tags(asList(tags))
        .build();
    reason.setId(UUID.randomUUID());
    return reason;
  }

  private StockEventCancelLineItemDto requested(UUID lineItemId, UUID reasonId) {
    return new StockEventCancelLineItemDto(lineItemId, reasonId, null);
  }
}
