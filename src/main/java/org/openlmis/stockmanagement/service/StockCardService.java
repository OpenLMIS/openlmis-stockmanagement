package org.openlmis.stockmanagement.service;

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.repository.StockCardLineItemsRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static org.openlmis.stockmanagement.domain.card.StockCard.createStockCardFrom;
import static org.openlmis.stockmanagement.domain.card.StockCardLineItem.createLineItemsFrom;

@Service
public class StockCardService {

  @Autowired
  private StockCardLineItemsRepository stockCardLineItemsRepository;

  @Autowired
  private StockCardRepository stockCardRepository;

  /**
   * Generate stock card line items and stock cards based on event, and persist them.
   *
   * @param stockEventDto the origin event.
   * @param savedEventId  saved event id.
   * @param currentUserId current user id.
   * @throws IllegalAccessException IllegalAccessException.
   * @throws InstantiationException InstantiationException.
   */
  public void saveFromEvent(StockEventDto stockEventDto, UUID savedEventId, UUID currentUserId)
          throws IllegalAccessException, InstantiationException {

    StockCard foundStockCard = tryToFindExistingCard(stockEventDto);
    List<StockCardLineItem> lineItems =
            createLineItemsFrom(stockEventDto, savedEventId, currentUserId);

    for (StockCardLineItem lineItem : lineItems) {
      if (foundStockCard == null) {
        createAndSaveNewStockCard(stockEventDto, lineItem);
      } else {
        lineItem.setStockCard(foundStockCard);
      }
    }
    stockCardLineItemsRepository.save(lineItems);
  }

  private void createAndSaveNewStockCard(StockEventDto stockEventDto, StockCardLineItem lineItem)
          throws IllegalAccessException, InstantiationException {
    StockCard stockCard = createStockCardFrom(stockEventDto, lineItem.getOriginEvent().getId());
    lineItem.setStockCard(stockCard);
    stockCardRepository.save(stockCard);
  }

  private StockCard tryToFindExistingCard(StockEventDto stockEventDto) {
    StockCard foundStockCard = null;
    if (stockEventDto.hasStockCardIdentifier()) {
      foundStockCard = stockCardRepository.findOne(stockEventDto.getStockCardId());
    } else if (stockEventDto.hasAlternativeStockCardIdentifier()) {
      foundStockCard = stockCardRepository.findByProgramIdAndFacilityIdAndOrderableId(
              stockEventDto.getProgramId(),
              stockEventDto.getFacilityId(),
              stockEventDto.getOrderableId());
    }
    return foundStockCard;
  }
}
