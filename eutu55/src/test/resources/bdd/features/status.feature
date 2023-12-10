Feature: Subscription API test
    Scenario: client makes call to GET /version
      Given Status service is running successfully
      When Request received for status
      Then Status fetch is successful