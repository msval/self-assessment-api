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

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.controllers.api.{JsonMarshaller, TaxYear}
import uk.gov.hmrc.selfassessmentapi.controllers.controllers.validate
import uk.gov.hmrc.selfassessmentapi.repositories.AnnualSummaryRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

abstract class AnnualSummaryService[T](marshaller: JsonMarshaller[T]) {
  val repository: AnnualSummaryRepository[T]
  implicit val reads = marshaller.reads
  implicit val writes = marshaller.writes

  def find(utr: SaUtr, taxYear: TaxYear): Future[Option[JsValue]] = repository.find(utr, taxYear).map(_.map(Json.toJson(_)))

  def createOrUpdate(utr: SaUtr, taxYear: TaxYear, value: JsValue): Either[ErrorResult, Future[Boolean]] = {
    // FIXME: Move validation to controller.
    validate[T, Boolean](value) {
      repository.createOrUpdate(utr, taxYear, _)
    }
  }
}
