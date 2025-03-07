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

package org.openlmis.stockmanagement.web.external.stockcardsummaries;

import static java.util.Comparator.comparing;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections.MapUtils;
import org.openlmis.stockmanagement.domain.card.StockCard;
import org.openlmis.stockmanagement.dto.referencedata.ApprovedProductDto;
import org.openlmis.stockmanagement.dto.referencedata.LotDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableDto;
import org.openlmis.stockmanagement.dto.referencedata.OrderableFulfillDto;
import org.openlmis.stockmanagement.service.referencedata.LotReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.OrderableReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramReferenceDataService;
import org.openlmis.stockmanagement.util.deferredloading.LotDeferredLoader;
import org.openlmis.stockmanagement.util.deferredloading.OrderableDeferredLoader;
import org.openlmis.stockmanagement.util.deferredloading.ProgramDeferredLoader;
import org.slf4j.profiler.Profiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@SuppressWarnings("PMD.TooManyMethods")
@Component
public class StockCardSummariesExternalDtoBuilder {
  private final LotReferenceDataService lotReferenceDataService;
  private final OrderableReferenceDataService orderableReferenceDataService;
  private final ProgramReferenceDataService programReferenceDataService;

  /**
   * Creates instance of StockCardSummariesExternalDtoBuilder.
   *
   * @param lotReferenceDataService       reference data service
   * @param orderableReferenceDataService orderable data service
   * @param programReferenceDataService   program data service
   */
  @Autowired
  public StockCardSummariesExternalDtoBuilder(LotReferenceDataService lotReferenceDataService,
      OrderableReferenceDataService orderableReferenceDataService,
      ProgramReferenceDataService programReferenceDataService) {
    this.lotReferenceDataService = lotReferenceDataService;
    this.orderableReferenceDataService = orderableReferenceDataService;
    this.programReferenceDataService = programReferenceDataService;
  }

  /**
   * Builds Stock Card Summaries for external integrations.
   *
   * @param approvedProducts      approved products, not null
   * @param stockCards            stock cards, not null
   * @param orderableFulfills     orderablses, not null
   * @param nonEmptySummariesOnly whether products without stock cards should be included
   * @param profiler              profiler logs
   * @return the list of stock card summaries, never null
   */
  public List<StockCardSummaryExternalDto> build(List<ApprovedProductDto> approvedProducts,
      List<StockCard> stockCards,
      Map<UUID, OrderableFulfillDto> orderableFulfills,
      boolean nonEmptySummariesOnly,
      Profiler profiler) {

    final DeferredLoaders deferredLoaders = new DeferredLoaders();

    profiler.start("GET_DEFERRED_STOCK_SUMMARIES");
    final List<DeferredStockCardsSummary> deferredStockCardsSummaries = approvedProducts.stream()
        .map(approvedProduct -> fromApprovedProduct(
            new ProductBuildContext(approvedProduct, stockCards, orderableFulfills, deferredLoaders,
                nonEmptySummariesOnly))).filter(Optional::isPresent).map(Optional::get).sorted(
            comparing(
                deferredStockCardsSummary -> deferredStockCardsSummary.getStockCards().size()))
        .collect(toList());

    profiler.start("LOAD_DEFERRED_DATA");
    deferredLoaders.loadAll();

    profiler.start("TO_DTO");
    return deferredStockCardsSummaries.stream().map(this::mapDto).collect(Collectors.toList());
  }

  private Optional<DeferredStockCardsSummary> fromApprovedProduct(ProductBuildContext context) {
    final Set<DeferredSummary> stockCardSummarySet = buildDeferredSummaries(context);

    if (context.isNonEmptySummariesOnly() && stockCardSummarySet.isEmpty()) {
      return empty();
    }

    return of(new DeferredStockCardsSummary(
        context.deferredProgram(context.getProduct().getProgram().getId()),
        context.deferredOrderable(context.getProduct().getOrderable().getId()),
        stockCardSummarySet));
  }

  private Set<DeferredSummary> buildDeferredSummaries(ProductBuildContext context) {
    final Set<DeferredSummary> stockCardSummarySet;

    if (context.getFulfills() == null) {
      stockCardSummarySet = new HashSet<>();
    } else {
      // Build summaries for fulfilling products
      stockCardSummarySet = context.getFulfills().getCanFulfillForMe().stream()
          .map(id -> buildStockCardSummaries(context.getProductsCards(id), context))
          .flatMap(List::stream).collect(toSet());
    }

    // Build summaries for the product
    stockCardSummarySet.addAll(buildStockCardSummaries(context.getProductsCards(), context));

    return stockCardSummarySet;
  }

