package uk.gov.hmrc.selfassessmentapi.live

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.selfassessmentapi.controllers.live.SourceController
import uk.gov.hmrc.selfassessmentapi.domain.SourceTypes
import uk.gov.hmrc.support.BaseFunctionalSpec

class NotImplementedSourcesSpec extends BaseFunctionalSpec {

  val sourceId = BSONObjectID.generate.stringify

  val notImplementedTypes  = Set(SourceTypes.Employments, SourceTypes.FurnishedHolidayLettings, SourceTypes.UKProperties,
    SourceTypes.UnearnedIncomes)
  
  "Create source" should {
    "return a resourceIsNotImplemented response" in {
      notImplementedTypes.foreach { source =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .post(s"/$saUtr/$taxYear/${source.name}", Some(source.example()))
          .thenAssertThat()
          .resourceIsNotImplemented()
      }
    }
  }

  "Find source by id" should {
    "return a resourceIsNotImplemented response" in {
      notImplementedTypes.foreach { source =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .get(s"/$saUtr/$taxYear/${source.name}/$sourceId")
          .thenAssertThat()
          .resourceIsNotImplemented()
      }
    }
  }

  "Find all source" should {
    "return a resourceIsNotImplemented response" in {
      notImplementedTypes.foreach { source =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .get(s"/$saUtr/$taxYear/${source.name}")
          .thenAssertThat()
          .resourceIsNotImplemented()
      }
    }
  }

  "Update source" should {
    "return a resourceIsNotImplemented response" in {
      notImplementedTypes.foreach { source =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .put(s"/$saUtr/$taxYear/${source.name}/$sourceId", Some(source.example()))
          .thenAssertThat()
          .resourceIsNotImplemented()
      }
    }
  }

  "Delete source" should {
    "return a resourceIsNotImplemented response" in {
      notImplementedTypes.foreach { source =>
        given()
          .userIsAuthorisedForTheResource(saUtr)
          .when()
          .delete(s"/$saUtr/$taxYear/${source.name}/$sourceId")
          .thenAssertThat()
          .resourceIsNotImplemented()
      }
    }
  }
}