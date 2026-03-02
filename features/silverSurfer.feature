Feature: Silver Surfer APIs testing

  Scenario Outline: Premium details api
    When KSF hits premium details api for "<insurancePlatform>" "<planCode>" "<productCode>" <sumInsured> <loanTenure>
    Then verify "<message>" in premium details response
    Examples:
    | insurancePlatform | planCode | productCode | sumInsured | loanTenure | message                                                                                            |
    |     ICICI         |    0     |    BNPL     |  180000    |    18      | BNPL ICICI lombard insurance product is not offered for sum insured > 100000                       |
    |     ICICI         |    0     |    BNPL     |   80000    |    18      | Lending Plan                                                                                       |
    |     ICICI         |    1     |    BNPL     |   80000    |    18      | no matching insurance product found for the given plan code.                                       |
    |     ICICI         |    0     |    IP       |  180000    |    18      | Lending Plan                                                                                       |
    |     ICICI         |    1     |    IP       |  180000    |    18      | Business Loan Plan                                                                                 |
    |     ICICI         |    1     |    IP       |  180000    |    25      | This ICICI lombard insurance product is not offered for loan tenure exceeding 24 months as of now. |
    |     ICICI         |    2     |    IP       |  180000    |    18      | no matching insurance product found for the given plan code.                                       |
    |     ICICI         |    0     |    GSG      |  180000    |    18      | Lending Plan                                                                                       |
    |     ICICI         |    1     |    GSG      |  180000    |    18      | Business Loan Plan                                                                                 |
    |     ICICI         |    2     |    GSG      |  180000    |    18      | Personal Loan Plan                                                                                 |
    |     ICICI         |    3     |    GSG      |  180000    |    18      | no matching insurance product found for the given plan code.                                       |

  Scenario: Insurance policy doc validation
    When KSF hits policy document api
    Then verify the policy document

  Scenario Outline: Policy details api for LAP
    When KSF hits policy details api for "<insurancePlatform>" "<productCode>""<LPC>"
    Then verify "<policyPurchaseStatus>""<productCode>" in policy details response
    Examples:
      | insurancePlatform | productCode | policyPurchaseStatus | LPC |
      | ICICISECURED      | GSM         | SUCCESS              | LAP |
      | ICICISECURED      | GPA         | SUCCESS              | LAP |


  Scenario Outline: Policy details api for EF partners
    When KSF hits policy details api for "<insurancePlatform>" "<productCode>""<LPC>"
    Then verify "<policyPurchaseStatus>""<productCode>" in policy details response
    Examples:
      | insurancePlatform | productCode | policyPurchaseStatus | LPC |
      | ICICI             | IP          | SUCCESS              | NBR |
      | ICICI             | GSG         | SUCCESS              | NRO |
      | ICICI             | BNPL        | SUCCESS              | GRO |

  Scenario Outline: Retrieve policy for a specific applicant
    When KSF hits policy history api for "<productCode>"
    Then verify "<planCode>" "<productCode>" in policy details response
    Examples:
      | planCode | productCode |
      | 0        | BNPL        |
      | 1        | IP          |
      | 1        | GSG         |