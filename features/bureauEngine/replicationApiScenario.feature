Feature: Bureau Replication API Scenarios

  @Regression
  Scenario Outline: Verify successful bureau replication using appFormId and applicantId
    Given KSF set source bureau data for <vendor> with <pullType> from "ReplicationTestData.json"
    And KSF generate unique stateless identifiers for the request
    When KSF generate replication payload using "appFormId" for <vendor> and <pullType> with 1 targets
    And KSF hit the Bureau Replication Api expecting 200
    Then KSF validate bureauEngine replica api "Success" response for the "success" list
    Examples:
      | vendor             | pullType   |
      | "ConsumerExperian" | "HardPull" |
      | "ConsumerCibil"    | "HardPull" |
      | "ConsumerExperian" | "SoftPull" |
      | "CommercialCibil"  | "HardPull"  |
      | "ConsumerCrif"     | "HardPull"  |

  @Regression
  Scenario Outline: Verify successful bureau replication using bureauPullId
    Given KSF set source bureau data for <vendor> with <pullType> from "ReplicationTestData.json"
    And KSF generate unique stateless identifiers for the request
    When KSF generate replication payload using "bureauPullId" for <vendor> and <pullType> with 1 targets
    And KSF hit the Bureau Replication Api expecting 200
    Then KSF validate bureauEngine replica api "Success" response for the "success" list
    Examples:
      | vendor             | pullType   |
      | "ConsumerExperian" | "HardPull" |
      | "ConsumerCibil"    | "HardPull" |
      | "ConsumerExperian" | "SoftPull" |
      | "CommercialCibil"  | "HardPull"  |
      | "ConsumerCrif"     | "HardPull"  |

  @Regression
  Scenario Outline: Verify multi-target replication with mixed withdrawalId
    Given KSF set source bureau data for <vendor> with <pullType> from "ReplicationTestData.json"
    When KSF generate replication payload using "bureauPullId" for <vendor> and <pullType> with 4 targets
    And KSF hit the Bureau Replication Api expecting 200
    Then KSF validate bureauEngine replica api "Success" response for the "success" list
    Examples:
      | vendor             | pullType   |
      | "ConsumerExperian" | "HardPull" |

  @Regression
  Scenario Outline: Verify replication for same Targets
    Given KSF set source bureau data for <vendor> with <pullType> from "ReplicationTestData.json"
    And KSF generate unique stateless identifiers for the request
    When KSF generate replication payload using "bureauPullId" for <vendor> and <pullType> with 1 targets
    And KSF hit the Bureau Replication Api expecting 200
    And KSF hit the Bureau Replication Api with same target expecting 200
    Then KSF validate bureauEngine replica api "Success" response for the "skipped" list
    Examples:
      | vendor             | pullType   |
      | "ConsumerExperian" | "HardPull" |

  @Regression
  Scenario Outline: Verify 412 scenario by sending null for mandatory fields
    Given KSF set source bureau data for <vendor> with <pullType> from "ReplicationTestData.json"
    And KSF generate unique stateless identifiers for the request
    When KSF generate replication payload using "bureauPullId" for <vendor> and <pullType> with 1 targets
    When KSF update all mandatory fields to null in payload based on "ReplicationApiMetadata.csv"
    And KSF hit the Bureau Replication Api expecting 412
    Then Validate BureauEngine error response status "Failure" and message "fields missing"
    And Validate all modified fields are present in the error response
    Examples:
      | vendor             | pullType   |
      | "ConsumerExperian" | "HardPull" |