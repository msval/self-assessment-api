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

import org.joda.time.LocalDate
import play.api.data.validation.ValidationError
import play.api.libs.json._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.controllers._
import uk.gov.hmrc.selfassessmentapi.controllers.api.{ErrorCode, JsonMarshaller, TaxYear}
import uk.gov.hmrc.selfassessmentapi.controllers.api.blindperson.BlindPerson
import uk.gov.hmrc.selfassessmentapi.controllers.api.charitablegiving.CharitableGiving
import uk.gov.hmrc.selfassessmentapi.controllers.api.childbenefit.ChildBenefit
import uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution.PensionContribution
import uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan.StudentLoan
import uk.gov.hmrc.selfassessmentapi.controllers.api.taxrefundedorsetoff.TaxRefundedOrSetOff
import uk.gov.hmrc.selfassessmentapi.repositories.{AnnualSummaryRepositoryWrapper, SelfAssessmentRepository}

import scala.concurrent.Future

case object ChildBenefitService extends AnnualSummaryService[ChildBenefit](ChildBenefit) {
  override val repository = AnnualSummaryRepositoryWrapper(SelfAssessmentRepository().ChildBenefitsRepository)

  // Overridden to perform additional verification of the dateBenefitsStopped field.
  override def createOrUpdate(utr: SaUtr, taxYear: TaxYear, value: JsValue): Either[ErrorResult, Future[Boolean]] = {
    val benefitResult = validateSync[ChildBenefit, ChildBenefit](value)(identity)

    benefitResult match {
      case Left(err: ErrorResult) => Left(err)
      case Right(benefit: ChildBenefit) =>
        validateDateStopped(benefit, taxYear, value).map(Left(_))
          .getOrElse(Right(repository.createOrUpdate(utr, taxYear, benefit)))
    }
  }

  private def validateDateStopped(childBenefit: ChildBenefit, taxYear: TaxYear, jsValue: JsValue): Option[ErrorResult] = {

    val yearFromBody = childBenefit.dateBenefitStopped
    val startYear = Integer.valueOf(taxYear.taxYear.split("-")(0))
    val startOfTaxYear = new LocalDate(startYear, 4, 6)
    val endOfTaxYear = new LocalDate(startYear + 1, 4, 5)
    val errorPath = JsPath \ "childBenefit" \ "dateBenefitStopped"

    val errors = yearFromBody.flatMap { date =>
      (date.isBefore(startOfTaxYear), date.isAfter(endOfTaxYear)) match {
        case (true, _) => Some(InvalidPart(ErrorCode.BENEFIT_STOPPED_DATE_INVALID, s"The dateBenefitStopped must be after the start of the tax year: $taxYear", errorPath.toString()))
        case (_, true) => Some(InvalidPart(ErrorCode.BENEFIT_STOPPED_DATE_INVALID, s"The dateBenefitStopped must be before the end of the tax year: $taxYear", errorPath.toString()))
        case _ => None
      }
    }

    errors.map(err => ValidationErrorResult(Seq((errorPath, Seq(ValidationError(err.message, err.code))))))
  }
}

case object TaxRefundedOrSetOffService extends AnnualSummaryService[TaxRefundedOrSetOff](TaxRefundedOrSetOff) {
  override val repository = AnnualSummaryRepositoryWrapper(SelfAssessmentRepository().TaxRefundedOrSetOffRepository)
}

case object StudentLoanService extends AnnualSummaryService[StudentLoan](StudentLoan) {
  override val repository = AnnualSummaryRepositoryWrapper(SelfAssessmentRepository().StudentLoanRepository)
}

case object BlindPersonService extends AnnualSummaryService[BlindPerson](BlindPerson) {
  override val repository = AnnualSummaryRepositoryWrapper(SelfAssessmentRepository().BlindPersonRepository)
}

case object CharitableGivingService extends AnnualSummaryService[CharitableGiving](CharitableGiving) {
  override val repository = AnnualSummaryRepositoryWrapper(SelfAssessmentRepository().CharitableGivingRepository)
}

case object PensionContributionService extends AnnualSummaryService[PensionContribution](PensionContribution) {
  override val repository = AnnualSummaryRepositoryWrapper(SelfAssessmentRepository().PensionContributionRepository)
}

// Returns failures for all operations.
case object NotFoundAnnualSummaryService extends AnnualSummaryService[Nothing](NothingMarshaller) {
  override lazy val repository = ???

  override def find(utr: SaUtr, taxYear: TaxYear) = Future.successful(None)

  override def createOrUpdate(utr: SaUtr, taxYear: TaxYear, value: JsValue) = Right(Future.successful(false))
}

private object NothingMarshaller extends JsonMarshaller[Nothing] {
  override val writes: Writes[Nothing] = new Writes[Nothing] {
    override def writes(o: Nothing) = ???
  }
  override val reads: Reads[Nothing] = new Reads[Nothing] {
    override def reads(json: JsValue) = ???
  }
  override def example(id: Option[String]): Nothing = ???
}