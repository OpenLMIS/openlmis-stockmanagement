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

package org.openlmis.stockmanagement.repository.custom.impl;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.stockmanagement.domain.event.StockEvent;
import org.openlmis.stockmanagement.domain.event.StockEventLineItem;
import org.openlmis.stockmanagement.repository.custom.StockEventSearchParams;
import org.openlmis.stockmanagement.repository.custom.StockEventsRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class StockEventsRepositoryImpl implements StockEventsRepositoryCustom {

  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Page<StockEvent> search(StockEventSearchParams params, Pageable pageable) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();

    CriteriaQuery<StockEvent> query = builder.createQuery(StockEvent.class);
    Root<StockEvent> root = query.from(StockEvent.class);
    query.where(toPredicates(builder, root, params));
    query.orderBy(builder.desc(root.get("processedDate")), builder.desc(root.get("id")));
    query.distinct(true);

    List<StockEvent> content = entityManager.createQuery(query)
        .setFirstResult((int) pageable.getOffset())
        .setMaxResults(pageable.getPageSize())
        .getResultList();

    Long total = countTotal(builder, params);
    return new PageImpl<>(content, pageable, total);
  }

  private Long countTotal(CriteriaBuilder builder, StockEventSearchParams params) {
    CriteriaQuery<Long> query = builder.createQuery(Long.class);
    Root<StockEvent> root = query.from(StockEvent.class);
    query.select(builder.countDistinct(root));
    query.where(toPredicates(builder, root, params));

    return entityManager.createQuery(query).getSingleResult();
  }

  private Predicate[] toPredicates(CriteriaBuilder builder, Root<StockEvent> root,
      StockEventSearchParams params) {
    List<Predicate> predicates = new ArrayList<>();
    predicates.add(builder.equal(root.get("facilityId"), params.getFacilityId()));
    predicates.add(builder.equal(root.get("programId"), params.getProgramId()));
    predicates.add(root.get("eventOrigin").in(params.getEventOrigins()));

    if (StringUtils.isNotBlank(params.getDocumentNumber())) {
      predicates.add(builder.like(
          builder.lower(root.get("documentNumber")),
          "%" + params.getDocumentNumber().toLowerCase() + "%"));
    }

    // An event matches when any of its line items falls in the range; the row
    // still shows the whole event, so its date may fall outside that window.
    if (params.getOccurredDateFrom() != null || params.getOccurredDateTo() != null) {
      Join<StockEvent, StockEventLineItem> lineItems = root.join("lineItems");
      if (params.getOccurredDateFrom() != null) {
        predicates.add(builder.greaterThanOrEqualTo(
            lineItems.get("occurredDate"), params.getOccurredDateFrom()));
      }
      if (params.getOccurredDateTo() != null) {
        predicates.add(builder.lessThanOrEqualTo(
            lineItems.get("occurredDate"), params.getOccurredDateTo()));
      }
    }

    return predicates.toArray(new Predicate[0]);
  }
}
