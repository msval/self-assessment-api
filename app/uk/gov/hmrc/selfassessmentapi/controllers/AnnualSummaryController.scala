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

package uk.gov.hmrc.selfassessmentapi.controllers

import play.api.hal.HalLink
import play.api.libs.json.Json
import play.api.libs.json.Json._
import play.api.mvc.Action
import play.api.mvc.hal._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.config.AppContext
import uk.gov.hmrc.selfassessmentapi.controllers.api.blindperson.BlindPersons
import uk.gov.hmrc.selfassessmentapi.controllers.api.charitablegiving.CharitableGivings
import uk.gov.hmrc.selfassessmentapi.controllers.api.childbenefit.ChildBenefits
import uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution.PensionContributions
import uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan.StudentLoans
import uk.gov.hmrc.selfassessmentapi.controllers.api.taxrefundedorsetoff.TaxRefundedOrSetOffs
import uk.gov.hmrc.selfassessmentapi.controllers.api.{AnnualSummaryType, FeatureSwitchedAnnualSummaryTypes, TaxYear}
import uk.gov.hmrc.selfassessmentapi.controllers.live.annualsummaries._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait AnnualSummaryController extends BaseController with Links {
  override val context: String = AppContext.apiGatewayLinkContext

  private def service(annualSummaryType: String): AnnualSummaryService[_] = {
    val summaryType = FeatureSwitchedAnnualSummaryTypes.types.find(_.name == annualSummaryType)
    val service = summaryType.map(annualSummaryService)
    service.getOrElse(NotFoundAnnualSummaryService)
  }

  private def annualSummaryService(annualSummaryType: AnnualSummaryType): AnnualSummaryService[_] = annualSummaryType match {
    case PensionContributions => PensionContributionService
    case CharitableGivings => CharitableGivingService
    case BlindPersons => BlindPersonService
    case StudentLoans => StudentLoanService
    case TaxRefundedOrSetOffs => TaxRefundedOrSetOffService
    case ChildBenefits => ChildBenefitService
  }

  def find(utr: SaUtr, taxYear: TaxYear, annualSummaryType: String) = Action.async {
    service(annualSummaryType).find(utr, taxYear) map {
      case Some(summary) => Ok(halResource(summary, Set(HalLink("self", s"/$utr/$taxYear/$annualSummaryType"))))
      case None => notFound
    }
  }

  def createOrUpdate(utr: SaUtr, taxYear: TaxYear, annualSummaryType: String) = Action.async(parse.json) { request =>
    service(annualSummaryType).createOrUpdate(utr, taxYear, request.body) match {
      case Left(error) => Future.successful {
        error match {
          case GenericErrorResult(message) => BadRequest(Json.toJson(invalidRequest(message)))
          case ValidationErrorResult(errors) => BadRequest(Json.toJson(invalidRequest(errors)))
        }
      }
      case Right(result) => result.map {
        case true => Created(halResource(obj(), Set(HalLink("self", s"/$utr/$taxYear/$annualSummaryType"))))
        case false => notFound
      }
    }
  }
}
