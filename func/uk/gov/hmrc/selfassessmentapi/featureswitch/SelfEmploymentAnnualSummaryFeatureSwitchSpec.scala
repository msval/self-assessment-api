package uk.gov.hmrc.selfassessmentapi.featureswitch

import org.joda.time.LocalDate
import play.api.libs.json.Json
import play.api.test.FakeApplication
import uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment.{AccountingType, SelfEmployment, AnnualSummary}
import uk.gov.hmrc.selfassessmentapi.resources.models.AccountingPeriod
import uk.gov.hmrc.support.BaseFunctionalSpec

class SelfEmploymentAnnualSummaryFeatureSwitchSpec extends BaseFunctionalSpec {

  private val conf: Map[String, _] =
    Map("Test.feature-switch.self-employments" ->
      Map("enabled" -> true, "annual" -> Map("enabled" -> false), "periods" -> Map("enabled" -> true))
    )

  override lazy val app: FakeApplication = new FakeApplication(additionalConfiguration = conf)

  "self-employments" should {
    "not be visible if feature Switched Off" in {
      val selfEmployment = Json.toJson(SelfEmployment(
        accountingPeriod = AccountingPeriod(LocalDate.now, LocalDate.now.plusDays(1)),
        accountingType = AccountingType.CASH,
        commencementDate = LocalDate.now.minusDays(1)))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(selfEmployment).to(s"/ni/$nino/self-employments")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .put(Json.toJson(AnnualSummary(None, None))).at(s"%sourceLocation%/$taxYear")
        .thenAssertThat()
        .statusIs(404)
    }
  }

}


