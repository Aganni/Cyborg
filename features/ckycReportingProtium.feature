Feature: CKYC Reporting Protium

  Scenario: CKYC Reporting through Protium
    Given KSF starts ckyc initiating for different appforms with Protium
    Then KSF hits batch status with Request ID to check the reporting status
#    Then KSF validates CKYC reporting for Protium