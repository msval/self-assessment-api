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

package uk.gov.hmrc.selfassessmentapi.services

import org.joda.time.{DateTimeZone, LocalDate}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.controllers.api.{Location, PeriodId}
import uk.gov.hmrc.selfassessmentapi.domain.Properties
import uk.gov.hmrc.selfassessmentapi.repositories.PropertiesRepository
import uk.gov.hmrc.selfassessmentapi.resources.Errors.Error
import uk.gov.hmrc.selfassessmentapi.resources.models.periods.PropertiesPeriod

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PropertiesService extends PeriodService[Location, PropertiesPeriod, Properties] {
  override val periodRepository = PropertiesRepository()

  private def create(nino: Nino, location: Location) = {
    val properties = Properties(BSONObjectID.generate, LocalDate.now(DateTimeZone.UTC), nino, location, Map.empty)
    periodRepository.create(properties)
  }

  override def createPeriod(nino: Nino, location: Location, period: PropertiesPeriod): Future[Either[Error, PeriodId]] = {
    periodRepository.retrieve(location, nino).map {
      case None => create(nino, location)
      case _ =>
    }.flatMap { _ =>
      super.createPeriod(nino, location, period)
    }
  }
}

object PropertiesService {
  def apply(): PropertiesService = new PropertiesService
}
