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

package org.openlmis.stockmanagement.web;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.openlmis.stockmanagement.domain.sourcedestination.Organization;
import org.openlmis.stockmanagement.exception.PermissionMessageException;
import org.openlmis.stockmanagement.repository.OrganizationRepository;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.util.Message;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import java.util.UUID;

public class OrganizationControllerIntegrationTest extends BaseWebTest {
  private static final String ORGANIZATION_API = "/api/organizations/";

  @MockBean
  private PermissionService permissionService;

  @MockBean
  private OrganizationRepository organizationRepository;

  @Test
  public void should_return_201_when_organization_created_successfully() throws Exception {
    //given
    Organization organization = createOrganization("New Org");
    when(organizationRepository.save(organization)).thenReturn(organization);

    //when
    ResultActions resultActions = mvc.perform(post(ORGANIZATION_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(organization)));

    //then
    resultActions.andExpect(status().isCreated())
        .andExpect(jsonPath("$.name", is(organization.getName())));
  }

  @Test
  public void should_return_200_when_user_has_permission_to_get_organizations() throws Exception {
    //given
    when(organizationRepository.findAll()).thenReturn(
        asList(createOrganization("Existing Org1"),
            createOrganization("Existing Org2")));

    //when
    ResultActions resultActions = mvc.perform(get(ORGANIZATION_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON));

    //then
    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  public void should_return_200_when_organization_update_completed() throws Exception {
    //given
    Organization organization = createOrganization("Updated Org");
    organization.setId(UUID.randomUUID());
    when(organizationRepository.findOne(organization.getId())).thenReturn(organization);
    when(organizationRepository.save(organization)).thenReturn(organization);

    //when
    ResultActions resultActions = mvc.perform(
        put(ORGANIZATION_API + organization.getId().toString())
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectToJsonString(organization)));

    //then
    resultActions.andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(organization.getId().toString())))
        .andExpect(jsonPath("$.name", is(organization.getName())));
  }

  @Test
  public void should_return_403_when_user_has_no_permission_to_manage_organizations()
      throws Exception {
    //given
    doThrow(new PermissionMessageException(new Message("key")))
        .when(permissionService).canManageOrganizations();
    Organization organization = createOrganization("Would Get 403");

    //1. try to create organization
    ResultActions postResult = mvc.perform(post(ORGANIZATION_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(organization)));
    postResult.andExpect(status().isForbidden());

    //2. try to update organization
    ResultActions putResult = mvc.perform(
        put(ORGANIZATION_API + UUID.randomUUID().toString())
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectToJsonString(organization)));
    putResult.andExpect(status().isForbidden());

    //3. try to retrieve organizations
    ResultActions getResult = mvc.perform(get(ORGANIZATION_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON));
    getResult.andExpect(status().isForbidden());
  }

  @Test
  public void should_return_200_when_try_to_create_organizations_has_existed() throws Exception {
    //given
    Organization organization = createOrganization("Test Org");
    when(organizationRepository.findByName(organization.getName())).thenReturn(organization);

    //when
    ResultActions resultActions = mvc.perform(post(ORGANIZATION_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(organization)));

    //then
    resultActions.andExpect(status().isOk());
  }

  @Test
  public void should_return_400_when_reason_without_name() throws Exception {
    //when
    ResultActions resultActions = mvc.perform(post(ORGANIZATION_API)
        .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectToJsonString(new Organization())));

    //then
    resultActions.andExpect(status().isBadRequest());
  }

  @Test
  public void should_return_400_when_would_be_updated_organization_content_exists()
      throws Exception {
    //given
    Organization updateOrg = createOrganization("Existing Org Name");
    when(organizationRepository.findOne(updateOrg.getId())).thenReturn(updateOrg);
    when(organizationRepository.findByName(updateOrg.getName()))
        .thenReturn(createOrganization("Existing Org Name"));

    //when
    ResultActions resultActions = mvc.perform(
        put(ORGANIZATION_API + updateOrg.getId())
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectToJsonString(updateOrg)));

    //then
    resultActions.andExpect(status().isBadRequest());
  }

  private Organization createOrganization(String name) {
    Organization organization1 = new Organization();
    organization1.setName(name);
    return organization1;
  }

}