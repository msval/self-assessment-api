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
import uk.gov.hmrc.selfassessmentapi.resources.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.resources.models.{SourceId, TaxYear}
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.{AnnualSummary, PropertiesPeriod}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PropertiesService extends PeriodService[Location, PropertiesPeriod, Properties] {

  override val repository = PropertiesRepository()

  private def create(nino: Nino, location: Location) = {
    val properties = Properties(BSONObjectID.generate, LocalDate.now(DateTimeZone.UTC), nino, location, Map.empty, Map.empty)
    repository.create(properties)
  }

  override def createPeriod(nino: Nino, location: Location, period: PropertiesPeriod): Future[Either[Error, PeriodId]] = {
    repository.retrieve(location, nino).flatMap { opt =>
      if (opt.isEmpty) create(nino, location) else Future.successful(true)
    }.flatMap { successful =>
      if (successful) super.createPeriod(nino, location, period) else throw new RuntimeException("Could not persist Properties to the database. Is the database available?")
    }
  }

  def updateAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear, summary: AnnualSummary) =
    repository.retrieve(id, nino).flatMap {
      case Some(properties) =>
        repository.update(id, nino, properties.copy(annualSummaries = properties.annualSummaries.updated(taxYear, summary)))
      case None => Future.successful(false)
    }

}

object PropertiesService {
  def apply(): PropertiesService = new PropertiesService

}
