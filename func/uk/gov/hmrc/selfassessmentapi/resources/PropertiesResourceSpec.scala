package uk.gov.hmrc.selfassessmentapi.resources

import org.joda.time.LocalDate
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty.{ExpenseType, IncomeType}
import uk.gov.hmrc.selfassessmentapi.resources.models.periods.{Expense, Income, PropertiesPeriod}
import uk.gov.hmrc.support.BaseFunctionalSpec

class PropertiesResourceSpec extends BaseFunctionalSpec {

  implicit def period2Json(period: PropertiesPeriod): JsValue = Json.toJson(period)

  "createPeriod" should {
    "return code 201 containing a location header when creating a uk property period" in {
      val incomes = Map(IncomeType.RentIncome -> Income(50.55), IncomeType.PremiumsOfLeaseGrant -> Income(20.22), IncomeType.ReversePremiums -> Income(100.25))
      val expenses = Map(ExpenseType.PremisesRunningCosts -> Expense(50.55, Some(10)), ExpenseType.Other -> Expense(100.22, Some(10)))
      val period = PropertiesPeriod(LocalDate.parse("2016-04-06"), LocalDate.parse("2017-04-05"), incomes, expenses, Some(100.25), Some(20.00))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(period).to(s"/ni/$nino/uk-properties/uk/periods")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/ni/$nino/uk-properties/uk/periods/\\w+".r)
    }

    "return code 400 when provided with an invalid uk property period" in {
      val incomes = Map(IncomeType.RentIncome -> Income(-50.55), IncomeType.PremiumsOfLeaseGrant -> Income(20.22), IncomeType.ReversePremiums -> Income(100.25))
      val expenses = Map(ExpenseType.PremisesRunningCosts -> Expense(50.55, Some(10)), ExpenseType.Other -> Expense(100.22, Some(10)))
      val period =  PropertiesPeriod(LocalDate.parse("2016-04-06"), LocalDate.parse("2016-04-05"), incomes, expenses, Some(100.25), Some(20.00))

      val expectedJson =
        s"""
           |{
           |  "code": "INVALID_REQUEST",
           |  "message": "Invalid request",
           |  "errors": [
           |    {
           |      "code": "INVALID_MONETARY_AMOUNT",
           |      "path": "/incomes/RentIncome/amount",
           |      "message": "amounts should be positive numbers with up to 2 decimal places"
           |    }
           |  ]
           |}
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(period).to(s"/ni/$nino/uk-properties/uk/periods")
        .thenAssertThat()
        .statusIs(400)
        .contentTypeIsJson()
        .bodyIsLike(expectedJson)
    }
  }

  "retrieveAllPeriods" ignore {
    "return code 200 when retrieving all periods associated with a properties business" in {
      val incomes = Map(IncomeType.RentIncome -> Income(50.55), IncomeType.PremiumsOfLeaseGrant -> Income(20.22), IncomeType.ReversePremiums -> Income(100.25))
      val expenses = Map(ExpenseType.PremisesRunningCosts -> Expense(50.55, Some(10)), ExpenseType.Other -> Expense(100.22, Some(10)))
      val periodOne = PropertiesPeriod(LocalDate.parse("2016-04-06"), LocalDate.parse("2017-04-05"), incomes, expenses, Some(100.25), Some(20.00))
      val periodTwo = periodOne.copy(from = LocalDate.parse("2017-04-06"), to = LocalDate.parse("2017-04-07"))

      val expectedBody =
         """
           |[
           |  {
           |    "from": "2016-04-06",
           |    "to": "2017-04-05"
           |  },
           |  {
           |    "from": "2017-04-06",
           |    "to": "2017-04-07"
           |  }
           |]
         """.stripMargin

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(periodOne).to(s"/ni/$nino/uk-properties/uk/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .post(periodTwo).to(s"/ni/$nino/uk-properties/uk/periods")
        .thenAssertThat()
        .statusIs(201)
        .when()
        .get("/ni/$nino/uk-properties/uk/periods")
        .thenAssertThat()
        .statusIs(200)
        .contentTypeIsJson()
        .bodyIsLike(expectedBody)
        .selectFields(_ \\ "id").isLength(2).matches("\\w+".r)
    }
  }
}
