Feature: Bureau Replication API Scenarios

  @Positive @Replication
  Scenario Outline: Verify successful bureau replication using appFormId and applicantId
    Given KSF set source bureau data for <vendor> from "ReplicationTestData.json"
    And KSF generate unique stateless identifiers for the request
    When KSF generate replication payload using "appFormId" for <vendor> with <pullType>
    And KSF hit the Bureau Replication Api expecting 200
    Then Validate BureauEngine response having status "Success" for "Replication"
    Examples:
      | vendor             | pullType   |
      | "ConsumerExperian" | "HardPull" |
      | "ConsumerCibil"    | "HardPull" |

  @Positive @Replication
  Scenario Outline: Verify successful bureau replication using bureauPullId
    Given KSF set source bureau data for <vendor> from "ReplicationTestData.json"
    And KSF generate unique stateless identifiers for the request
    When KSF generate replication payload using "bureauPullId" for <vendor> with <pullType>
    And KSF hit the Bureau Replication Api expecting 200
    Then Validate BureauEngine response having status "Success" for "Replication"
    Examples:
      | vendor             | pullType   |
      | "ConsumerExperian" | "HardPull" |

  @Positive @Replication @Withdrawal
  Scenario Outline: Verify successful bureau replication at withdrawal level
    Given KSF set source bureau data for <vendor> from "ReplicationTestData.json"
    And KSF set source withdrawalId as "source-withdrawal-123"
    And KSF generate unique stateless identifiers for the request
    And KSF set withdrawalId as "target-withdrawal-456" for re-pull
    And KSF set target LPC as "NVI"
    When KSF generate replication payload using "appFormId" for <vendor> with <pullType>
    And KSF hit the Bureau Replication Api expecting 200
    Then Validate BureauEngine response having status "Success" for "Replication"
    Examples:
      | vendor             | pullType   |
      | "ConsumerExperian" | "HardPull" |

  @Negative @Replication
  Scenario Outline: Verify failure when replication source mismatch with vendor
    Given KSF set source bureau data for "MismatchVendor" from "ReplicationTestData.json"
    And KSF generate unique stateless identifiers for the request
    When KSF generate replication payload using "appFormId" for <vendor> with <pullType>
    And KSF hit the Bureau Replication Api expecting 412
    Then Validate BureauEngine response having status "Failure" for "Replication"
    Examples:
      | vendor             | pullType   |
      | "ConsumerExperian" | "HardPull" |
