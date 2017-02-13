package org.openlmis.stockmanagement.service;

import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.dto.StockCardDto;
import org.openlmis.stockmanagement.dto.StockEventDto;
import org.openlmis.stockmanagement.repository.StockCardLineItemsRepository;
import org.openlmis.stockmanagement.repository.StockCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    StockCard stockCard = findExistingOrCreateNewCard(stockEventDto, savedEventId);

    createLineItemsFrom(stockEventDto, stockCard, currentUserId).forEach(lineItem -> {
      stockCardLineItemsRepository.save(lineItem);
    });
  }

  private StockCard findExistingOrCreateNewCard(StockEventDto stockEventDto, UUID savedEventId)
          throws InstantiationException, IllegalAccessException {

    StockCard foundCard = stockCardRepository.findByProgramIdAndFacilityIdAndOrderableId(
            stockEventDto.getProgramId(),
            stockEventDto.getFacilityId(),
            stockEventDto.getOrderableId());

    if (foundCard != null) {
      return foundCard;
    } else {
      return stockCardRepository.save(createStockCardFrom(stockEventDto, savedEventId));
    }
  }

  public StockCardDto findStockCardById(UUID stockCardId) {
    return null;
  }
}
