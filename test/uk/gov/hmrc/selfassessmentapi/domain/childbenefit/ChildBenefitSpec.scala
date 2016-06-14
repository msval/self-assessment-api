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

package uk.gov.hmrc.selfassessmentapi.domain.childbenefit

import play.api.libs.json.Json
import uk.gov.hmrc.selfassessmentapi.domain.ErrorCode._
import uk.gov.hmrc.selfassessmentapi.domain.JsonSpec
import uk.gov.hmrc.selfassessmentapi.domain.employment.ExpenseType._

class ChildBenefitSpec extends JsonSpec {

  "format" should {
    "round trip ChildBenefit json" in {
        roundTripJson(ChildBenefit(amount = BigDecimal(1000), numberOfChildren = 3))
    }
  }

  "validate" should {

    "reject amounts with more than 2 decimal values" in {
      Seq(BigDecimal(1000.123), BigDecimal(1000.12456), BigDecimal(1000.123454), BigDecimal(1000.123456789)).foreach { testAmount =>
        val expense = ChildBenefit(amount = testAmount, numberOfChildren = 3)
        assertValidationError[ChildBenefit](
          expense,
          Map("/amount" -> INVALID_MONETARY_AMOUNT), "Expected invalid amount in child benefit")
      }
    }

    "reject negative amount" in {
      val expense = ChildBenefit(amount = BigDecimal(-1000.13), numberOfChildren = 3)
      assertValidationError[ChildBenefit](
        expense,
        Map("/amount" -> INVALID_MONETARY_AMOUNT), "Expected negative amount in child benefit")
    }

    "reject zero and negative number of children" in {
      Seq(0, -2).foreach{
        testNumber =>
          val expense = ChildBenefit(amount = BigDecimal(1000), numberOfChildren = testNumber)
          assertValidationError[ChildBenefit](
            expense,
            Map("/numberOfChildren" -> VALUE_BELOW_MINIMUM), "Expected zero or negative number of children")
      }
    }
  }
}