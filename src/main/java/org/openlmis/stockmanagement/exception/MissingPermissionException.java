package org.openlmis.stockmanagement.exception;


public class MissingPermissionException extends RuntimeException {

  public MissingPermissionException(String permissionName) {
    super("You do not have the following permission to perform this action: " + permissionName);
  }

}
