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

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.config.FeatureSwitch
import uk.gov.hmrc.selfassessmentapi.controllers.api.{AnnualSummaryType, SourceTypes}
import SourceTypes._
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.selfassessmentapi.controllers.api.blindperson.BlindPersons
import uk.gov.hmrc.selfassessmentapi.controllers.api.charitablegiving.CharitableGivings
import uk.gov.hmrc.selfassessmentapi.controllers.api.childbenefit.ChildBenefits
import uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution.PensionContributions
import uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan.StudentLoans
import uk.gov.hmrc.selfassessmentapi.controllers.api.taxrefundedorsetoff.TaxRefundedOrSetOffs
import uk.gov.hmrc.selfassessmentapi.repositories.SelfAssessmentMongoRepository
import uk.gov.hmrc.selfassessmentapi.repositories.domain.LiabilityResult
import uk.gov.hmrc.selfassessmentapi.repositories.live._

import scala.concurrent.Future

class LiabilityServiceSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private val saUtr = generateSaUtr()
  private val liabilityRepo = mock[LiabilityMongoRepository]
  private val employmentRepo = mock[EmploymentMongoRepository]
  private val selfEmploymentRepo = mock[SelfEmploymentMongoRepository]
  private val benefitRepo = mock[BenefitsMongoRepository]
  private val ukPropertyRepo = mock[UKPropertiesMongoRepository]
  private val furnishedHolidayLettingsRepo = mock[FurnishedHolidayLettingsMongoRepository]
  private val dividendsRepo = mock[DividendMongoRepository]
  private val banksRepo = mock[BanksMongoRepository]
  private val saRepo = mock[SelfAssessmentMongoRepository]
  private val featureSwitch = mock[FeatureSwitch]

  private val service = new LiabilityService(employmentRepo,
                                             selfEmploymentRepo,
                                             benefitRepo,
                                             furnishedHolidayLettingsRepo,
                                             liabilityRepo,
                                             ukPropertyRepo,
                                             banksRepo,
                                             dividendsRepo,
                                             saRepo,
                                             featureSwitch)

  override def beforeEach(): Unit = {
    reset(employmentRepo,
      selfEmploymentRepo,
      benefitRepo,
      furnishedHolidayLettingsRepo,
      liabilityRepo,
      ukPropertyRepo,
      banksRepo,
      dividendsRepo,
      saRepo)

    // Stub save and calculate methods to return the same item they are given.
    when(liabilityRepo.save(any[LiabilityResult])).thenAnswer(new Answer[Future[LiabilityResult]] {
      override def answer(invocation: InvocationOnMock): Future[LiabilityResult] = {
        val arg = invocation.getArguments.head.asInstanceOf[LiabilityResult]
        Future.successful(arg)
      }
    })
  }

  "calculate" should {
    "not get employment sources from repository when Employment source is switched on" in {
      when(featureSwitch.isEnabled(Employments)).thenReturn(true)
      when(employmentRepo.findAll(saUtr, taxYear)).thenReturn(Seq())

      await(service.calculate(saUtr, taxYear))

      verify(employmentRepo).findAll(saUtr, taxYear)
    }

    "not get employment sources from repository when Employment source is switched off" in {
      when(featureSwitch.isEnabled(Employments)).thenReturn(false)

      await(service.calculate(saUtr, taxYear))

      verifyNoMoreInteractions(employmentRepo)
    }

    "get self employment sources from repository when Self Employment source is switched on" in {
      when(featureSwitch.isEnabled(SelfEmployments)).thenReturn(true)
      when(selfEmploymentRepo.findAll(saUtr, taxYear)).thenReturn(Seq())

      await(service.calculate(saUtr, taxYear))

      verify(selfEmploymentRepo).findAll(saUtr, taxYear)
    }

    "not get self employment sources from repository when Self Employment source is switched off" in {
      when(featureSwitch.isEnabled(SelfEmployments)).thenReturn(false)

      await(service.calculate(saUtr, taxYear))

      verifyNoMoreInteractions(selfEmploymentRepo)
    }

    "get benefits sources from repository when benefits source is switched on" in {
      when(featureSwitch.isEnabled(Benefits)).thenReturn(true)
      when(benefitRepo.findAll(saUtr, taxYear)).thenReturn(Seq())

      await(service.calculate(saUtr, taxYear))

      verify(benefitRepo).findAll(saUtr, taxYear)
    }

    "not get benefit source from repository when benefit source is switched off" in {
      when(featureSwitch.isEnabled(Benefits)).thenReturn(false)

      await(service.calculate(saUtr, taxYear))

      verifyNoMoreInteractions(benefitRepo)
    }

    "get UK property sources from repository when the UK property source is switched on" in {
      when(featureSwitch.isEnabled(UKProperties)).thenReturn(true)
      when(ukPropertyRepo.findAll(saUtr, taxYear)).thenReturn(Seq())

      await(service.calculate(saUtr, taxYear))

      verify(ukPropertyRepo).findAll(saUtr, taxYear)
    }

    "not get UK property sources from repository when the UK property source is switched off" in {
      when(featureSwitch.isEnabled(UKProperties)).thenReturn(false)

      await(service.calculate(saUtr, taxYear))

      verifyNoMoreInteractions(ukPropertyRepo)
    }

    "get savings from repository when the Savings source is switched on" in {
      when(featureSwitch.isEnabled(Banks)).thenReturn(true)
      when(banksRepo.findAll(saUtr, taxYear)).thenReturn(Seq())

      await(service.calculate(saUtr, taxYear))

      verify(banksRepo).findAll(saUtr, taxYear)
    }

    "get savings from repository when the Savings source is switched off" in {
      when(featureSwitch.isEnabled(Banks)).thenReturn(false)

      await(service.calculate(saUtr, taxYear))

      verifyNoMoreInteractions(banksRepo)
    }

    "get pension contributions from repository when the PensionContributions annual summary is switched on" in {
      when(featureSwitch.isEnabled(PensionContributions)).thenReturn(true)
      when(saRepo.PensionContributionRepository).thenCallRealMethod()
      when(saRepo.findBy(saUtr, taxYear)).thenReturn(None)

      await(service.calculate(saUtr, taxYear))

      verify(saRepo).findBy(saUtr, taxYear)
    }

    "get pension contributions from repository when the PensionContributions annual summary is switched off" in {
      when(featureSwitch.isEnabled(PensionContributions)).thenReturn(false)

      await(service.calculate(saUtr, taxYear))

      verifyNoMoreInteractions(saRepo)
    }

    "get charitable givings from repository when the CharitableGivings annual summary is switched on" in {
      when(featureSwitch.isEnabled(CharitableGivings)).thenReturn(true)
      when(saRepo.CharitableGivingRepository).thenCallRealMethod()
      when(saRepo.findBy(saUtr, taxYear)).thenReturn(None)

      await(service.calculate(saUtr, taxYear))

      verify(saRepo).findBy(saUtr, taxYear)
    }

    "get charitable givings from repository when the CharitableGivings annual summary is switched off" in {
      when(featureSwitch.isEnabled(CharitableGivings)).thenReturn(false)

      await(service.calculate(saUtr, taxYear))

      verifyNoMoreInteractions(saRepo)
    }

    "get blind persons from repository when the BlindPersons annual summary is switched on" in {
      when(featureSwitch.isEnabled(BlindPersons)).thenReturn(true)
      when(saRepo.BlindPersonRepository).thenCallRealMethod()
      when(saRepo.findBy(saUtr, taxYear)).thenReturn(None)

      await(service.calculate(saUtr, taxYear))

      verify(saRepo).findBy(saUtr, taxYear)
    }

    "get blind persons from repository when the BlindPersons annual summary is switched off" in {
      when(featureSwitch.isEnabled(BlindPersons)).thenReturn(false)

      await(service.calculate(saUtr, taxYear))

      verifyNoMoreInteractions(saRepo)
    }

    "get student loans from repository when the StudentLoans annual summary is switched on" in {
      when(featureSwitch.isEnabled(StudentLoans)).thenReturn(true)
      when(saRepo.StudentLoanRepository).thenCallRealMethod()
      when(saRepo.findBy(saUtr, taxYear)).thenReturn(None)

      await(service.calculate(saUtr, taxYear))

      verify(saRepo).findBy(saUtr, taxYear)
    }

    "get student loans from repository when the StudentLoans annual summary is switched off" in {
      when(featureSwitch.isEnabled(StudentLoans)).thenReturn(false)

      await(service.calculate(saUtr, taxYear))

      verifyNoMoreInteractions(saRepo)
    }

    "get tax refunded or set off from repository when the TaxRefundedOrSetOffs annual summary is switched on" in {
      when(featureSwitch.isEnabled(TaxRefundedOrSetOffs)).thenReturn(true)
      when(saRepo.TaxRefundedOrSetOffRepository).thenCallRealMethod()
      when(saRepo.findBy(saUtr, taxYear)).thenReturn(None)

      await(service.calculate(saUtr, taxYear))

      verify(saRepo).findBy(saUtr, taxYear)
    }

    "get tax refunded or set off from repository when the s annual summary is switched off" in {
      when(featureSwitch.isEnabled(TaxRefundedOrSetOffs)).thenReturn(false)

      await(service.calculate(saUtr, taxYear))

      verifyNoMoreInteractions(saRepo)
    }

    "get child benefits from repository when the ChildBenefits annual summary is switched on" in {
      when(featureSwitch.isEnabled(ChildBenefits)).thenReturn(true)
      when(saRepo.ChildBenefitsRepository).thenCallRealMethod()
      when(saRepo.findBy(saUtr, taxYear)).thenReturn(None)

      await(service.calculate(saUtr, taxYear))

      verify(saRepo).findBy(saUtr, taxYear)
    }

    "get child benefits from repository when the ChildBenefits annual summary is switched off" in {
      when(featureSwitch.isEnabled(ChildBenefits)).thenReturn(false)

      await(service.calculate(saUtr, taxYear))

      verifyNoMoreInteractions(saRepo)
    }
  }
}
