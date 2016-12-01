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

package uk.gov.hmrc.selfassessmentapi.resources.models

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.resources.{CapAt, Sum}

case class SelfEmploymentAllowances(annualInvestmentAllowance: Option[BigDecimal] = None,
                                    capitalAllowanceMainPool: Option[BigDecimal] = None,
                                    capitalAllowanceSpecialRatePool: Option[BigDecimal] = None,
                                    businessPremisesRenovationAllowance: Option[BigDecimal] = None,
                                    enhancedCapitalAllowance: Option[BigDecimal] = None,
                                    allowancesOnSales: Option[BigDecimal] = None) {

  private val maxAnnualInvestmentAllowance = 200000

  def total = {
    Sum(CapAt(annualInvestmentAllowance, maxAnnualInvestmentAllowance), capitalAllowanceMainPool, capitalAllowanceSpecialRatePool,
      businessPremisesRenovationAllowance, enhancedCapitalAllowance, allowancesOnSales)
  }
}

object SelfEmploymentAllowances {

  lazy val example = SelfEmploymentAllowances(
    annualInvestmentAllowance = Some(BigDecimal(1000.00)),
    capitalAllowanceMainPool = Some(BigDecimal(150.00)),
    capitalAllowanceSpecialRatePool = Some(BigDecimal(5000.50)),
    businessPremisesRenovationAllowance = Some(BigDecimal(600.00)),
    enhancedCapitalAllowance = Some(BigDecimal(50.00)),
    allowancesOnSales = Some(BigDecimal(3399.99)))

  implicit val writes = Json.writes[SelfEmploymentAllowances]

  implicit val reads: Reads[SelfEmploymentAllowances] = (
      (__ \ "annualInvestmentAllowance").readNullable[BigDecimal](positiveAmountValidator) and
      (__ \ "capitalAllowanceMainPool").readNullable[BigDecimal](positiveAmountValidator) and
      (__ \ "capitalAllowanceSpecialRatePool").readNullable[BigDecimal](positiveAmountValidator) and
      (__ \ "businessPremisesRenovationAllowance").readNullable[BigDecimal](positiveAmountValidator) and
      (__ \ "enhancedCapitalAllowance").readNullable[BigDecimal](positiveAmountValidator) and
      (__ \ "allowancesOnSales").readNullable[BigDecimal](positiveAmountValidator)
    ) (SelfEmploymentAllowances.apply _)
}
