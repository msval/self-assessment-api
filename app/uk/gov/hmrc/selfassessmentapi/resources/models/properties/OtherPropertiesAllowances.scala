/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.selfassessmentapi.resources.models.properties

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.resources.Sum
import uk.gov.hmrc.selfassessmentapi.resources.models._

case class OtherPropertiesAllowances(annualInvestmentAllowance: Option[Amount] = None,
                                     businessPremisesRenovationAllowance: Option[Amount] = None,
                                     otherCapitalAllowance: Option[Amount] = None,
                                     costOfReplacingDomesticItems: Option[Amount] = None,
                                     zeroEmissionsGoodsVehicleAllowance: Option[Amount] = None) {

  def total: BigDecimal =
    Sum(annualInvestmentAllowance, businessPremisesRenovationAllowance, otherCapitalAllowance, costOfReplacingDomesticItems)
}

object OtherPropertiesAllowances {
  implicit val writes: Writes[OtherPropertiesAllowances] = Json.writes[OtherPropertiesAllowances]

  implicit val reads: Reads[OtherPropertiesAllowances] = (
    (__ \ "annualInvestmentAllowance").readNullable[Amount](positiveAmountValidator) and
      (__ \ "businessPremisesRenovationAllowance").readNullable[Amount](positiveAmountValidator) and
      (__ \ "otherCapitalAllowance").readNullable[Amount](positiveAmountValidator) and
      (__ \ "costOfReplacingDomesticItems").readNullable[Amount](positiveAmountValidator) and
      (__ \ "zeroEmissionsGoodsVehicleAllowance").readNullable[Amount](positiveAmountValidator)
    ) (OtherPropertiesAllowances.apply _)
}
