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

package org.openlmis.stockmanagement.interceptor;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openlmis.stockmanagement.exception.ValidationMessageException;
import org.openlmis.stockmanagement.util.Message;
import org.openlmis.stockmanagement.util.PageableRequestContext;
import org.openlmis.stockmanagement.validators.PageableValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Service
public class MvcInterceptor extends HandlerInterceptorAdapter {

  private static final String SIZE_PARAM = "size";
  private static final String PAGE_PARAM = "page";
  private static final String PAGEABLE_CONTEXT = "pageableContext";

  @Autowired
  private PageableValidator pageableValidator;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler) throws Exception {
    Map<String, String[]> params = request.getParameterMap();

    PageableRequestContext context = new PageableRequestContext(
                extractIntegerParam(params, SIZE_PARAM),
                extractIntegerParam(params, PAGE_PARAM));

    Errors errors = new BeanPropertyBindingResult(context, PAGEABLE_CONTEXT);
    pageableValidator.validate(context, errors);

    if (errors.getErrorCount() > 0) {
      throw new ValidationMessageException(new Message(errors.getFieldError().getCode(),
                errors.getFieldError().getArguments()));
    }

    return true;
  }

  private Integer extractIntegerParam(Map<String, String[]> params, String name) {
    return params.get(name) == null ? null : Integer.valueOf(params.get(name)[0]);
  }
}