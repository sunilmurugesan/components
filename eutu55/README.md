# PDS EUTU55 Service

PDS EUTU55 Service represents a set of Rest API's exposed for the purpose of inbound IOSS-DR message processing and outbound Admin actions.
The resulting data is stored in [BOLT PDS DB](http://10.102.81.44/bolt/eos-db).

Following Inbound IOSS-DR message processing operations are exposed:

* Dissemination / Synchronisation - Receives, validates, processes and persists the IOSS-DR dissemination data
* Notification - To notify the end of retrieval processing. This is a callback as a result of Retrieval call

Following Outbound Admin action operations are exposed:

* Subscription - to manage subscription to the dissemination process. This updates the subscription to activate or deactivate Dissemination, or update the email contact details for the subscription
* Retrieval - to request a retransmission of data from a specified date in the event of some discrepancy
* Subscription Status Enquiry - to check the current Subscription status 
* Status - to retrieve the last know dissemination state from the central IOSS-DR system
* Ping - to test the health of the connection to the IOSS-DR Central system

Apart from these main operations, the following REST operation has also been exposed which is consumed by the Admin UI internally. 

* Get Admin Action Audit Data - Retrieve the Admin Action Audit data stored in PDS database based on the filter criteria

<table>
    <tr>
        <th>API</th>
        <th>Endpoint</th>
        <th>Operation</th>
    </tr>
    <tr>
        <td>Dissemination / Synchronisation</td>
        <td>{BASE_URL}/pds/cnit/eutu55/synchronisation/v1</td>
        <td>POST</td>
    </tr>
    <tr>
        <td>Notification</td>
        <td>{BASE_URL}/pds/cnit/eutu55/notificationcbs/v1</td>
        <td>POST</td>
    </tr>
    <tr>
        <td>Subscription</td>
        <td>{BASE_URL}/pds/cnit/eutu55/subscription/v1</td>
        <td>POST</td>
    </tr>
    <tr>
        <td>Retrieval</td>
        <td>{BASE_URL}/pds/cnit/eutu55/retrieval/v1</td>
        <td>POST</td>
    </tr>
    <tr>
        <td>Subscription Status</td>
        <td>{BASE_URL}/pds/cnit/eutu55/subscription/status/v1</td>
        <td>GET</td>
    </tr>
    <tr>
        <td>Status</td>
        <td>{BASE_URL}/pds/cnit/eutu55/status/v1</td>
        <td>GET</td>
    </tr>
    <tr>
        <td>Ping</td>
        <td>{BASE_URL}/pds/cnit/eutu55/ping/v1</td>
        <td>GET</td>
    </tr>
    <tr>
        <td>Admin Action Audit</td>
        <td>{BASE_URL}/pds/cnit/eutu55/admin/audit/v1</td>
        <td>POST</td>
    </tr>
</table>

Refer to [PDS EUTU55 LLD](https://cds-confluence.t.cit.corp.hmrc.gov.uk/pages/viewpage.action?pageId=106496421) for more info

## Getting Started


### Prerequisites

In order to set up pds-eutu55-service in a local environment, make sure the PDS DB has been installed.
Checkout project [eos-db](http://10.102.81.44/bolt/eos-db) from GitLab and follow instructions as detailed in [README.md](http://10.102.81.44/bolt/eos-db/blob/developR2/README.md) for eos-db project.

### Installing

Clone the project [pds-eutu55-service](http://10.102.81.44/bolt/pds-eutu55-service) from GitLab.

### Building the project

pds-eutu55-service project build life cycle is managed through Maven. In order to build and package the application simply issue the following command

```mvn clean install```

By default this command will also execute the unit tests.

### Running the (unit) tests

pds-eutu55-service project build life cycle is managed through Maven. In order to run the unit test suite simply run Maven test phase from the command line:

```mvn test```

Code coverage report as a result of running unit tests can be found at:
* [pds-eutu55-service/target/jacoco-report/index.html]()

### Running pds-eutu55-service

Run the main EUTU55Application Spring Boot Launcher class in IDE (or ```mvn clean install spring-boot:run``` in the terminal window)
