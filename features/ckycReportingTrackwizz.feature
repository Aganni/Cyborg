Feature: CKYC Reporting through Trackwizz

  Scenario: CKYC Reporting
    Given KSF starts ckyc initiating for different appforms with Trackwizz
    Then KSF hits batch status with UUID to check the reporting status
    Then KSF generates S3 URL for initiated appforms
    Then KSF validates CKYC reporting