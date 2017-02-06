package org.openlmis.stockmanagement.domain.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.openlmis.stockmanagement.domain.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Data
@AllArgsConstructor
@Table(name = "stock_cards", schema = "stockmanagement")
public class StockCard extends BaseEntity {
}
