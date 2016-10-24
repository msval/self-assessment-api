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

package uk.gov.hmrc.selfassessmentapi.services.live.calculation

import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi._
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureSwitch}
import uk.gov.hmrc.selfassessmentapi.controllers.api.SourceTypes._
import uk.gov.hmrc.selfassessmentapi.controllers.api.blindperson.BlindPersons
import uk.gov.hmrc.selfassessmentapi.controllers.api.charitablegiving.CharitableGivings
import uk.gov.hmrc.selfassessmentapi.controllers.api.childbenefit.ChildBenefits
import uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution.PensionContributions
import uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan.StudentLoans
import uk.gov.hmrc.selfassessmentapi.controllers.api.taxrefundedorsetoff.TaxRefundedOrSetOffs
import uk.gov.hmrc.selfassessmentapi.controllers.api.{ErrorCode, LiabilityId, SelfAssessment, SourceType, SourceTypes, TaxYear, _}
import uk.gov.hmrc.selfassessmentapi.controllers.live.annualsummaries._
import uk.gov.hmrc.selfassessmentapi.controllers.{api, LiabilityError => _, LiabilityErrors => _}
import uk.gov.hmrc.selfassessmentapi.repositories.{SelfAssessmentMongoRepository, SelfAssessmentRepository}
import uk.gov.hmrc.selfassessmentapi.repositories.domain.{Benefits, Employment, Liability, SelfEmployment, _}
import uk.gov.hmrc.selfassessmentapi.repositories.live._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LiabilityService(employmentRepo: EmploymentMongoRepository,
                       selfEmploymentRepo: SelfEmploymentMongoRepository,
                       benefitsRepo: BenefitsMongoRepository,
                       furnishedHolidayLettingsRepo: FurnishedHolidayLettingsMongoRepository,
                       liabilityRepo: LiabilityMongoRepository,
                       ukPropertiesRepo: UKPropertiesMongoRepository,
                       savingsRepo: BanksMongoRepository,
                       dividendsRepo: DividendMongoRepository,
                       saRepository: SelfAssessmentMongoRepository,
                       featureSwitch: FeatureSwitch) {

  def find(saUtr: SaUtr, taxYear: TaxYear): Future[Option[Either[controllers.LiabilityErrors, api.Liability]]] = {
    liabilityRepo
      .findBy(saUtr, taxYear)
      .map(_.map {
        case calculationError: LiabilityErrors =>
          Left(
            controllers.LiabilityErrors(ErrorCode.LIABILITY_CALCULATION_ERROR,
                                         "Liability calculation error",
                                         calculationError.errors.map(error =>
                                           controllers.LiabilityError(error.code, error.message))))
        case liability: Liability => Right(liability.toLiability)
      })
  }

  def calculate(saUtr: SaUtr, taxYear: TaxYear): Future[Either[LiabilityCalculationErrorId, LiabilityId]] = {
    for {
      employments <- if (isSourceEnabled(Employments)) employmentRepo.findAll(saUtr, taxYear) else Future.successful(Seq[Employment]())
      selfEmployments <- if (isSourceEnabled(SelfEmployments)) selfEmploymentRepo.findAll(saUtr, taxYear) else Future.successful(Seq[SelfEmployment]())
      benefits <- if (isSourceEnabled(SourceTypes.Benefits)) benefitsRepo.findAll(saUtr, taxYear) else Future.successful(Seq[Benefits]())
      ukProperties <- if (isSourceEnabled(SourceTypes.UKProperties)) ukPropertiesRepo.findAll(saUtr, taxYear) else Future.successful(Seq[UKProperties]())
      dividends <- if (isSourceEnabled(SourceTypes.Dividends)) dividendsRepo.findAll(saUtr, taxYear) else Future.successful(Seq[Dividend]())
      banks <- if (isSourceEnabled(SourceTypes.Banks)) savingsRepo.findAll(saUtr, taxYear) else Future.successful(Seq[Bank]())
      furnishedHolidayLettings <- if (isSourceEnabled(SourceTypes.FurnishedHolidayLettings)) furnishedHolidayLettingsRepo.findAll(saUtr, taxYear) else Future.successful(Seq[FurnishedHolidayLettings]())
      pensionContribution <- if (isAnnualSummaryEnabled(PensionContributions)) saRepository.PensionContributionRepository.find(saUtr, taxYear) else Future.successful(None)
      charitableGiving <- if (isAnnualSummaryEnabled(CharitableGivings)) saRepository.CharitableGivingRepository.find(saUtr, taxYear) else Future.successful(None)
      blindPerson <- if (isAnnualSummaryEnabled(BlindPersons)) saRepository.BlindPersonRepository.find(saUtr, taxYear) else Future.successful(None)
      studentLoan <- if (isAnnualSummaryEnabled(StudentLoans)) saRepository.StudentLoanRepository.find(saUtr, taxYear) else Future.successful(None)
      taxRefundedOrSetOff <- if (isAnnualSummaryEnabled(TaxRefundedOrSetOffs)) saRepository.TaxRefundedOrSetOffRepository.find(saUtr, taxYear) else Future.successful(None)
      childBenefit <- if (isAnnualSummaryEnabled(ChildBenefits)) saRepository.ChildBenefitsRepository.find(saUtr, taxYear) else Future.successful(None)
      liability = Liability.create(saUtr, taxYear, SelfAssessment(employments = employments, selfEmployments = selfEmployments,
        ukProperties = ukProperties, benefits = benefits, furnishedHolidayLettings = furnishedHolidayLettings,
        dividends = dividends, banks = banks, pensionContribution = pensionContribution, charitableGiving = charitableGiving,
        blindPerson = blindPerson, studentLoan = studentLoan, taxRefundedOrSetOff = taxRefundedOrSetOff, childBenefit = childBenefit))
      liability <- liabilityRepo.save(LiabilityOrError(liability))
    } yield
      liability match {
        case calculationError: LiabilityErrors => Left(calculationError.liabilityCalculationErrorId)
        case liability: Liability => Right(liability.liabilityId)
      }
  }

  private[calculation] def isSourceEnabled(sourceType: SourceType) = featureSwitch.isEnabled(sourceType)
  private[calculation] def isAnnualSummaryEnabled(annualSummaryType: AnnualSummaryType) = featureSwitch.isEnabled(annualSummaryType)
}

object LiabilityService {

  private lazy val service = new LiabilityService(EmploymentRepository(),
                                                  SelfEmploymentRepository(),
                                                  BenefitsRepository(),
                                                  FurnishedHolidayLettingsRepository(),
                                                  LiabilityRepository(),
                                                  UKPropertiesRepository(),
                                                  BanksRepository(),
                                                  DividendRepository(),
                                                  SelfAssessmentRepository(),
                                                  FeatureSwitch(AppContext.featureSwitch))

  def apply() = service
}