  private List<DeferredSummary> buildStockCardSummaries(List<StockCard> stockCards,
      ProductBuildContext context) {
    if (isEmpty(stockCards)) {
      return Collections.emptyList();
    } else {
      return stockCards.stream().map(
          stockCard -> new DeferredSummary(context.deferredLot(stockCard.getLotId()),
              context.deferredOrderable(stockCard.getOrderableId()), stockCard)).collect(toList());
    }
  }

  private StockCardSummaryExternalDto mapDto(DeferredStockCardsSummary deferredStockCardsSummary) {

    final List<StockCardSummaryItemExternalDto> stockCards =
        deferredStockCardsSummary.getStockCards().stream().map(this::mapDto).collect(toList());

    final Integer totalStockOnHand =
        stockCards.stream().mapToInt(StockCardSummaryItemExternalDto::getStockOnHand).sum();

    return new StockCardSummaryExternalDto(deferredStockCardsSummary.getProgram().get().getCode(),
        deferredStockCardsSummary.getOrderable().get().getProductCode(), totalStockOnHand,
        stockCards);
  }

  private StockCardSummaryItemExternalDto mapDto(DeferredSummary deferredStockCardSummary) {

    final Optional<LotDto> maybeLot = ofNullable(deferredStockCardSummary.getLot());

    return new StockCardSummaryItemExternalDto(maybeLot.map(LotDto::getLotCode).orElse(null),
        deferredStockCardSummary.getOrderable().getProductCode(),
        deferredStockCardSummary.getStockCard().getStockOnHand(),
        maybeLot.map(LotDto::getExpirationDate).orElse(null),
        deferredStockCardSummary.getStockCard().getOccurredDate());
  }

  @AllArgsConstructor
  private static class DeferredSummary {
    private final LotDeferredLoader.Handle lot;
    private final OrderableDeferredLoader.Handle orderable;
    @Getter
    private final StockCard stockCard;

    public LotDto getLot() {
      return lot != null ? lot.get() : null;
    }

    public OrderableDto getOrderable() {
      return orderable != null ? orderable.get() : null;
    }
  }

  @AllArgsConstructor
  @Getter
  private static class DeferredStockCardsSummary {
    private final ProgramDeferredLoader.Handle program;
    private final OrderableDeferredLoader.Handle orderable;
    private final Set<DeferredSummary> stockCards;
  }

  @Getter
  private class DeferredLoaders {
    private final LotDeferredLoader lotLoader;
    private final OrderableDeferredLoader orderableLoader;
    private final ProgramDeferredLoader programLoader;

    DeferredLoaders() {
      this.lotLoader = new LotDeferredLoader(lotReferenceDataService);
      this.orderableLoader = new OrderableDeferredLoader(orderableReferenceDataService);
      this.programLoader = new ProgramDeferredLoader(programReferenceDataService);
    }

    void loadAll() {
      lotLoader.loadDeferredObjects();
      orderableLoader.loadDeferredObjects();
      programLoader.loadDeferredObjects();
    }
  }

  private class ProductBuildContext {
    private final DeferredLoaders deferredLoaders;
    private final Map<UUID, List<StockCard>> stockCardByProduct;
    @Getter
    private final ApprovedProductDto product;
    @Getter
    private final OrderableFulfillDto fulfills;
    @Getter
    private final boolean nonEmptySummariesOnly;

    ProductBuildContext(ApprovedProductDto product,
        List<StockCard> allStockCards,
        Map<UUID, OrderableFulfillDto> orderableFulfill,
        DeferredLoaders deferredLoaders,
        boolean nonEmptySummariesOnly) {
      this.deferredLoaders = deferredLoaders;
      this.product = product;
      this.stockCardByProduct =
          allStockCards.stream().collect(groupingBy(StockCard::getOrderableId, toList()));
      this.fulfills =
          MapUtils.isEmpty(orderableFulfill) ? null : orderableFulfill.get(product.getId());
      this.nonEmptySummariesOnly = nonEmptySummariesOnly;
    }

    List<StockCard> getProductsCards() {
      return getProductsCards(product.getOrderable().getId());
    }

    List<StockCard> getProductsCards(UUID id) {
      return stockCardByProduct.get(id);
    }

    LotDeferredLoader.Handle deferredLot(UUID id) {
      return deferredLoaders.getLotLoader().deferredLoad(id);
    }

    OrderableDeferredLoader.Handle deferredOrderable(UUID id) {
      return deferredLoaders.getOrderableLoader().deferredLoad(id);
    }

    ProgramDeferredLoader.Handle deferredProgram(UUID id) {
      return deferredLoaders.getProgramLoader().deferredLoad(id);
    }
  }
}
