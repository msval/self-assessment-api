package uk.gov.hmrc.selfassessmentapi.live

import play.api.libs.json.Json
import play.api.test.FakeApplication
import uk.gov.hmrc.selfassessmentapi.controllers.api.AnnualSummaryType
import uk.gov.hmrc.selfassessmentapi.controllers.api.blindperson.BlindPersons
import uk.gov.hmrc.selfassessmentapi.controllers.api.charitablegiving.CharitableGivings
import uk.gov.hmrc.selfassessmentapi.controllers.api.childbenefit.ChildBenefits
import uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution.PensionContributions
import uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan.StudentLoans
import uk.gov.hmrc.selfassessmentapi.controllers.api.taxrefundedorsetoff.TaxRefundedOrSetOffs
import uk.gov.hmrc.support.BaseFunctionalSpec

class AnnualSummaryControllerSpec extends BaseFunctionalSpec {

  override lazy val app = FakeApplication(additionalConfiguration = Map(
    "Test.feature-switch.childBenefit.enabled" -> true,
    "Test.feature-switch.pensionContribution.enabled" -> true,
    "Test.feature-switch.taxRefundedOrSetOff.enabled" -> true,
    "Test.feature-switch.charitableGiving.enabled" -> true,
    "Test.feature-switch.studentLoan.enabled" -> true,
    "Test.feature-switch.blindPerson.enabled" -> true))

  private val summaryTypes = AnnualSummaryType.types
  val invalidSummaryTypes = Seq("hello", "world", "cow", "says", "moo")

  private val invalidJson =
    """
      |{
      |"snake": 555555
      |}
    """.stripMargin

  private val emptyJson = "{}"

  "find" should {
    summaryTypes.foreach { summaryType =>
      s"return code 200 when an annual summary exists for ${summaryType.name}" in {
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .put(s"/$saUtr/$taxYear/${summaryType.name}", Some(summaryType.example))
          .thenAssertThat()
          .statusIs(201)
          .contentTypeIsHalJson()
          .when()
          .get(s"/$saUtr/$taxYear/${summaryType.name}")
          .thenAssertThat()
          .statusIs(200)
          .contentTypeIsHalJson()
      }

      s"return code 404 when an annual summary does not exist for ${summaryType.name}" in {
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .get(s"/$saUtr/$taxYear/${summaryType.name}")
          .thenAssertThat()
          .statusIs(404)
      }
    }
  }

  "createOrUpdate" should {
    summaryTypes.foreach { summaryType =>
      s"return code 201 when provided with a valid JSON for an annual summary that exists for ${summaryType.name}" in {
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .put(s"/$saUtr/$taxYear/${summaryType.name}", Some(summaryType.example))
          .thenAssertThat()
          .statusIs(201)
          .contentTypeIsHalJson()
      }

      s"return code 400 when provided with an invalid JSON for an annual summary that exists for ${summaryType.name}" in {
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .put(s"/$saUtr/$taxYear/${summaryType.name}", Some(Json.parse(invalidJson)))
          .thenAssertThat()
          .statusIs(400)
      }

      s"overwrite an existing annual summary for ${summaryType.name}" in {
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .put(s"/$saUtr/$taxYear/${summaryType.name}", Some(summaryType.example))
          .thenAssertThat()
          .statusIs(201)
          .when()
          .put(s"/$saUtr/$taxYear/${summaryType.name}", Some(Json.parse(emptyJson)))
          .thenAssertThat()
          .statusIs(201)
          .contentTypeIsHalJson()
          .bodyIsLike("{}")
      }
    }


    invalidSummaryTypes foreach { summaryType =>
      s"return code 404 when provided any JSON for an annual summary that does not exist for $summaryType" in {
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .put(s"/$saUtr/$taxYear/$summaryType", Some(Json.parse(invalidJson)))
          .thenAssertThat()
          .statusIs(404)
      }
    }

    "be able to store and retrieve multiple annual summaries using the same UTR and tax year" in {
      given()
        .userIsAuthorisedForTheResource(saUtr)
        .when()
        .put(s"/$saUtr/$taxYear/charitableGiving", Some(CharitableGivings.example))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(s"/$saUtr/$taxYear/studentLoan", Some(StudentLoans.example))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(s"/$saUtr/$taxYear/blindPerson", Some(BlindPersons.example))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(s"/$saUtr/$taxYear/pensionContribution", Some(PensionContributions.example))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(s"/$saUtr/$taxYear/childBenefit", Some(ChildBenefits.example))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(s"/$saUtr/$taxYear/taxRefundedOrSetOff", Some(TaxRefundedOrSetOffs.example))
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get(s"/$saUtr/$taxYear/charitableGiving")
        .thenAssertThat()
        .statusIs(200)
        .when()
        .get(s"/$saUtr/$taxYear/studentLoan")
        .thenAssertThat()
        .statusIs(200)
        .when()
        .get(s"/$saUtr/$taxYear/pensionContribution")
        .thenAssertThat()
        .statusIs(200)
        .when()
        .get(s"/$saUtr/$taxYear/blindPerson")
        .thenAssertThat()
        .statusIs(200)
        .when()
        .get(s"/$saUtr/$taxYear/taxRefundedOrSetOff")
        .thenAssertThat()
        .statusIs(200)
        .when()
        .get(s"/$saUtr/$taxYear/childBenefit")
        .thenAssertThat()
        .statusIs(200)
    }
  }
}
