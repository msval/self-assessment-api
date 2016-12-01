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

import org.joda.time.LocalDate
import play.api.libs.json.{Format, Json}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.controllers.api.{Location, PeriodId}
import uk.gov.hmrc.selfassessmentapi.resources.models.AccountingPeriod
import uk.gov.hmrc.selfassessmentapi.resources.models.periods.PropertiesPeriod

case class Properties(id: BSONObjectID,
                      lastModifiedDateTime: LocalDate,
                      nino: Nino,
                      location: Location,
                      periods: Map[PeriodId, PropertiesPeriod]) extends PeriodContainer[PropertiesPeriod, Properties] with LastModifiedDateTime {
  // TODO: Post-MVP, the accounting period should be derived from the current tax year.
  private val startTaxYear = LocalDate.parse("2016-04-06")
  private val endTaxYear = LocalDate.parse("2017-04-05")
  private val accountingPeriod = AccountingPeriod(startTaxYear, endTaxYear)

  override def setPeriodsTo(periodId: PeriodId, period: PropertiesPeriod): Properties =
    this.copy(periods = periods.updated(periodId, period))

  override def containsMisalignedPeriod(period: PropertiesPeriod): Boolean = {
    if (periods.isEmpty) !period.from.isEqual(accountingPeriod.start)
    else !(period.to.isBefore(accountingPeriod.end) || period.to.isEqual(accountingPeriod.end))
  }
}

object Properties {
  implicit val mongoFormats = ReactiveMongoFormats.mongoEntity({
    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val localDateFormat: Format[LocalDate] = ReactiveMongoFormats.localDateFormats
    Format(Json.reads[Properties], Json.writes[Properties])
  })
}
