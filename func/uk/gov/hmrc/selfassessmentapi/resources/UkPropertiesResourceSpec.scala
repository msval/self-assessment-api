package uk.gov.hmrc.selfassessmentapi.resources

import org.joda.time.LocalDate
import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.controllers.api.ukproperty.{ExpenseType, IncomeType}
import uk.gov.hmrc.selfassessmentapi.resources.models.periods.{Expense, Income, PropertiesPeriod}
import uk.gov.hmrc.support.BaseFunctionalSpec

class UkPropertiesResourceSpec extends BaseFunctionalSpec {
  "createPeriod" should {
    "return code 201 containing a location header when creating a uk property period" in {
      val incomes = Map(IncomeType.RentIncome -> Income(50.55), IncomeType.PremiumsOfLeaseGrant -> Income(20.22), IncomeType.ReversePremiums -> Income(100.25))
      val expenses = Map(ExpenseType.PremisesRunningCosts -> Expense(50.55, Some(10)), ExpenseType.Other -> Expense(100.22, Some(10)))
      val period = Json.toJson(PropertiesPeriod(LocalDate.now, LocalDate.now.plusDays(1), incomes, expenses, Some(100.25), Some(20.00)))

      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .post(period).to(s"/ni/$nino/properties/uk/periods")
        .thenAssertThat()
        .statusIs(201)
        .responseContainsHeader("Location", s"/ni/$nino/properties/uk/periods/\\w+".r)
    }
  }
}
