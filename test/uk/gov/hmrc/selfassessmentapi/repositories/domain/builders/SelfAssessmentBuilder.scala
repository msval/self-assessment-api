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

package uk.gov.hmrc.selfassessmentapi.repositories.domain.builders

import org.joda.time.LocalDate
import uk.gov.hmrc.selfassessmentapi.controllers.api.SelfAssessment
import uk.gov.hmrc.selfassessmentapi.controllers.api.UkCountryCodes._
import uk.gov.hmrc.selfassessmentapi.controllers.api.blindperson.BlindPerson
import uk.gov.hmrc.selfassessmentapi.controllers.api.charitablegiving.{
  CharitableGiving,
  GiftAidPayments,
  LandAndProperties,
  SharesAndSecurities
}
import uk.gov.hmrc.selfassessmentapi.controllers.api.childbenefit.ChildBenefit
import uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution.{PensionContribution, PensionSaving}
import uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan.StudentLoan
import uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan.StudentLoanPlanType.{apply => _, _}
import uk.gov.hmrc.selfassessmentapi.controllers.api.taxrefundedorsetoff.TaxRefundedOrSetOff

class SelfAssessmentBuilder {

  private var selfAssessment = SelfAssessment()

  def withSelfEmployments(selfEmployments: SelfEmploymentBuilder*) = {
    selfAssessment = selfAssessment.copy(selfEmployments = selfEmployments.map(_.create()))
    this
  }

  def withEmployments(employments: EmploymentBuilder*) = {
    selfAssessment = selfAssessment.copy(employments = employments.map(_.create()))
    this
  }

  def withFurnishedHolidayLettings(furnishedHolidayLettings: FurnishedHolidayLettingBuilder*) = {
    selfAssessment = selfAssessment.copy(furnishedHolidayLettings = furnishedHolidayLettings.map(_.create()))
    this
  }

  def withUkProperties(ukProperties: UKPropertyBuilder*) = {
    selfAssessment = selfAssessment.copy(ukProperties = ukProperties.map(_.create()))
    this
  }

  def withDividends(dividends: DividendBuilder*) = {
    selfAssessment = selfAssessment.copy(dividends = dividends.map(_.create()))
    this
  }

  def withSavings(banks: BankBuilder*) = {
    selfAssessment = selfAssessment.copy(banks = banks.map(_.create()))
    this
  }

  def withPensionContributions() = {
    selfAssessment = selfAssessment.copy(pensionContribution = Some(PensionContribution()))
    this
  }

  /*
   * Pension Contributions
   */

  def ukRegisteredPension(amount: BigDecimal) = {
    selfAssessment = selfAssessment.copy(
      pensionContribution = selfAssessment.pensionContribution.map(_.copy(ukRegisteredPension = Some(amount))))
    this
  }

  def retirementAnnuityContract(amount: BigDecimal) = {
    selfAssessment = selfAssessment.copy(
      pensionContribution = selfAssessment.pensionContribution.map(_.copy(retirementAnnuity = Some(amount))))
    this
  }

  def employerScheme(amount: BigDecimal) = {
    selfAssessment = selfAssessment.copy(
      pensionContribution = selfAssessment.pensionContribution.map(_.copy(employerScheme = Some(amount))))
    this
  }

  def overseasPension(amount: BigDecimal) = {
    selfAssessment = selfAssessment.copy(
      pensionContribution = selfAssessment.pensionContribution.map(_.copy(overseasPension = Some(amount))))
    this
  }

  def pensionSavings(excessOfAnnualAllowance: BigDecimal = 0, taxPaidByPensionScheme: BigDecimal = 0) = {
    selfAssessment = selfAssessment.copy(
      pensionContribution = selfAssessment.pensionContribution.map(
        _.copy(pensionSavings = Some(PensionSaving(excessOfAnnualAllowance = Some(excessOfAnnualAllowance),
                                                   taxPaidByPensionScheme = Some(taxPaidByPensionScheme))))))
    this
  }

  /*
   * Charitable Givings
   */

  def giftAidPayments(totalInTaxYear: BigDecimal,
                      oneOff: BigDecimal,
                      toNonUkCharities: BigDecimal,
                      carriedBackToPreviousTaxYear: BigDecimal,
                      carriedFromNextTaxYear: BigDecimal) = {
    selfAssessment = selfAssessment.copy(
      charitableGiving = selfAssessment.charitableGiving.map(
        _.copy(
          giftAidPayments = Some(
            GiftAidPayments(Some(totalInTaxYear),
                            Some(oneOff),
                            Some(toNonUkCharities),
                            Some(carriedBackToPreviousTaxYear),
                            Some(carriedFromNextTaxYear))))))
    this
  }

  def sharesSecurities(totalInTaxYear: BigDecimal, toNonUkCharities: BigDecimal) = {
    selfAssessment = selfAssessment.copy(
      charitableGiving = selfAssessment.charitableGiving.map(
        _.copy(sharesSecurities = Some(SharesAndSecurities(totalInTaxYear, Some(toNonUkCharities))))))
    this
  }

  def landAndProperties(totalInTaxYear: BigDecimal, toNonUkCharities: BigDecimal) = {
    selfAssessment = selfAssessment.copy(
      charitableGiving = selfAssessment.charitableGiving.map(
        _.copy(landProperties = Some(LandAndProperties(totalInTaxYear, Some(toNonUkCharities))))))
    this
  }

  def withCharitableGivings() = {
    selfAssessment = selfAssessment.copy(charitableGiving = Some(CharitableGiving()))
    this
  }

  def withChildBenefit(amount: BigDecimal, numberOfChildren: Int, dateBenefitStopped: LocalDate) = {
    selfAssessment =
      selfAssessment.copy(childBenefit = Some(ChildBenefit(amount, numberOfChildren, Some(dateBenefitStopped))))
    this
  }

  def withBlindPerson(country: UkCountryCode,
                      registrationAuthority: String,
                      spouseSurplusAllowance: BigDecimal,
                      wantsSpouseToUseSurplusAllowance: Boolean) = {
    selfAssessment = selfAssessment.copy(
      blindPerson = Some(
        BlindPerson(Some(country),
                    Some(registrationAuthority),
                    Some(spouseSurplusAllowance),
                    Some(wantsSpouseToUseSurplusAllowance))))
    this
  }

  def withStudentLoan(planType: StudentLoanPlanType, deductedByEmployers: BigDecimal) = {
    selfAssessment = selfAssessment.copy(studentLoan = Some(StudentLoan(planType, Some(deductedByEmployers))))
    this
  }

  def withTaxRefundedOrSetOff(amount: BigDecimal) = {
    selfAssessment = selfAssessment.copy(taxRefundedOrSetOff = Some(TaxRefundedOrSetOff(amount)))
    this
  }

  def create() = selfAssessment
}

object SelfAssessmentBuilder {
  def apply() = new SelfAssessmentBuilder()
}
