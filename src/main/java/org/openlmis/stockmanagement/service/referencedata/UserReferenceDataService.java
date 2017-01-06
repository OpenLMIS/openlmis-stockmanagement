package org.openlmis.stockmanagement.service.referencedata;

import org.openlmis.stockmanagement.dto.BooleanResultDto;
import org.openlmis.stockmanagement.dto.UserDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserReferenceDataService extends BaseReferenceDataService<UserDto> {

  @Override
  protected String getUrl() {
    return "/api/users/";
  }

  @Override
  protected Class<UserDto> getResultClass() {
    return UserDto.class;
  }

  @Override
  protected Class<UserDto[]> getArrayResultClass() {
    return UserDto[].class;
  }

  public Collection<UserDto> findUsers(Map<String, Object> parameters) {
    return findAll("search", parameters);
  }

  /**
   * This method retrieves a user with given name.
   *
   * @param name the name of user.
   * @return UserDto containing user's data, or null if such user was not found.
   */
  public UserDto findUser(String name) {
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("username", name);

    List<UserDto> users = new ArrayList<>(postFindAll("search", Collections.emptyMap(),
        requestBody));
    return users.isEmpty() ? null : users.get(0);
  }

  /**
   * Check if user has a right with certain criteria.
   *
   * @param user     id of user to check for right
   * @param right    right to check
   * @param program  program to check (for supervision rights, can be {@code null})
   * @param facility facility to check (for supervision rights, can be {@code null})
   * @return an instance of {@link BooleanResultDto} with true or false depending on if user has the
   *         right.
   */
  public BooleanResultDto hasRight(UUID user, UUID right, UUID program, UUID facility,
                                   UUID warehouse) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put("rightId", right);

    if (null != program) {
      parameters.put("programId", program);
    }

    if (null != facility) {
      parameters.put("facilityId", facility);
    }

    if (null != warehouse) {
      parameters.put("warehouseId", warehouse);
    }

    return get(BooleanResultDto.class, user + "/hasRight", parameters);
  }

}
