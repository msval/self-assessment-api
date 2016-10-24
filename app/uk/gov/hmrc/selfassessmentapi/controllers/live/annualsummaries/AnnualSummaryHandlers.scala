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

package uk.gov.hmrc.selfassessmentapi.controllers.live.annualsummaries

import uk.gov.hmrc.selfassessmentapi.controllers.AnnualSummaryService
import uk.gov.hmrc.selfassessmentapi.controllers.api.blindperson.BlindPerson
import uk.gov.hmrc.selfassessmentapi.controllers.api.charitablegiving.CharitableGiving
import uk.gov.hmrc.selfassessmentapi.controllers.api.childbenefit.ChildBenefit
import uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution.PensionContribution
import uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan.StudentLoan
import uk.gov.hmrc.selfassessmentapi.controllers.api.taxrefundedorsetoff.TaxRefundedOrSetOff
import uk.gov.hmrc.selfassessmentapi.repositories.{AnnualSummaryRepositoryWrapper, SelfAssessmentRepository}

case object ChildBenefitService$ extends AnnualSummaryService[ChildBenefit](ChildBenefit) {
  override val repository = AnnualSummaryRepositoryWrapper(SelfAssessmentRepository().ChildBenefitsRepository)
}

case object TaxRefundedOrSetOffService$ extends AnnualSummaryService[TaxRefundedOrSetOff](TaxRefundedOrSetOff) {
  override val repository = AnnualSummaryRepositoryWrapper(SelfAssessmentRepository().TaxRefundedOrSetOffRepository)
}

case object StudentLoanService$ extends AnnualSummaryService[StudentLoan](StudentLoan) {
  override val repository = AnnualSummaryRepositoryWrapper(SelfAssessmentRepository().StudentLoanRepository)
}

case object BlindPersonService$ extends AnnualSummaryService[BlindPerson](BlindPerson) {
  override val repository = AnnualSummaryRepositoryWrapper(SelfAssessmentRepository().BlindPersonRepository)
}

case object CharitableGivingService$ extends AnnualSummaryService[CharitableGiving](CharitableGiving) {
  override val repository = AnnualSummaryRepositoryWrapper(SelfAssessmentRepository().CharitableGivingRepository)
}

case object PensionContributionService$ extends AnnualSummaryService[PensionContribution](PensionContribution) {
  override val repository = AnnualSummaryRepositoryWrapper(SelfAssessmentRepository().PensionContributionRepository)
}
