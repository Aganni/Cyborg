Feature: Bureau Replication API Scenarios

  @Positive @Replication
  Scenario Outline: Verify successful bureau replication using appFormId and applicantId
    Given KSF set source bureau data for <vendor> with <pullType> from "ReplicationTestData.json"
    And KSF generate unique stateless identifiers for the request
    When KSF generate replication payload using "appFormId" for <vendor> and <pullType> with 1 targets
    And KSF hit the Bureau Replication Api expecting 200
    Then KSF validate bureauEngine replica api "Success" response for the "success" list
    And KSF validate report data consistency for <vendor>
    Examples:
      | vendor             | pullType   |
      | "ConsumerExperian" | "HardPull" |
#      | "ConsumerCibil"    | "HardPull" |
#      | "ConsumerExperian" | "SoftPull" |
#      | "CommercialCibil"  | "HardPull"  |
#      | "ConsumerCrif"     | "HardPull"  |

  @Positive @Replication
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

  @Positive @Replication @MultiTarget
  Scenario Outline: Verify multi-target replication with mixed withdrawalId
    Given KSF set source bureau data for <vendor> with <pullType> from "ReplicationTestData.json"
    When KSF generate replication payload using "bureauPullId" for <vendor> and <pullType> with 4 targets
    And KSF hit the Bureau Replication Api expecting 200
    Then KSF validate bureauEngine replica api "Success" response for the "success" list
    Examples:
      | vendor             | pullType   |
      | "ConsumerExperian" | "HardPull" |

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


  @Negative @Replication
  Scenario Outline: Verify failure when replication source mismatch with vendor
    Given KSF set source bureau data for <vendor> with <pullType> from "ReplicationTestData.json"
    And KSF generate unique stateless identifiers for the request
    When KSF generate replication payload using "bureauPullId" for <vendor> and <pullType> with 1 targets
    And KSF hit the Bureau Replication Api expecting 412
    Then Validate BureauEngine response having status "Failure" for "Replication"
    Examples:
      | vendor             | pullType   |
      | "MismatchVendor"   | "HardPull" |

  @Positive @Replication @Withdrawal
  Scenario Outline: Verify successful bureau replication at withdrawal level
    Given KSF set source bureau data for <vendor> with <pullType> from "ReplicationTestData.json"
    And KSF generate unique stateless identifiers for the request
    And KSF set withdrawalId as "target-withdrawal-456" for re-pull
    And KSF set target LPC as "NVI"
    When KSF generate replication payload using "appFormId" for <vendor> with <pullType>
    And KSF hit the Bureau Replication Api expecting 200
    Then Validate BureauEngine response having status "Success" for "Replication"
    Examples:
      | vendor             | pullType   |
      | "ConsumerExperian" | "HardPull" |



  @Positive @Replication @Duplicate
  Scenario Outline: Verify replication is skipped for duplicate requests
    Given KSF set source bureau data for <vendor> from "ReplicationTestData.json"
    And KSF generate unique stateless identifiers for the request
    When KSF generate replication payload using "appFormId" for <vendor> with <pullType>
    And KSF hit the Bureau Replication Api expecting 200
    And KSF hit the Bureau Replication Api expecting 200
    Then Validate BureauEngine response having status "Success" for "Replication"
    Examples:
      | vendor             | pullType   |
      | "ConsumerExperian" | "HardPull" |


  @Positive @Replication @Consistency
  Scenario Outline: Verify report data consistency after replication
    Given KSF set source bureau data for <vendor> from "ReplicationTestData.json"
    And KSF generate unique stateless identifiers for the request
    When KSF generate replication payload using "appFormId" for <vendor> with <pullType>
    And KSF hit the Bureau Replication Api expecting 200
    Then Validate BureauEngine response having status "Success" for "Replication"
    And KSF validate report data consistency for <vendor>
    Examples:
      | vendor             | pullType   |
      | "ConsumerExperian" | "HardPull" |
