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

import org.mockito.Mockito.{reset, when}
import org.mockito.Matchers.any
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.config.FeatureSwitch
import uk.gov.hmrc.selfassessmentapi.controllers.api.blindperson.BlindPersons
import uk.gov.hmrc.selfassessmentapi.controllers.api.charitablegiving.CharitableGivings
import uk.gov.hmrc.selfassessmentapi.controllers.api.childbenefit.ChildBenefits
import uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution.PensionContributions
import uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan.StudentLoans
import uk.gov.hmrc.selfassessmentapi.controllers.api.taxrefundedorsetoff.TaxRefundedOrSetOffs

class FeatureSwitchedAnnualSummaryTypesSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  val mockFeatureSwitch = mock[FeatureSwitch]

  object TestFeatureSwitchedAnnualSummaryTypes extends FeatureSwitchedAnnualSummaryTypes {
    override val featureSwitch: FeatureSwitch = mockFeatureSwitch
  }

  override def beforeEach() = {
    reset(mockFeatureSwitch)
  }

  "types" should {
    "return an empty set when all annual summaries are disabled" in {
      when(mockFeatureSwitch.isEnabled(any[AnnualSummaryType]())).thenReturn(false)
      TestFeatureSwitchedAnnualSummaryTypes.types shouldBe Set()
    }

    "return a set containing only enabled annual summaries" in {
      when(mockFeatureSwitch.isEnabled(PensionContributions)).thenReturn(false)
      when(mockFeatureSwitch.isEnabled(CharitableGivings)).thenReturn(false)
      when(mockFeatureSwitch.isEnabled(BlindPersons)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(StudentLoans)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(TaxRefundedOrSetOffs)).thenReturn(false)
      when(mockFeatureSwitch.isEnabled(ChildBenefits)).thenReturn(false)

      TestFeatureSwitchedAnnualSummaryTypes.types shouldBe Set(BlindPersons, StudentLoans)
    }

    "return a sequence containing all annual summaries when all are enabled" in {
      when(mockFeatureSwitch.isEnabled(any[AnnualSummaryType]())).thenReturn(true)
      TestFeatureSwitchedAnnualSummaryTypes.types shouldBe AnnualSummaryType.types
    }
  }

  "atLeastOnePropertyEnabled" should {
    "return true when at least one annual summary is enabled" in {
      when(mockFeatureSwitch.isEnabled(PensionContributions)).thenReturn(false)
      when(mockFeatureSwitch.isEnabled(CharitableGivings)).thenReturn(false)
      when(mockFeatureSwitch.isEnabled(BlindPersons)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(StudentLoans)).thenReturn(true)
      when(mockFeatureSwitch.isEnabled(TaxRefundedOrSetOffs)).thenReturn(false)
      when(mockFeatureSwitch.isEnabled(ChildBenefits)).thenReturn(false)

      TestFeatureSwitchedAnnualSummaryTypes.atLeastOnePropertyIsEnabled shouldBe true
    }

    "return false when no annual summaries are enabled" in {
      when(mockFeatureSwitch.isEnabled(any[AnnualSummaryType]())).thenReturn(false)

      TestFeatureSwitchedAnnualSummaryTypes.atLeastOnePropertyIsEnabled shouldBe false
    }
  }
}
