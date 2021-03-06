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

package uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment

import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.selfassessmentapi.resources.models.ErrorCode

case class SelfEmploymentAnnualSummary(allowances: Option[Allowances], adjustments: Option[Adjustments])

object SelfEmploymentAnnualSummary {
  implicit val writes: Writes[SelfEmploymentAnnualSummary] = Json.writes[SelfEmploymentAnnualSummary]

  implicit val reads: Reads[SelfEmploymentAnnualSummary] = (
    (__ \ "allowances").readNullable[Allowances] and
      (__ \ "adjustments").readNullable[Adjustments]
  )(SelfEmploymentAnnualSummary.apply _).filter(
    ValidationError(
      "Balancing charge on BPRA (Business Premises Renovation Allowance) can only be claimed when there is a value for BPRA.",
      ErrorCode.INVALID_BALANCING_CHARGE_BPRA)) { annualSummary => validateBalancingChargeBPRA(annualSummary) }

  private def validateBalancingChargeBPRA(annualSummary: SelfEmploymentAnnualSummary): Boolean = {
    annualSummary.adjustments.forall { adjustments =>
      adjustments.balancingChargeBPRA.forall{ _ =>
        annualSummary.allowances.exists(_.businessPremisesRenovationAllowance.exists(_ > 0))
      }
    }
  }
}
