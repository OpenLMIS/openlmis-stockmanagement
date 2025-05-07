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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.service.notification.NotificationChannelDto.EMAIL;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.stockmanagement.dto.referencedata.UserDto;
import org.openlmis.stockmanagement.service.AuthService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceTest {
  private static final String ACCESS_TOKEN = "token";
  private static final String MAIL_SUBJECT = "subject";
  private static final String MAIL_CONTENT = "content";
  private static final String BASE_URL = "http://localhost";
  private static final String NOTIFICATION_URL = BASE_URL + "/api/notifications";

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
    ReflectionTestUtils.setField(notificationService, "notificationUrl", BASE_URL);
  }

  @Test
  public void shouldNotifyUser() throws Exception {
    UserDto user = mock(UserDto.class);

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

  private NotificationDto getNotificationRequest(UserDto user) {
    Map<String, MessageDto> messages = new HashMap<>();
    messages.put(EMAIL.toString(), new MessageDto(MAIL_SUBJECT, MAIL_CONTENT));

    return new NotificationDto(user.getId(), messages);
  }
}
