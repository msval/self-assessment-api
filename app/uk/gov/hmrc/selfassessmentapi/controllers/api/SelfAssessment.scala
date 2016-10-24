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

package uk.gov.hmrc.selfassessmentapi.controllers.api

import uk.gov.hmrc.selfassessmentapi.controllers.api.blindperson.BlindPerson
import uk.gov.hmrc.selfassessmentapi.controllers.api.charitablegiving.CharitableGiving
import uk.gov.hmrc.selfassessmentapi.controllers.api.childbenefit.ChildBenefit
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.PropertyLocationType
import uk.gov.hmrc.selfassessmentapi.controllers.api.furnishedholidaylettings.PropertyLocationType.PropertyLocationType
import uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution.PensionContribution
import uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan.StudentLoan
import uk.gov.hmrc.selfassessmentapi.controllers.api.taxrefundedorsetoff.TaxRefundedOrSetOff
import uk.gov.hmrc.selfassessmentapi.repositories.domain._

case class SelfAssessment(employments: Seq[Employment] = Seq(),
                          selfEmployments: Seq[SelfEmployment] = Seq(),
                          benefits: Seq[Benefits] = Seq(),
                          dividends: Seq[Dividend] = Seq(),
                          ukProperties: Seq[UKProperties] = Seq(),
                          pensionContribution: Option[PensionContribution] = None,
                          charitableGiving: Option[CharitableGiving] = None,
                          blindPerson: Option[BlindPerson] = None,
                          studentLoan: Option[StudentLoan] = None,
                          taxRefundedOrSetOff: Option[TaxRefundedOrSetOff] = None,
                          childBenefit: Option[ChildBenefit] = None,
                          furnishedHolidayLettings: Seq[FurnishedHolidayLettings] = Seq(),
                          banks: Seq[Bank] = Seq()) {
  private def furnishedHolidayLettingsFor(propertyLocationType: PropertyLocationType) = furnishedHolidayLettings.filter(_.propertyLocation == propertyLocationType)
  def eeaFurnishedHolidayLettings = furnishedHolidayLettingsFor(PropertyLocationType.EEA)
  def ukFurnishedHolidayLettings = furnishedHolidayLettingsFor(PropertyLocationType.UK)

}
