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

package uk.gov.hmrc.selfassessmentapi.controllers.live

import play.api.hal.HalLink
import play.api.libs.json.Json._
import play.api.mvc.Action
import play.api.mvc.hal._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, FeatureConfig}
import uk.gov.hmrc.selfassessmentapi.controllers
import uk.gov.hmrc.selfassessmentapi.controllers.api._
import scala.concurrent.Future

object TaxYearDiscoveryController extends controllers.TaxYearDiscoveryController {
  override val context: String = AppContext.apiGatewayLinkContext

  def discoverTaxYear(utr: SaUtr, taxYear: TaxYear) = Action.async { _ =>
    val halLinks = buildSourceHalLinks(utr, taxYear) ++
      buildAnnualSummaryHalLinks(utr, taxYear) + HalLink("self", discoverTaxYearHref(utr, taxYear))

    Future.successful(Ok(halResource(obj(), halLinks)))
  }

  private def buildSourceHalLinks(utr: SaUtr, taxYear: TaxYear) = {
    SourceTypes.types.filter { source =>
      AppContext.featureSwitch.exists { config =>
        FeatureConfig(config).isSourceEnabled(source.name)
      }
    } map { source =>
      HalLink(source.name, sourceHref(utr, taxYear, source))
    }
  }

  private def buildAnnualSummaryHalLinks(utr: SaUtr, taxYear: TaxYear) = {
    FeatureSwitchedAnnualSummaryTypes.types.map { annualSummaryType =>
      HalLink(annualSummaryType.name, s"/$utr/$taxYear/${annualSummaryType.name}")
    }
  }
}
