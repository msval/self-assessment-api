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

package uk.gov.hmrc.selfassessmentapi.domain

import org.mockito.BDDMockito._
import org.scalatest.Matchers
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.config.FeatureSwitch
import uk.gov.hmrc.selfassessmentapi.controllers.api.FeatureSwitchedAnnualSummaryTypes
import uk.gov.hmrc.selfassessmentapi.controllers.api.blindperson.BlindPersons
import uk.gov.hmrc.selfassessmentapi.controllers.api.charitablegiving.CharitableGivings
import uk.gov.hmrc.selfassessmentapi.controllers.api.childbenefit.ChildBenefits
import uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution.PensionContributions
import uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan.StudentLoans
import uk.gov.hmrc.selfassessmentapi.controllers.api.taxrefundedorsetoff.TaxRefundedOrSetOffs

class AnnualSummaryTypesSpec extends UnitSpec with MockitoSugar with Matchers {

  val featureSwitch = mock[FeatureSwitch]

  "types" should {
    Seq(PensionContributions, CharitableGivings, BlindPersons, TaxRefundedOrSetOffs, StudentLoans, ChildBenefits).foreach {
      annualSummaryType =>
        s"return $annualSummaryType if it is switched on" in {
          given(featureSwitch.isEnabled(annualSummaryType)).willReturn(true)
          AnnualSummaryTypes(featureSwitch).types should contain only annualSummaryType
        }
        s"not return $annualSummaryType if it is switched off" in {
          given(featureSwitch.isEnabled(annualSummaryType)).willReturn(false)
          AnnualSummaryTypes(featureSwitch).types should not contain annualSummaryType
        }
    }
  }

  "forName" should {
    "return nothing when given an invalid name" in {
      AnnualSummaryTypes(featureSwitch).fromName("nonsense") shouldBe None
    }

    Seq(PensionContributions, CharitableGivings, BlindPersons, TaxRefundedOrSetOffs, StudentLoans, ChildBenefits).foreach {
      taxYearPropertyType =>
        s"return the corresponding type object when given valid name: $taxYearPropertyType" in {
          given(featureSwitch.isEnabled(taxYearPropertyType)).willReturn(true)
          AnnualSummaryTypes(featureSwitch).fromName(taxYearPropertyType.name) shouldBe Some(taxYearPropertyType)
        }
    }

  }
}

sealed case class AnnualSummaryTypes(override val featureSwitch: FeatureSwitch)
    extends FeatureSwitchedAnnualSummaryTypes
