package uk.gov.hmrc.selfassessmentapi.live

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.controllers.api.FeatureSwitchedAnnualSummaryTypes
import uk.gov.hmrc.support.BaseFunctionalSpec

class AnnualSummaryControllerSpec extends BaseFunctionalSpec {

  private val summaryTypes = FeatureSwitchedAnnualSummaryTypes.types

  private val invalidJson =
    """
      |{
      |"snake": 555555
      |}
    """.stripMargin

  private val emptyJson = "{}"

  "find" should {
    "return code 200 when an annual summary exists" in {
      summaryTypes.foreach { summaryType =>
        s"for ${summaryType.name}" in {
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
      }
    }

    "return code 404 when an annual summary does not exist" in {
      summaryTypes.foreach { summaryType =>
        s"for ${summaryType.name}" in {
          given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .get(s"/$saUtr/$taxYear/${summaryType.name}")
          .thenAssertThat()
          .statusIs(404)
        }
      }
    }
  }

  "createOrUpdate" should {
    "return code 201 when provided with a valid JSON for an annual summary that exists" in {
      summaryTypes.foreach { summaryType =>
        s"for ${summaryType.name}" in {
          given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .put(s"/$saUtr/$taxYear/${summaryType.name}", Some(summaryType.example))
          .thenAssertThat()
          .statusIs(201)
          .contentTypeIsHalJson()
        }
      }
    }

    "return code 400 when provided with an invalid JSON for an annual summary that exists" in {
      summaryTypes.foreach { summaryType =>
        s"for ${summaryType.name}" in {
          given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .put(s"/$saUtr/$taxYear/${summaryType.name}", Some(Json.parse(invalidJson)))
          .thenAssertThat()
          .statusIs(400)
        }
      }
    }

    "return code 404 when provided any JSON for an annual summary that does not exist" in {
      val invalidSummaries = Seq("hello", "world", "cow", "says", "moo")

      invalidSummaries.foreach { summaryType =>
        s"for $summaryType" in {
          given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .put(s"/$saUtr/$taxYear/$summaryType", Some(Json.parse(invalidJson)))
          .thenAssertThat()
          .statusIs(404)
        }
      }
    }

    "overwrite an existing annual summary" in {
      summaryTypes.foreach { summaryType =>
        s"for ${summaryType.name}" in {
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
    }
  }
}
