package org.openlmis.stockmanagement.service.referencedata;

public class ReferenceDataNotFoundException extends RuntimeException {

  /**
   * Constructs the exception.
   *
   * @param resource the resource that we were trying to retrieve
   */
  public ReferenceDataNotFoundException(String resource) {
    super(String.format("The id of %s does not exist.", resource));
  }
}
