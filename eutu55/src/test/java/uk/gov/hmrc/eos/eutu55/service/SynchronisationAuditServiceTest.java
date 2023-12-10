package uk.gov.hmrc.eos.eutu55.service;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmrc.eos.eutu55.dao.SynchronisationAuditDao;
import uk.gov.hmrc.eos.eutu55.model.IossVat;
import uk.gov.hmrc.eos.eutu55.model.IossVatAudit;
import uk.gov.hmrc.eos.eutu55.utils.OperationType;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.verify;
import static uk.gov.hmrc.eos.eutu55.IossVatTestHelper.iossVatCreateOf;
import static uk.gov.hmrc.eos.eutu55.IossVatTestHelper.iossVatDeleteOf;
import static uk.gov.hmrc.eos.eutu55.IossVatTestHelper.iossVatUpdateOf;
import static uk.gov.hmrc.eos.eutu55.utils.OperationType.CREATE;
import static uk.gov.hmrc.eos.eutu55.utils.OperationType.DELETE;
import static uk.gov.hmrc.eos.eutu55.utils.OperationType.UPDATE;

@ExtendWith(MockitoExtension.class)
class SynchronisationAuditServiceTest {

    @Mock
    private SynchronisationAuditDao dao;

    @InjectMocks
    private SynchronisationAuditService service;

    @Captor
    ArgumentCaptor<List<IossVatAudit>> auditRecordsCaptor;

    @Test
    @DisplayName("IOSS VAT records with the combination of create, update and delete operations can be audited")
    void iossVatRecordsCanBeAudited() {

        List<IossVat> iossVatRecordsForAuditing = List.of(
                iossVatCreateOf("IM0000000001"),
                iossVatUpdateOf("IM0000000002"),
                iossVatDeleteOf("IM0000000003")
        );
        service.audit(iossVatRecordsForAuditing);

        verify(dao).save(auditRecordsCaptor.capture());

        List<IossVatAudit> auditRecords = auditRecordsCaptor.getValue();
        assertAll("Audit Records",
                () -> assertThat(auditRecords, Matchers.<Collection<IossVatAudit>>allOf(
                        hasSize(3),
                        hasItems(hasIossVatRecords("IM0000000001", "IM0000000002", "IM0000000003")))),
                () -> assertThat(operationTypes(auditRecords), Matchers.<Collection<OperationType>>allOf(
                        hasSize(3),
                        hasItems(CREATE, UPDATE, DELETE)))
        );
    }

    @Test
    @DisplayName("IOSS VAT records with create, update, delete operations for same ioss vat id can be audited")
    void iossVatRecordsWithMultipleIossVatIdCanBeAudited() {

        List<IossVat> iossVatRecordsForAuditing = List.of(
                iossVatCreateOf("IM0000000001"),
                iossVatUpdateOf("IM0000000001"),
                iossVatDeleteOf("IM0000000001"),
                iossVatDeleteOf("IM0000000001"),
                iossVatCreateOf("IM0000000002"),
                iossVatCreateOf("IM0000000002"),
                iossVatUpdateOf("IM0000000002")
        );
        service.audit(iossVatRecordsForAuditing);

        verify(dao).save(auditRecordsCaptor.capture());

        List<IossVatAudit> auditRecords = auditRecordsCaptor.getValue();
        assertAll("Audit Records",
                () -> assertThat(auditRecords, Matchers.<Collection<IossVatAudit>>allOf(
                        hasSize(7),
                        hasItems(hasIossVatRecords("IM0000000001", "IM0000000002")))),
                () -> assertThat(operationTypes(auditRecords), Matchers.<Collection<OperationType>>allOf(
                        hasSize(7),
                        hasItems(CREATE, UPDATE, DELETE)))
        );
    }

    private Matcher<IossVatAudit>[] hasIossVatRecords(String... iossVatIds) {
        return Stream.of(iossVatIds).map(iossVatId -> Matchers.<IossVatAudit>hasProperty("iossVat", hasProperty("iossVatId", is(iossVatId)))).toArray(Matcher[]::new);
    }

    private List<OperationType> operationTypes(List<IossVatAudit> auditRecords) {
        return auditRecords.stream().map(IossVatAudit::getOperationType).collect(Collectors.toList());
    }

}