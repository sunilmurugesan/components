package uk.gov.hmrc.eos.eutu55.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmrc.eos.eutu55.exception.EISGatewayException;
import uk.gov.hmrc.eos.eutu55.logger.LoggerComponent;
import uk.gov.hmrc.eos.eutu55.model.RetrievalRequest;
import uk.gov.hmrc.eos.eutu55.model.RetrievalResultResponse;
import uk.gov.hmrc.eos.eutu55.service.RetrievalService;
import uk.gov.hmrc.eos.eutu55.utils.Outcome;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmrc.eos.eutu55.utils.ErrorCode.EUTU55_EIS_GATEWAY_EXCEPTION;

@WebMvcTest(RetrievalController.class)
class RetrievalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RetrievalService retrievalService;

    @MockBean
    private LoggerComponent logger;

    private static final String USER_ID = "TEST_USER";


    @Test
    @DisplayName("retrieveData endpoint should be invoked and retrieval service is called")
    void retrieveData_shouldCallRetrievalEndpointAndCallService() throws Exception {

        RetrievalResultResponse respMsg = new RetrievalResultResponse(Outcome.ACCEPTED);
        when(retrievalService.processRetrieval(any(RetrievalRequest.class),any())).thenReturn(respMsg);
        mockMvc.perform(post("/pds/cnit/eutu55/retrieval/v1")
                        .content("{\"fromDate\":\"2006-05-04\"}")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("userId", USER_ID)
                )
                .andDo(print())
                .andExpect(status().isOk())
        ;
        verify(retrievalService).processRetrieval(any(RetrievalRequest.class), any());
    }

    @Test
    @DisplayName("retrieveData endpoint should return ok even if RetrievalResponse is not ok")
    void retrieveData_shouldCallRetrievalEndpointAndCallServiceResponseOKEvenWhenRetrievalResponseNotOK() throws Exception {

        RetrievalResultResponse respMsg = new RetrievalResultResponse(Outcome.REJECTED);
        when(retrievalService.processRetrieval(any(RetrievalRequest.class), any())).thenReturn(respMsg);
        mockMvc.perform(post("/pds/cnit/eutu55/retrieval/v1")
                        .content("{\"fromDate\":\"2006-05-04\"}")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("userId", USER_ID)
                )
                .andDo(print())
                .andExpect(status().isOk())
        ;
        verify(retrievalService).processRetrieval(any(RetrievalRequest.class), any());
    }

    @Test
    @DisplayName("retrieveData endpoint should throw EISGatewayException and respond with error code")
    void retrieveData_shouldCallRetrievalEndpointAndThrowsEISException() throws Exception {

        doThrow(new EISGatewayException(EUTU55_EIS_GATEWAY_EXCEPTION)).when(retrievalService).processRetrieval(any(RetrievalRequest.class), anyString());

        MvcResult mvcResult = mockMvc.perform(post("/pds/cnit/eutu55/retrieval/v1")
                .content("{\"fromDate\":\"2006-05-04\"}")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("userId", USER_ID)
        )
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andReturn();
        verify(retrievalService).processRetrieval(any(RetrievalRequest.class), any());
        assertThat(mvcResult.getResponse().getContentAsString().contains(EUTU55_EIS_GATEWAY_EXCEPTION.code()));
    }

}