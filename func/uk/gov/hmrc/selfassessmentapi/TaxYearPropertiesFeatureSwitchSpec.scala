package uk.gov.hmrc.selfassessmentapi

import play.api.libs.json.Json
import play.api.test.FakeApplication
import uk.gov.hmrc.support.BaseFunctionalSpec

class SwitchPensionContributionOnSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.pensionContribution.enabled" -> true))

  "if pension contribution is turned on then tax year resource" should {
    "return 200" in {

      val payload = Json.parse(
        s"""
           |{
           | 		"ukRegisteredPension": 1000.45,
           | 		"retirementAnnuity": 1000.0,
           | 		"employerScheme": 12000.05,
           | 		"overseasPension": 1234.43
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear/pensionContribution", Some(payload))
        .thenAssertThat()
        .statusIs(201)

      given()
        .when()
        .put(s"/sandbox/$saUtr/$taxYear/pensionContribution", Some(payload))
        .thenAssertThat()
        .statusIs(201)
    }
  }
}

class SwitchPensionContributionOffSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.pensionContribution.enabled" -> false))

  "if pension contribution is turned off then tax year resource" should {
    "return 404 not found with the correct error code" in {

      val payload = Json.parse(
        s"""
           |{
           | 		"ukRegisteredPension": 1000.45,
           | 		"retirementAnnuity": 1000.0,
           | 		"employerScheme": 12000.05,
           | 		"overseasPension": 1234.43
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear/pensionContribution", Some(payload))
        .thenAssertThat()
        .isNotFound

      given()
        .when()
        .put(s"/sandbox/$saUtr/$taxYear/pensionContribution", Some(payload))
        .thenAssertThat()
        .isNotFound
    }
  }
}

class SwitchCharitableGivingsOnSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.charitableGiving.enabled" -> true))

  "if charitable givings are turned on then tax year resource" should {
    "return 200" in {

      val payload = Json.parse(
        s"""
           |{
           |     "giftAidPayments": {
           |       "totalInTaxYear": 10000.0,
           |       "oneOff": 5000.0,
           |       "toNonUkCharities": 1000.0,
           |       "carriedBackToPreviousTaxYear": 1000.0,
           |       "carriedFromNextTaxYear": 2000.0
           |     },
           |     "sharesSecurities": {
           |       "totalInTaxYear": 2000.0,
           |       "toNonUkCharities": 500.0
           |     },
           |     "landProperties":  {
           |       "totalInTaxYear": 4000.0,
           |       "toNonUkCharities": 3000.0
           |     }
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear/charitableGiving", Some(payload))
        .thenAssertThat()
        .statusIs(201)

      given()
        .when()
        .put(s"/sandbox/$saUtr/$taxYear/charitableGiving", Some(payload))
        .thenAssertThat()
        .statusIs(201)
    }
  }
}

class SwitchCharitableGivingsOffSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.charitableGiving.enabled" -> false))

  "if charitable givings are turned off then tax year resource" should {
    "return 404 not found with the correct error code" in {

      val payload = Json.parse(
        s"""
           |{
           |     "giftAidPayments": {
           |       "totalInTaxYear": 10000.0,
           |       "oneOff": 5000.0,
           |       "toNonUkCharities": 1000.0,
           |       "carriedBackToPreviousTaxYear": 1000.0,
           |       "carriedFromNextTaxYear": 2000.0
           |     },
           |     "sharesSecurities": {
           |       "totalInTaxYear": 2000.0,
           |       "toNonUkCharities": 500.0
           |     },
           |     "landProperties":  {
           |       "totalInTaxYear": 4000.0,
           |       "toNonUkCharities": 3000.0
           |     }
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear/charitableGiving", Some(payload))
        .thenAssertThat()
        .isNotFound

      given()
        .when()
        .put(s"/sandbox/$saUtr/$taxYear/charitableGiving", Some(payload))
        .thenAssertThat()
        .isNotFound
    }
  }
}

class SwitchBlindPersonOnSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.blindPerson.enabled" -> true))

  "if blind person is turned on then tax year resource" should {
    "return 200" in {

      val payload = Json.parse(
        s"""
           |{
           | 		"country": "Wales",
           | 		"registrationAuthority": "Registrar",
           | 		"spouseSurplusAllowance": 2000.05,
           | 		"wantSpouseToUseSurplusAllowance": true
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear/blindPerson", Some(payload))
        .thenAssertThat()
        .statusIs(201)

      given()
        .when()
        .put(s"/sandbox/$saUtr/$taxYear/blindPerson", Some(payload))
        .thenAssertThat()
        .statusIs(201)
    }
  }
}

class SwitchBlindPersonOffSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.blindPerson.enabled" -> false))

  "if blind person is turned off then tax year resource" should {
    "return 404 not found with the correct error code" in {

      val payload = Json.parse(
        s"""
           |{
           | 		"country": "Wales",
           | 		"registrationAuthority": "Registrar",
           | 		"spouseSurplusAllowance": 2000.05,
           | 		"wantSpouseToUseSurplusAllowance": true
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear/blindPerson", Some(payload))
        .thenAssertThat()
        .isNotFound

      given()
        .when()
        .put(s"/sandbox/$saUtr/$taxYear/blindPerson", Some(payload))
        .thenAssertThat()
        .isNotFound
    }
  }
}

class SwitchStudentLoanOnSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.studentLoan.enabled" -> true))

  "if student loan is turned on then tax year resource" should {
    "return 200" in {

      val payload = Json.parse(
        s"""
           |{
           |     "planType": "Plan1",
           |     "deductedByEmployers": 2000.00
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear/studentLoan", Some(payload))
        .thenAssertThat()
        .statusIs(201)
    }
  }
}

class SwitchStudentLoanOffSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.studentLoan.enabled" -> false))

  "if student loan is turned off then tax year resource" should {
    "return 404 not found with the correct error code" in {

      val payload = Json.parse(
        s"""
           |{
           |     "planType": "Plan1",
           |     "deductedByEmployers": 2000.00
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear/studentLoan", Some(payload))
        .thenAssertThat()
        .isNotFound

      given()
        .when()
        .put(s"/sandbox/$saUtr/$taxYear/studentLoan", Some(payload))
        .thenAssertThat()
        .isNotFound
    }
  }
}

class SwitchTaxRefundedOrSetOffOnSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.taxRefundedOrSetOff.enabled" -> true))

  "if tax refunded or set off is turned on then tax year resource" should {
    "return 200" in {

      val payload = Json.parse(
        s"""
           |{
           |     "amount": 2000.00
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear/taxRefundedOrSetOff", Some(payload))
        .thenAssertThat()
        .statusIs(201)

      given()
        .when()
        .put(s"/sandbox/$saUtr/$taxYear/taxRefundedOrSetOff", Some(payload))
        .thenAssertThat()
        .statusIs(201)
    }
  }
}

class SwitchTaxRefundedOrSetOffOffSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.taxRefundedOrSetOff.enabled" -> false))

  "if tax refunded or set off is turned off then tax year resource" should {
    "return 404 not found with the correct error code" in {

      val payload = Json.parse(
        s"""
           |{
           |     "amount": 2000.00
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear/taxRefundedOrSetOff", Some(payload))
        .thenAssertThat()
        .isNotFound

      given()
        .when()
        .put(s"/sandbox/$saUtr/$taxYear/taxRefundedOrSetOff", Some(payload))
        .thenAssertThat()
        .isNotFound
    }
  }
}

class SwitchChildBenefitOnSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.childBenefit.enabled" -> true))

  "if child benefit is turned on then tax year resource" should {
    "return 200" in {

      val payload = Json.parse(
        s"""
           |{
           |    "amount": 1234.34,
           |    "numberOfChildren": 3,
           |    "dateBenefitStopped": "2016-04-06"
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear/childBenefit", Some(payload))
        .thenAssertThat()
        .statusIs(201)

      given()
        .when()
        .put(s"/sandbox/$saUtr/$taxYear/childBenefit", Some(payload))
        .thenAssertThat()
        .statusIs(201)
    }
  }
}

class SwitchChildBenefitOffSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.childBenefit.enabled" -> false))

  "if child benefit is turned off then tax year resource" should {
    "return 404 not found with the correct error code" in {

      val payload = Json.parse(
        s"""
           |{
           |    "amount": 1234.34,
           |    "numberOfChildren": 3,
           |    "dateBenefitStopped": "2016-04-06"
           |}
        """.stripMargin)

      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear/childBenefit", Some(payload))
        .thenAssertThat()
        .isNotFound

      given()
        .when()
        .put(s"/sandbox/$saUtr/$taxYear/childBenefit", Some(payload))
        .thenAssertThat()
        .isNotFound
    }
  }
}
