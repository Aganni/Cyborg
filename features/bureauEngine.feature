Feature: BureauEngine Scenarios

  @Regression
  Scenario Outline: Successful BureauPull for all vendors for the first time pull
    Given KSF generate unique stateless identifiers for the request
    When KSF hit the BureauEngine Api for <vendor> BureauPull with <pullType> payload expecting 200
    Then Validate BureauEngine response having status "Success" for <vendor>
    And KSF hit the BureauEngine Api again with same identifiers expecting 412
    Then Validate BureauEngine error response "Failure" and message constant "BUREAU_RECORDS_EXISTS" for "CreditReport" Api
    Examples:
      | vendor             |  pullType   |
      | "ConsumerCibil"    | "HardPull"  |
      | "ConsumerExperian" | "HardPull"  |
      | "CommercialCibil"  | "HardPull"  |
      | "ConsumerCrif"     | "HardPull"  |
      | "ConsumerExperian" | "SoftPull"  |


  @Regression
  Scenario Outline:Successful BureauPull at Withdrawal level
    Given KSF generate unique stateless identifiers for the request
    And KSF set withdrawalId as "cyborg2026WithdrawalId" for re-pull
    When KSF hit the BureauEngine Api for <vendor> BureauPull with <pullType> payload expecting 200
    Then Validate BureauEngine response having status "Success" for <vendor>
    And KSF hit the BureauEngine Api again with same identifiers expecting 412
    Then Validate BureauEngine error response "Failure" and message constant "BUREAU_RECORDS_EXISTS" for "CreditReport" Api
    Examples:
      | vendor             |  pullType   |
      | "ConsumerCibil"    | "HardPull"  |
      | "ConsumerExperian" | "HardPull"  |
      | "CommercialCibil"  | "HardPull"  |
      | "ConsumerCrif"     | "HardPull"  |
      | "ConsumerExperian" | "SoftPull"  |

  @Regression
  Scenario Outline: Verify Bureau Pull with different KYC documents for all vendors
    Given KSF generate unique stateless identifiers for the request
    When KSF hit BureauEngine for <vendor> with PullType <PullType>, KycType <Type> and value <Value> with <statuCode>
    Then Validate BureauEngine response having status <status> for <vendor>
    And KSF verify the <vendor> bureau "Request" XML has "IdType" as <code>
    # kyc Type "panCard" is covered in the Happy flow scenario, so not included here
    Examples:
      | vendor             | PullType   | Type             | Value            | status  | statuCode |code |
      | "ConsumerCibil"    | "HardPull" | "voterId"        | "GJT9812930"     |"Success"| 200       |"03" |
      | "ConsumerCibil"    | "HardPull" | "drivingLicence" | "DL992023888376" |"Success"| 200       |"04" |
      | "ConsumerCibil"    | "HardPull" | "passport"       | "M00988875"      |"Success"| 200       |"02" |
      | "ConsumerCibil"    | "HardPull" | "rationCard"     | "2008120987"     |"Success"| 200       |"05" |
      | "ConsumerCibil"    | "HardPull" | "aadhaar"        | "2008120987"     |"Failure"| 412       | ""  |
      | "ConsumerExperian" | "HardPull" | "rationCard"     | "2008120987"     |"Failure"| 412       | ""  |
      | "CommercialCibil"  | "HardPull" | "rationCard"     | "2008120987"     |"Failure"| 412       | ""  |
      | "ConsumerCrif"     | "HardPull" | "rationCard"     | "2008120987"     |"Failure"| 412       | ""  |
      | "ConsumerExperian" | "Softpull" | "rationCard"     | "2008120987"     |"Failure"| 412       | ""  |

  @Regression
  Scenario Outline: Verify Bureau Pull with different Loan Types for ConsumerExperian
    Given KSF generate unique stateless identifiers for the request
    And KSF prepare bureau request for <vendor> with <pullType>
    When KSF update payload path "loanType" with value <loanType>
    And KSF hit the BureauEngine Api with prepared payload expecting <statusCode>
    Then Validate BureauEngine response having status <status> for <vendor>
    And KSF verify the <vendor> bureau "Request" XML has "EnquiryReason" as <code>
    # LoanType "Personal" is covered in the Happy flow scenario, so not included here
    Examples:
      | vendor             | pullType   | loanType   | status    | statusCode | code |
      | "ConsumerExperian" | "HardPull" | "business" | "Success" | 200        | "3"  |
      | "ConsumerExperian" | "HardPull" | "others"   | "Success" | 200        | "99" |
      | "ConsumerExperian" | "HardPull" | "secured"  | "Failure" | 412        | ""   |
      | "ConsumerExperian" | "SoftPull" | "business" | "Success" | 200        | "3"  |
      | "ConsumerExperian" | "SoftPull" | "others"   | "Success" | 200        | "99" |
      | "ConsumerExperian" | "SoftPull" | "secured"  | "Failure" | 412        | ""   |

  @Regression
  Scenario Outline: Verify Bureau Pull with different Loan Types for Consumer Cibil
    Given KSF generate unique stateless identifiers for the request
    And KSF prepare bureau request for <vendor> with <pullType>
    When KSF update payload path "loanType" with value <loanType>
    And KSF hit the BureauEngine Api with prepared payload expecting <statusCode>
    Then Validate BureauEngine response having status <status> for <vendor>
    And KSF verify the <vendor> bureau "Request" XML has "Purpose" as <code>
    # LoanType "Personal" is covered in the Happy flow scenario, so not included here
    Examples:
      | vendor            | pullType   | loanType   | status    | statusCode | code |
      | "ConsumerCibil"   | "HardPull" | "business" | "Success" | 200        | "50" |
      | "ConsumerCibil"   | "HardPull" | "unsecured"| "Success" | 200        | "61" |
      | "ConsumerCibil"   | "HardPull" | "secured"  | "Success" | 200        | "61" |
      | "ConsumerCibil"   | "HardPull" | "consumer" | "Success" | 200        | "06" |
      | "ConsumerCibil"   | "HardPull" | "others"   | "Failure" | 412        | "50" |

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
      | "ConsumerCibil"     | "NoPull" | "KBL" |"true" |
