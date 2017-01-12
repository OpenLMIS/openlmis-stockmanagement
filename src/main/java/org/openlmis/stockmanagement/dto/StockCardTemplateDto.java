package org.openlmis.stockmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.openlmis.stockmanagement.domain.template.AvailableStockCardFields;
import org.openlmis.stockmanagement.domain.template.AvailableStockCardLineItemFields;
import org.openlmis.stockmanagement.domain.template.StockCardTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.stream.Collectors.toList;

@Data
@JsonInclude(NON_NULL)
public class StockCardTemplateDto {

  private UUID programId;
  private UUID facilityTypeId;

  private List<StockCardFieldDto> stockCardFields = new ArrayList<>();
  private List<StockCardLineItemFieldDto> stockCardLineItemFields = new ArrayList<>();

  /**
   * Create stock card template dto object from DB model object.
   *
   * @param template the original object to convert from.
   * @return dto object.
   */
  public static StockCardTemplateDto from(StockCardTemplate template) {
    if (template == null) {
      return null;
    }

    StockCardTemplateDto dto = new StockCardTemplateDto();
    dto.setProgramId(template.getProgramId());
    dto.setFacilityTypeId(template.getFacilityTypeId());

    dto.stockCardFields = template.getStockCardFields().stream()
            .map(StockCardFieldDto::from).collect(toList());

    dto.stockCardLineItemFields = template.getStockCardLineItemFields().stream()
            .map(StockCardLineItemFieldDto::from).collect(toList());

    return dto;
  }

  /**
   * Convert to DB model object.
   *
   * @param availableCardFields     will be used to match stock card fields
   * @param availableLineItemFields will be use to match line item fields
   * @return DB model object.
   */
  public StockCardTemplate toModel(
          List<AvailableStockCardFields> availableCardFields,
          List<AvailableStockCardLineItemFields> availableLineItemFields) {

    StockCardTemplate template = new StockCardTemplate();
    template.setFacilityTypeId(this.getFacilityTypeId());
    template.setProgramId(this.getProgramId());

    template.setStockCardFields(stockCardFields.stream()
            .map(cardFieldDto -> cardFieldDto.toModel(template, availableCardFields))
            .collect(toList()));

    template.setStockCardLineItemFields(stockCardLineItemFields.stream()
            .map(lineItemFieldDto -> lineItemFieldDto.toModel(template, availableLineItemFields))
            .collect(toList()));

    return template;
  }
}
