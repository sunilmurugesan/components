package uk.gov.hmrc.eos.eutu55.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmrc.eos.eutu55.logger.LoggerComponent;
import uk.gov.hmrc.eos.eutu55.service.SynchronisationService;
import uk.gov.hmrc.eu.eutu55.synchronisation.AcknowledgementType;
import uk.gov.hmrc.eu.eutu55.synchronisation.PublishNumbersRespMsg;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;
import static uk.gov.hmrc.eos.eutu55.helper.TestHelper.request;
import static uk.gov.hmrc.eos.eutu55.helper.TestHelper.response;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.ACCEPTED;

@WebMvcTest(SynchronisationController.class)
class SynchronisationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SynchronisationService synchronisationService;

    @MockBean
    private LoggerComponent logger;

    @Test
    @DisplayName("When a message with valid records received, " +
            "message should be processed successfully and return valid response with outcome 1")
    void processIossVatData() throws Exception {
        String createOperationRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<v1:PublishNumbersReqMsg xmlns:servicestype=\"http://xmlns.ec.eu/BusinessObjects/IOSS_DR/Common/V1\"\n" +
                " xmlns:v1=\"http://xmlns.ec.eu/BusinessActivityService/IOSS_DR/ISynchronisationCBS/V1\"\n" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                " xsi:schemaLocation=\"http://xmlns.ec.eu/BusinessActivityService/IOSS_DR/ISynchronisationCBS/V1 file:/D:/fdev/ioss-dr/IOSS-DR_updates/FD3-SC09-DLV-153-6.9.1-2-ECOM-IOSS-DR%20Design%20Document%20for%20National%20Applications-SfI-v1.08/TSC/BusinessActivityService/IOSS_DR%20(CCN2)/SynchronisationCBS/V1/ICCN2.Service.Taxation.Default.IOSS_DR.SynchronisationCBS.xsd\">\n" +
                "    <servicestype:iossVatNumberUpdate>\n" +
                "        <servicestype:operation>C</servicestype:operation>\n" +
                "        <servicestype:iossVatId>IM0000000000</servicestype:iossVatId>\n" +
                "        <servicestype:validityStartDate>2006-05-04</servicestype:validityStartDate>\n" +
                "        <servicestype:validityEndDate>2006-05-04</servicestype:validityEndDate>\n" +
                "        <servicestype:modificationDateTime>2006-05-04T18:13:51.0</servicestype:modificationDateTime>\n" +
                "    </servicestype:iossVatNumberUpdate>\n" +
                "    <servicestype:iossVatNumberUpdate>\n" +
                "        <servicestype:operation>C</servicestype:operation>\n" +
                "        <servicestype:iossVatId>IM0000000001</servicestype:iossVatId>\n" +
                "        <servicestype:validityStartDate>2006-05-04</servicestype:validityStartDate>\n" +
                "        <servicestype:validityEndDate>2006-05-04</servicestype:validityEndDate>\n" +
                "        <servicestype:modificationDateTime>2006-05-04T18:13:51.0</servicestype:modificationDateTime>\n" +
                "    </servicestype:iossVatNumberUpdate>\n" +
                "</v1:PublishNumbersReqMsg>";

        PublishNumbersRespMsg publishNumbersRespMsg = new PublishNumbersRespMsg();
        AcknowledgementType acknowledgementType = new AcknowledgementType();
        acknowledgementType.setOutcome(ACCEPTED.getValue());
        publishNumbersRespMsg.setPublicationResults(acknowledgementType);
        when(synchronisationService.process(anyList())).thenReturn(publishNumbersRespMsg);
        mockMvc.perform(post("/pds/cnit/eutu55/synchronisation/v1")
                        .content(createOperationRequest)
                        .contentType(MediaType.APPLICATION_XML))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(xpath("//publicationResults/outcome").number((double) 1));
    }


    @Test
    @DisplayName("When an invalid message is received from EIS, then it is validated against the schema and correct fault is returned")
    void processInvalidIossVatData() throws Exception {
        mockMvc.perform(post("/pds/cnit/eutu55/synchronisation/v1")
                        .content(request("synchronisation/invalid-create-operation.xml"))
                        .contentType(MediaType.APPLICATION_XML))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(response("synchronisation/fault-invalid-request.xml")));
    }
}
