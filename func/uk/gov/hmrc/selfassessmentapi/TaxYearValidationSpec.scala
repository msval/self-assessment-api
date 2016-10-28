package uk.gov.hmrc.selfassessmentapi

import play.api.libs.json.Json
import play.api.test.FakeApplication
import uk.gov.hmrc.support.BaseFunctionalSpec

// FIXME: Refactor into live and sandbox tests

class TaxYearValidationSpec extends BaseFunctionalSpec {
  override lazy val app: FakeApplication = FakeApplication(additionalConfiguration = Map("Test.feature-switch.pensionContribution.enabled" -> true,
                                                                                         "Test.feature-switch.charitableGiving.enabled" -> true,
                                                                                         "Test.feature-switch.blindPerson.enabled" -> true,
                                                                                         "Test.feature-switch.studentLoan.enabled" -> true,
                                                                                         "Test.feature-switch.taxRefundedOrSetOff.enabled" -> true,
                                                                                         "Test.feature-switch.childBenefit.enabled" -> true))

  "the payload is invalid for a live request, they" should {
    "receive 400 if the dateBenefitStopped is before the end of tax year from the url " in {
      val payload = Json.parse(
        s"""
           |{
           |  "amount": 1234.34,
           |  "numberOfChildren": 3,
           |  "dateBenefitStopped": "2016-04-05"
           |}
        """.stripMargin)
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear/childBenefit", Some(payload))
        .thenAssertThat()
        .isValidationError("/childBenefit/dateBenefitStopped", "BENEFIT_STOPPED_DATE_INVALID")
    }
  }

  "the payload is invalid for a live request, they" should {
    "receive 400 if the dateBenefitStopped is after the end of tax year from the url " in {
      val payload = Json.parse(
        s"""
           |{
           |  "amount": 1234.34,
           |  "numberOfChildren": 3,
           |  "dateBenefitStopped": "2017-04-06"
           |}
        """.stripMargin)
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear/childBenefit", Some(payload))
        .thenAssertThat()
        .isValidationError("/childBenefit/dateBenefitStopped", "BENEFIT_STOPPED_DATE_INVALID")
    }
  }


  "the payload is invalid for a sandbox request, they" should {
    "receive 400 if the dateBenefitStopped is before the end of tax year from the url " in {
      val payload = Json.parse(
        s"""
           |{
           |  "amount": 1234.34,
           |  "numberOfChildren": 3,
           |  "dateBenefitStopped": "2016-04-05"
           |}
        """.stripMargin)
      when()
        .put(s"/sandbox/$saUtr/$taxYear/childBenefit", Some(payload))
        .thenAssertThat()
        .isValidationError("/childBenefit/dateBenefitStopped", "BENEFIT_STOPPED_DATE_INVALID")
    }
  }

  "the payload is invalid for a sandbox request, they" should {
    "receive 400 if the dateBenefitStopped is after the end of tax year from the url " in {
      val payload = Json.parse(
        s"""
           |{
           |  "amount": 1234.34,
           |  "numberOfChildren": 3,
           |  "dateBenefitStopped": "2017-04-06"
           |}
        """.stripMargin)
      when()
        .put(s"/sandbox/$saUtr/$taxYear/childBenefit", Some(payload))
        .thenAssertThat()
        .isValidationError("/childBenefit/dateBenefitStopped", "BENEFIT_STOPPED_DATE_INVALID")
    }
  }

  "if the tax year is invalid for a sandbox request, they" should {
    "receive 400" in {
      when()
        .get(s"/sandbox/$saUtr/not-a-tax-year").withAcceptHeader()
        .thenAssertThat()
        .isBadRequest("TAX_YEAR_INVALID")
    }
  }

  "if the tax year in the path is valid for a live request, they" should {
    "receive 200" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/$taxYear").withAcceptHeader()
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsHalJson()
        .bodyHasLink("self", s"/self-assessment/$saUtr/$taxYear")
        .bodyHasLink("self-employments", s"""/self-assessment/$saUtr/$taxYear/self-employments""")
    }
  }

  "if the tax year is invalid for a live request, they" should {
    "receive 400" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .get(s"/$saUtr/not-a-tax-year").withAcceptHeader()
        .thenAssertThat()
        .isBadRequest("TAX_YEAR_INVALID")
    }
  }
}
