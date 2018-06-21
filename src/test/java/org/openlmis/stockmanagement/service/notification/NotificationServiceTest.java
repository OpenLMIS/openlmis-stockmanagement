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

package org.openlmis.stockmanagement.service.notification;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.referencedata.UserDto;
import org.openlmis.stockmanagement.service.AuthService;
import org.openlmis.util.NotificationRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceTest {
  private static final String ACCESS_TOKEN = "token";
  private static final String USER_EMAIL = "test@test.te";
  private static final String FROM = "noreply@test.te";
  private static final String MAIL_SUBJECT = "subject";
  private static final String MAIL_CONTENT = "content";
  private static final String NOTIFICATION_URL = "http://localhost/notifiation";

  @Mock
  private AuthService authService;

  @Mock
  private RestTemplate restTemplate;

  @InjectMocks
  private NotificationService notificationService;

  @Before
  public void setUp() {
    when(authService.obtainAccessToken()).thenReturn(ACCESS_TOKEN);

    ReflectionTestUtils.setField(notificationService, "restTemplate", restTemplate);
    ReflectionTestUtils.setField(notificationService, "notificationUrl", NOTIFICATION_URL);
    ReflectionTestUtils.setField(notificationService, "from", FROM);
  }

  @Test
  public void shouldNotifyUser() throws Exception {
    UserDto user = mock(UserDto.class);
    when(user.getEmail()).thenReturn(USER_EMAIL);

    notificationService.notify(user, MAIL_SUBJECT, MAIL_CONTENT);

    ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);

    verify(restTemplate).postForObject(eq(
        new URI(NOTIFICATION_URL)),
        captor.capture(), eq(Object.class));

    assertEquals(
        singletonList("Bearer " + ACCESS_TOKEN),
        captor.getValue().getHeaders()
            .get(HttpHeaders.AUTHORIZATION));
    assertTrue(
        EqualsBuilder.reflectionEquals(getNotificationRequest(user),
            captor.getValue().getBody()));
  }

  private NotificationRequest getNotificationRequest(UserDto user) {
    return new NotificationRequest(
          FROM, user.getEmail(), MAIL_SUBJECT, MAIL_CONTENT
      );
  }
}
