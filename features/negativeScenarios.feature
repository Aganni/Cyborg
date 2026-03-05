Feature: BureauEngine Negative Scenarios

  @Negative
  Scenario Outline: Verify 412 Failure for empty mandatory fields
    Given KSF generate unique stateless identifiers for the request
    And KSF prepare bureau request for <vendor> with <pullType>
    When KSF update all mandatory fields to empty string in payload based on "BureauEngineMetadata.csv"
    And KSF hit the BureauEngine Api with refactored payload expecting 412
    Then Validate BureauEngine error response status "Failure" and message "fields missing"
    Examples:
      | vendor             |  pullType   |
      | "ConsumerCibil"    | "HardPull"  |

  @Negative
  Scenario Outline: Verify 412 Failure for null mandatory fields
    Given KSF generate unique stateless identifiers for the request
    And KSF prepare bureau request for <vendor> with <pullType>
    When KSF update all mandatory fields to null in payload based on "BureauEngineMetadata.csv"
    And KSF hit the BureauEngine Api with refactored payload expecting 412
    Then Validate BureauEngine error response status "Failure" and message "fields missing"
    Examples:
      | vendor             |  pullType   |
      | "ConsumerCibil"    | "HardPull"  |

  @Negative
  Scenario Outline: Verify 200 Success after removing non-mandatory fields
    Given KSF generate unique stateless identifiers for the request
    And KSF prepare bureau request for <vendor> with <pullType>
    When KSF remove all non-mandatory fields from payload based on "BureauEngineMetadata.csv"
    And KSF hit the BureauEngine Api with refactored payload expecting 200
    Then Validate BureauEngine response is 200 and check no null values
    Examples:
      | vendor             |  pullType   |
      | "ConsumerCibil"    | "HardPull"  |
      | "ConsumerExperian" | "HardPull"  |

  @Negative
  Scenario Outline: Verify 412 Failure for type mismatch
    Given KSF generate unique stateless identifiers for the request
    And KSF prepare bureau request for <vendor> with <pullType>
    When KSF update mandatory integer fields with string in payload based on "BureauEngineMetadata.csv"
    And KSF hit the BureauEngine Api with refactored payload expecting 412
    Then Validate BureauEngine error response status "Failure" and message "type mismatch"
    Examples:
      | vendor             |  pullType   |
      | "ConsumerCibil"    | "HardPull"  |

