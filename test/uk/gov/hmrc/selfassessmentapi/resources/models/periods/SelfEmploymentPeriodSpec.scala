/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.selfassessmentapi.resources.models.periods

import org.joda.time.LocalDate
import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.resources.{ErrorCode, JsonSpec}
import uk.gov.hmrc.selfassessmentapi.resources.models.{BalancingChargeType, ExpenseType, IncomeType}

class SelfEmploymentPeriodSpec extends JsonSpec {
  "SelfEmploymentPeriod" should {
    "round trip" in {
      val period = SelfEmploymentPeriod(LocalDate.now, LocalDate.now.plusDays(1), Map.empty, Map.empty, Map.empty, Some(200))
      roundTripJson(period)
    }

    "return a INVALID_PERIOD error when using a period with a 'from' date that becomes before the 'to' date" in {
      val period = SelfEmploymentPeriod(LocalDate.now, LocalDate.now.minusDays(1), Map.empty, Map.empty, Map.empty, Some(200))
      assertValidationErrorWithCode(period,
        "", ErrorCode.INVALID_PERIOD)
    }

    "return a INVALID_MONETARY_AMOUNT error when income contains a negative value" in {
      val period = SelfEmploymentPeriod(LocalDate.now.minusDays(1), LocalDate.now, Map(IncomeType.Turnover -> Income(-5000)), Map.empty, Map.empty, Some(200))

      assertValidationErrorWithCode(period,
        "/incomes/Turnover/amount", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "return a INVALID_MONETARY_AMOUNT error when income amount contains more than 2 decimal places" in {
      val period = SelfEmploymentPeriod(LocalDate.now.minusDays(1), LocalDate.now, Map(IncomeType.Turnover -> Income(10.123)), Map.empty, Map.empty, Some(200))

      assertValidationErrorWithCode(period,
        "/incomes/Turnover/amount", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "return a INVALID_MONETARY_AMOUNT error when expense contains a negative value" in {
      val period = SelfEmploymentPeriod(
        LocalDate.now.minusDays(1), LocalDate.now, Map.empty, Map(ExpenseType.CoGBought -> Expense(-500, None), ExpenseType.BadDebt -> Expense(200, Some(100))), Map.empty, Some(200))

      assertValidationErrorWithCode(period,
        "/expenses/CoGBought/amount", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "return a INVALID_MONETARY_AMOUNT error when expense contains more than 2 decimal places" in {
      val period = SelfEmploymentPeriod(
        LocalDate.now.minusDays(1), LocalDate.now, Map.empty, Map(ExpenseType.CoGBought -> Expense(500.123, None), ExpenseType.BadDebt -> Expense(200, None)), Map.empty, Some(200))

      assertValidationErrorWithCode(period,
        "/expenses/CoGBought/amount", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "return a INVALID_DISALLOWABLE_AMOUNT error when expense disallowableAmount > amount" in {
      val period = SelfEmploymentPeriod(
        LocalDate.now.minusDays(1), LocalDate.now, Map.empty, Map(ExpenseType.CoGBought -> Expense(500, Some(600)), ExpenseType.BadDebt -> Expense(200, Some(100))), Map.empty, Some(200))

      assertValidationErrorWithCode(period,
        "/expenses/CoGBought", ErrorCode.INVALID_DISALLOWABLE_AMOUNT)
    }

    "return a DEPRECIATION_DISALLOWABLE_AMOUNT error when expense 'amount' and 'disallowableAmount' fields are not equal for depreciations" in {
      val period = SelfEmploymentPeriod(
        LocalDate.now.minusDays(1), LocalDate.now, Map.empty, Map(ExpenseType.Depreciation -> Expense(200, Some(100)), ExpenseType.BadDebt -> Expense(200, Some(100))), Map.empty, Some(200))

      assertValidationErrorWithCode(period,
        "/expenses", ErrorCode.DEPRECIATION_DISALLOWABLE_AMOUNT)
    }

    "return a INVALID_MONETARY_AMOUNT error when balancingCharge amount is negative" in {
      val period = SelfEmploymentPeriod(
        LocalDate.now.minusDays(1), LocalDate.now, Map.empty, Map.empty, Map(BalancingChargeType.BPRA -> BalancingCharge(-100)), Some(200))

      assertValidationErrorWithCode(period,
        "/balancingCharges/BPRA/amount", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "return a INVALID_MONETARY_AMOUNT error when balancingCharge amount contains more than 2 decimal places" in {
      val period = SelfEmploymentPeriod(
        LocalDate.now.minusDays(1), LocalDate.now, Map.empty, Map.empty, Map(BalancingChargeType.BPRA -> BalancingCharge(100.123)), Some(200))

      assertValidationErrorWithCode(period,
        "/balancingCharges/BPRA/amount", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "return a INVALID_MONETARY_AMOUNT error when goodsAndServicesOwnUse is negative" in {
      val period = SelfEmploymentPeriod(
        LocalDate.now.minusDays(1), LocalDate.now, Map.empty, Map.empty, Map.empty, Some(-200))

      assertValidationErrorWithCode(period,
        "/goodsAndServicesOwnUse", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "return a INVALID_MONETARY_AMOUNT error when goodsAndServicesOwnUse contains more than two decimal places" in {
      val period = SelfEmploymentPeriod(
        LocalDate.now.minusDays(1), LocalDate.now, Map.empty, Map.empty, Map.empty, Some(200.123))

      assertValidationErrorWithCode(period,
        "/goodsAndServicesOwnUse", ErrorCode.INVALID_MONETARY_AMOUNT)
    }

    "return an error when provided with an empty json body" in {
      assertValidationErrorsWithMessage[SelfEmploymentPeriod](Json.parse("{}"),
        Map("/from" -> "error.path.missing",
            "/to" -> "error.path.missing"))
    }
  }
}
