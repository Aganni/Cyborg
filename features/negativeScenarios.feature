Feature: BureauEngine Negative Scenarios

  @Negative
  Scenario Outline: Verify 400 Bad Request for missing mandatory fields
    Given KSF generate unique stateless identifiers for the request
    And KSF prepare bureau request for <vendor> with <pullType>
    When KSF remove all mandatory fields from the payload based on "BureauEngineMetadata.json"
    And KSF hit the BureauEngine Api with prepared payload expecting 412
    Then Validate BureauEngine error response status "Failure" and message "fields missing"
    And Validate BureauEngine error response for missing mandatory fields
    Examples:
      | vendor             |  pullType   |
      | "ConsumerCibil"    | "HardPull"  |
      | "ConsumerExperian" | "HardPull"  |

  @Negative
  Scenario Outline: Verify failure for empty mandatory fields
    Given KSF generate unique stateless identifiers for the request
    And KSF prepare bureau request for <vendor> with <pullType>
    When KSF remove all mandatory fields from the payload based on "BureauEngineMetadata_Empty.json"
    And KSF hit the BureauEngine Api with prepared payload expecting 412
    Then Validate BureauEngine error response status "Failure" and message "fields missing"
    And Validate BureauEngine error response for missing mandatory fields
    Examples:
      | vendor             |  pullType   |
      | "ConsumerCibil"    | "HardPull"  |
