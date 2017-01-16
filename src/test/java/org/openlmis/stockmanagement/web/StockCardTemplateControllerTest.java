package org.openlmis.stockmanagement.web;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openlmis.stockmanagement.domain.template.StockCardTemplate;
import org.openlmis.stockmanagement.dto.StockCardTemplateDto;
import org.openlmis.stockmanagement.errorhandling.GlobalErrorHandling;
import org.openlmis.stockmanagement.exception.AuthenticationException;
import org.openlmis.stockmanagement.exception.MissingPermissionException;
import org.openlmis.stockmanagement.service.PermissionService;
import org.openlmis.stockmanagement.service.StockCardTemplateService;
import org.openlmis.stockmanagement.service.referencedata.ReferenceDataNotFoundException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.openlmis.stockmanagement.testutils.StockCardTemplateBuilder.createTemplateDto;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class StockCardTemplateControllerTest extends BaseWebTest {

  private MockMvc mvc;

  @MockBean
  private StockCardTemplateService stockCardTemplateService;

  @MockBean
  private PermissionService permissionService;

  @InjectMocks
  StockCardTemplateController controllerUnderTest;

  private UUID programId = UUID.randomUUID();
  private UUID facilityTypeId = UUID.randomUUID();

  static final String STOCK_CARD_TEMPLATE_API = "/api/stockCardTemplates";

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);

    mvc = MockMvcBuilders
            .standaloneSetup(controllerUnderTest)
            .setControllerAdvice(new GlobalErrorHandling())
            .build();
  }

  @Test
  public void should_get_default_stock_card_templates_without_params() throws Exception {
    //given
    when(stockCardTemplateService.getDefaultStockCardTemplate())
            .thenReturn(createTemplateDto());

    //when
    MockHttpServletRequestBuilder builder = get(STOCK_CARD_TEMPLATE_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE);

    ResultActions resultActions = mvc.perform(builder);

    //then
    resultActions
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content()
                    .json("{'stockCardFields':[{'name':'packSize'}]}"));
  }

  @Test
  public void should_search_for_stock_card_templates() throws Exception {

    //given
    when(stockCardTemplateService.findByProgramIdAndFacilityTypeId(programId, facilityTypeId))
            .thenReturn(createTemplateDto());

    //when
    MockHttpServletRequestBuilder builder = get(STOCK_CARD_TEMPLATE_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param("program", programId.toString())
            .param("facilityType", facilityTypeId.toString());

    ResultActions resultActions = mvc.perform(builder);

    //then
    resultActions
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andExpect(content().json("{'stockCardFields':[{'name':'packSize', displayed:true}]}"));
  }

  @Test
  public void should_return_404_when_template_not_found() throws Exception {
    //given
    when(stockCardTemplateService.findByProgramIdAndFacilityTypeId(any(), any()))
            .thenReturn(null);

    //when
    ResultActions resultActions = mvc.perform(get(STOCK_CARD_TEMPLATE_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .param("program", UUID.randomUUID().toString())
            .param("facilityType", UUID.randomUUID().toString()));

    //then
    resultActions
            .andExpect(status().isNotFound());
  }

  @Test
  public void should_return_201_when_create_template() throws Exception {

    //given
    Mockito.doNothing().when(permissionService).canCreateStockCardTemplate();
    when(stockCardTemplateService.saveOrUpdate(any(StockCardTemplateDto.class)))
            .thenReturn(createTemplateDto());

    //when
    ObjectMapper mapper = new ObjectMapper();
    String jsonString = mapper.writeValueAsString(new StockCardTemplate());

    ResultActions resultActions = mvc.perform(post(STOCK_CARD_TEMPLATE_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonString));

    //then
    resultActions.andExpect(status().isCreated());
  }

  @Test
  public void should_return_403_when_create_template_permission_not_found() throws Exception {
    //given
    Mockito.doThrow(new MissingPermissionException("MANAGE_STOCK_CARD_TEMPLATES"))
            .when(permissionService).canCreateStockCardTemplate();

    //when
    ObjectMapper mapper = new ObjectMapper();
    String jsonString = mapper.writeValueAsString(new StockCardTemplate());

    ResultActions resultActions = mvc.perform(post(STOCK_CARD_TEMPLATE_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonString));

    //then
    resultActions.andExpect(status().isForbidden());
  }

  @Test
  public void should_return_401_when_user_unauthorized() throws Exception {
    //given
    Mockito.doThrow(new AuthenticationException("MANAGE_STOCK_CARD_TEMPLATES"))
            .when(permissionService).canCreateStockCardTemplate();

    //when
    ObjectMapper mapper = new ObjectMapper();
    String jsonString = mapper.writeValueAsString(new StockCardTemplate());

    ResultActions resultActions = mvc.perform(post(STOCK_CARD_TEMPLATE_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonString));

    //then
    resultActions.andExpect(status().isUnauthorized());
  }

  @Test
  public void should_return_400_when_program_or_facility_type_do_not_exist() throws Exception {
    //given
    Mockito.doThrow(new ReferenceDataNotFoundException(""))
            .when(stockCardTemplateService).saveOrUpdate(any());

    //when
    ObjectMapper mapper = new ObjectMapper();
    String jsonString = mapper.writeValueAsString(new StockCardTemplate());

    ResultActions resultActions = mvc.perform(post(STOCK_CARD_TEMPLATE_API)
            .param(ACCESS_TOKEN, ACCESS_TOKEN_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonString));

    //then
    resultActions.andExpect(status().isBadRequest());
  }
}