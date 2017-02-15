package org.openlmis.stockmanagement.domain.adjustment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlmis.stockmanagement.domain.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stock_card_line_item_reasons", schema = "stockmanagement")
@JsonInclude(NON_NULL)
public class StockCardLineItemReason extends BaseEntity {

  @Column(nullable = false, columnDefinition = TEXT_COLUMN_DEFINITION)
  private String name;

  @Column(columnDefinition = TEXT_COLUMN_DEFINITION)
  private String description;

  @Column(nullable = false, columnDefinition = TEXT_COLUMN_DEFINITION)
  @Enumerated(value = EnumType.STRING)
  private ReasonType reasonType;

  @Column(nullable = false, columnDefinition = TEXT_COLUMN_DEFINITION)
  @Enumerated(value = EnumType.STRING)
  private ReasonCategory reasonCategory;

  @Column(nullable = false)
  private Boolean isFreeTextAllowed = false;

  /**
   * Create physical credit reason.
   *
   * @return physical credit reason.
   */
  public static StockCardLineItemReason physicalCredit() {
    return builder()
            .reasonType(ReasonType.CREDIT)
            .reasonCategory(ReasonCategory.PHYSICAL_INVENTORY)
            .name("Overstock")
            .description("Inventory correction in case of overstock")
            .build();
  }


  /**
   * Create physical debit reason.
   *
   * @return physical debit reason.
   */
  public static StockCardLineItemReason physicalDebit() {
    return builder()
            .reasonType(ReasonType.DEBIT)
            .reasonCategory(ReasonCategory.PHYSICAL_INVENTORY)
            .name("Understock")
            .description("Inventory correction in case of understock")
            .build();
  }


  /**
   * Create physical balance reason.
   *
   * @return physical balance reason.
   */
  public static StockCardLineItemReason physicalBalance() {
    return builder()
            .reasonType(ReasonType.BALANCE_ADJUSTMENT)
            .reasonCategory(ReasonCategory.PHYSICAL_INVENTORY)
            .name("Balance adjustment")
            .description("Balance adjustment")
            .build();
  }
}
