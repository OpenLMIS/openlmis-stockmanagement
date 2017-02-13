package org.openlmis.stockmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderableDto {
  private UUID id;
  private String productCode;
  private String name;
  private long packSize;
  private long packRoundingThreshold;
  private boolean roundToZero;
  private Set<ProgramOrderableDto> programs;
  private DispensableDto dispensable;

  /**
   * Returns the number of packs to order. For this Orderable given a desired number of
   * dispensing units, will return the number of packs that should be ordered.
   *
   * @param dispensingUnits # of dispensing units we'd like to order for
   * @return the number of packs that should be ordered.
   */
  public long packsToOrder(long dispensingUnits) {
    if (dispensingUnits <= 0 || packSize == 0) {
      return 0;
    }

    long packsToOrder = dispensingUnits / packSize;
    long remainderQuantity = dispensingUnits % packSize;

    if (remainderQuantity > 0 && remainderQuantity > packRoundingThreshold) {
      packsToOrder += 1;
    }

    if (packsToOrder == 0 && !roundToZero) {
      packsToOrder = 1;
    }

    return packsToOrder;
  }

  /**
   * Find ProgramOrderableDto in programs using programId.
   *
   * @param programId programId
   * @return product
   */
  public ProgramOrderableDto findProgramOrderableDto(UUID programId) {
    if (programs != null) {
      for (ProgramOrderableDto programOrderableDto : programs) {
        if (programOrderableDto.getProgramId().equals(programId)) {
          return programOrderableDto;
        }
      }
    }
    return null;
  }
}
