Feature: Bureau Post CreditReport API Scenarios

  @Regression
  Scenario Outline: Verify External Bureau API for both (parse=false) and (parse=true) scenarios for all vendors
    Given KSF generate unique stateless identifiers for the request
    And KSF generate <lpc> payload for external api with <pullType> for <vendor> and parse <parse>
    When KSF hit the external BureauEngine Api expecting 200
    Then KSF validate external response score for vendor <vendor>
    And KSF hit the BureauEngine Api again with same identifiers expecting 412
    Then Validate BureauEngine error response "Failure" and message constant "BUREAU_RECORDS_EXISTS_FOR_EXTENAL_API" for "External" Api
    Examples:
      | vendor              | pullType | lpc   | parse |
      | "ConsumerEquifax"   |"HardPull"| "ESC" |"false"|
      | "CommercialCibil"   |"HardPull"| "KBL" |"false"|
      | "ConsumerCibil"     |"HardPull"| "BPT" |"false"|
      | "ConsumerCibil"     |"SoftPull"| "KBL" |"false"|
      | "ConsumerCrif"      |"HardPull"| "ESC" |"false"|
      | "ConsumerCrif"      |"SoftPull"| "ESC" |"false"|
      | "ConsumerExperian"  |"HardPull"| "ESC" |"false"|
      | "ConsumerExperian"  |"SoftPull"| "ESC" |"false"|
      | "ConsumerCibil"     |"HardPull"| "KBL" |"true" |
      | "ConsumerCibil"     | "NoPull" | "KBL" |"true" |

  @Regression
  Scenario Outline: Verify External Bureau API for both (parse=false) and (parse=true) scenarios at Withdrawal level
    Given KSF generate unique stateless identifiers for the request
    And KSF set withdrawalId as "cyborg2026WithdrawalId" for re-pull
    And KSF generate <lpc> payload for external api with <pullType> for <vendor> and parse <parse>
    When KSF hit the external BureauEngine Api expecting 200
    Then KSF validate external response score for vendor <vendor>
    And KSF hit the BureauEngine Api again with same identifiers expecting 412
    Then Validate BureauEngine error response "Failure" and message constant "BUREAU_RECORDS_EXISTS_FOR_EXTENAL_API" for "External" Api
    Examples:
      | vendor              | pullType | lpc   | parse |
      | "ConsumerEquifax"   |"HardPull"| "ESC" |"false"|
      | "CommercialCibil"   |"HardPull"| "KBL" |"false"|
      | "ConsumerCibil"     |"HardPull"| "BPT" |"false"|
      | "ConsumerCibil"     |"SoftPull"| "KBL" |"false"|
      | "ConsumerCrif"      |"HardPull"| "ESC" |"false"|
      | "ConsumerCrif"      |"SoftPull"| "ESC" |"false"|
      | "ConsumerExperian"  |"HardPull"| "ESC" |"false"|
      | "ConsumerExperian"  |"SoftPull"| "ESC" |"false"|
      | "ConsumerCibil"     |"HardPull"| "KBL" |"true" |
      | "ConsumerCibil"     | "NoPull" | "KBL" |"true"

  @Regression
  Scenario Outline: Verify 412 Failure for External API with empty mandatory fields
    Given KSF generate unique stateless identifiers for the request
    And KSF generate <lpc> payload for external api with <pullType> for <vendor> and parse <parse>
    When KSF update all mandatory fields to empty string in payload based on "ExternalApiMetadata.csv"
    And KSF hit the External BureauEngine Api with refactored payload expecting 412
    Then Validate BureauEngine error response status "Failure" and message "fields missing"
    And Validate all modified fields are present in the error response
    Examples:
      | vendor             | pullType   | lpc   | parse   |
      | "ConsumerCibil"    | "HardPull" | "ESC" | "true"  |
      | "ConsumerExperian" | "HardPull" | "ESC" | "false" |

  @Regression
  Scenario Outline: Verify 412 Failure for External API with null mandatory fields
    Given KSF generate unique stateless identifiers for the request
    And KSF generate <lpc> payload for external api with <pullType> for <vendor> and parse <parse>
    When KSF update all mandatory fields to null in payload based on "ExternalApiMetadata.csv"
    And KSF hit the External BureauEngine Api with refactored payload expecting 412
    Then Validate BureauEngine error response status "Failure" and message "fields missing"
    And Validate all modified fields are present in the error response
    Examples:
      | vendor             | pullType   | lpc   | parse   |
      | "ConsumerCibil"    | "HardPull" | "ESC" | "true"  |
      | "ConsumerExperian" | "HardPull" | "ESC" | "false" |