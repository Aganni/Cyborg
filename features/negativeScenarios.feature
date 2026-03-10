Feature: Negative Scenarios

#  @Negative @External
#  Scenario Outline: Verify 412 Failure for External API with type mismatch
#    Given KSF generate unique stateless identifiers for the request
#    And KSF generate <lpc> payload for external api with <pullType> for <vendor> and parse <parse>
#    When KSF update mandatory string fields with integer in payload based on "ExternalApiMetadata.csv"
#    And KSF hit the External BureauEngine Api with refactored payload expecting 412
#    Then Validate BureauEngine error response status "Failure" and message "type mismatch"
#    Examples:
#      | vendor          | pullType   | lpc   | parse   |
#      | "ConsumerCibil" | "HardPull" | "ESC" | "false" |
