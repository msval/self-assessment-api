package uk.gov.hmrc.selfassessmentapi.controllers.api

import org.mockito.Mockito
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.selfassessmentapi.UnitSpec
import uk.gov.hmrc.selfassessmentapi.config.FeatureSwitch

class FeatureSwitchedAnnualSummaryTypesSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  val mockFeatureSwitch = mock[FeatureSwitch]

  override def beforeEach() = {
    Mockito.reset(mockFeatureSwitch)
  }


}
