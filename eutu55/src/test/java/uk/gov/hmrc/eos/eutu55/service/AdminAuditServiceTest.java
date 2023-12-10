package uk.gov.hmrc.eos.eutu55.service;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import uk.gov.hmrc.eos.eutu55.dao.AdminAuditDao;
import uk.gov.hmrc.eos.eutu55.entity.AdminAudit;
import uk.gov.hmrc.eos.eutu55.model.AdminAuditRequest;
import uk.gov.hmrc.eos.eutu55.model.AdminAuditResponse;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmrc.eos.eutu55.utils.AdminActionType.NOTIFICATION;
import static uk.gov.hmrc.eos.eutu55.utils.AdminActionType.RETRIEVAL;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.ACCEPTED;

@ExtendWith(MockitoExtension.class)
class AdminAuditServiceTest {

    @Mock
    private AdminAuditDao dao;

    @InjectMocks
    private AdminAuditService service;

    @Captor
    ArgumentCaptor<AdminAudit> adminAuditCaptor;

    private static final String USER_ID = "TEST_USER";


    @Test
    @DisplayName("Admin audit record can be created and handed to repository for creation")
    void shouldCreateAdminAuditRecord() {
        service.save(new Object() {
            @Getter
            private String field1 = "this is field 1";
            @Getter
            private String field2 = "this is field 2";
        }, NOTIFICATION, ACCEPTED);

        verify(dao).save(adminAuditCaptor.capture());

        AdminAudit adminAudit = adminAuditCaptor.getValue();
        assertAll("Admin Audit Record",
                () -> assertThat(adminAudit.getAction(), is(NOTIFICATION)),
                () -> assertThat(adminAudit.getResponseOutcome(), is(ACCEPTED)),
                () -> assertThat(StringUtils.deleteWhitespace(adminAudit.getEuPayload()), equalTo(StringUtils.deleteWhitespace("{\"field1\":\"this is field 1\",\"field2\":\"this is field 2\"}"))),
                () -> assertThat(adminAudit.getRequestTime(), is(notNullValue()))
        );
    }

    @Test
    @DisplayName("Admin audit record can be persisted without EU payload and handed to repository for creation")
    void shouldPersistAdminAuditRecordWithoutPayload() {
        service.save(NOTIFICATION, ACCEPTED, USER_ID);

        verify(dao).save(adminAuditCaptor.capture());

        AdminAudit adminAudit = adminAuditCaptor.getValue();
        assertAll("Admin Audit Record",
                () -> assertThat(adminAudit.getAction(), is(NOTIFICATION)),
                () -> assertThat(adminAudit.getResponseOutcome(), is(ACCEPTED)),
                () -> assertThat(adminAudit.getEuPayload(), is(nullValue())),
                () -> assertThat(adminAudit.getRequestTime(), is(notNullValue()))
        );
    }

    @Test
    @DisplayName("Fetch Admin audit records based on request filter")
    void shouldFetchAdminAuditRecordsBasedOnFilter() {
        //Given
        AdminAuditRequest payload = AdminAuditRequest.builder()
                .build();

        Page<AdminAudit> adminAuditPage = new PageImpl<>(List.of(AdminAudit.buildAdminAudit(RETRIEVAL, ACCEPTED, USER_ID)));

        when(dao.filterBy(payload)).thenReturn(adminAuditPage);

        //When
        AdminAuditResponse adminAuditResponse = service.getAdminAudits(payload);

        //Then
        verify(dao).filterBy(payload);

        assertAll("Filtered Admin Audit Records",
                () -> assertThat(adminAuditResponse.getRecordCount(), is(1L)),
                () -> assertThat(adminAuditResponse.getAdminActions().get(0).getOutcome(), is(ACCEPTED)),
                () -> assertThat(adminAuditResponse.getAdminActions().get(0).getAction(), is(RETRIEVAL))
        );
    }

}