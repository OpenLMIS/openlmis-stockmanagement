package org.openlmis.stockmanagement.service;

import org.openlmis.stockmanagement.domain.card.StockCardLineItem;
import org.openlmis.stockmanagement.repository.StockCardLineItemsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockCardService {

  @Autowired
  private StockCardLineItemsRepository stockCardLineItemsRepository;

  public void save(List<StockCardLineItem> lineItems) {
    stockCardLineItemsRepository.save(lineItems);
  }
}
