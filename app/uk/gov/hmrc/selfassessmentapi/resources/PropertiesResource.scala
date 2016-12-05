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

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.FeatureSwitchAction
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.domain.Properties
import uk.gov.hmrc.selfassessmentapi.resources.SelfEmploymentsResource.{NotFound, Ok}
import uk.gov.hmrc.selfassessmentapi.resources.models._
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.{AnnualSummary, PropertiesPeriod}
import uk.gov.hmrc.selfassessmentapi.services.PropertiesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PropertiesResource extends PeriodResource[PropertyLocation, PropertiesPeriod, Properties] with BaseResource {
  override val context = AppContext.apiGatewayLinkContext
  override val service = PropertiesService()
  override val sourceType = SourceType.Properties

  private val annSummaryFeatureSwitch = FeatureSwitchAction(sourceType, "annual")

  def updateAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear): Action[JsValue] = annSummaryFeatureSwitch.asyncFeatureSwitch { request =>
    validate[AnnualSummary, Boolean](request.body) {
      service.updateAnnualSummary(nino, id, taxYear, _)
    } match {
      case Left(errorResult) =>
        Future.successful {
          errorResult match {
            case GenericErrorResult(message) => BadRequest(Json.toJson(Errors.badRequest(message)))
            case ValidationErrorResult(errors) => BadRequest(Json.toJson(Errors.badRequest(errors)))
          }
        }
      case Right(result) => result.map {
        case true => NoContent
        case false => NotFound
      }
    }
  }

  def retrieveAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear): Action[AnyContent] = annSummaryFeatureSwitch.asyncFeatureSwitch {
    service.retrieveAnnualSummary(id, taxYear, nino).map {
      case Some(summary) => Ok(Json.toJson(summary))
      case None => NotFound
    }
  }

}
