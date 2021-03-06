/*
 * Copyright 2017 HM Revenue & Customs
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

import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import play.api.libs.json.{Format, Json}
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.selfassessmentapi.resources.models.AccountingType.AccountingType
import uk.gov.hmrc.selfassessmentapi.resources.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.resources.models._
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.PropertyType.PropertyType
import uk.gov.hmrc.selfassessmentapi.resources.models.properties._
import uk.gov.hmrc.selfassessmentapi.resources.models.{AccountingPeriod, TaxYear}

case class Properties(id: BSONObjectID,
                      nino: Nino,
                      accountingType: AccountingType,
                      lastModifiedDateTime: DateTime = DateTime.now(DateTimeZone.UTC),
                      accountingPeriod: AccountingPeriod = AccountingPeriod(LocalDate.parse("2017-04-06"), LocalDate.parse("2018-04-05")),
                      fhlBucket: FHLPropertiesBucket = FHLPropertiesBucket(Map.empty, Map.empty),
                      otherBucket: OtherPropertiesBucket = OtherPropertiesBucket(Map.empty, Map.empty))
    extends LastModifiedDateTime {

  def toModel = properties.Properties(accountingType = accountingType)

  def annualSummary(propertyType: PropertyType, key: TaxYear): PropertiesAnnualSummary = propertyType match {
    case PropertyType.OTHER => otherBucket.annualSummaries.getOrElse(key, new OtherPropertiesAnnualSummary(None, None))
    case PropertyType.FHL => fhlBucket.annualSummaries.getOrElse(key, new FHLPropertiesAnnualSummary(None, None))
  }
}

object Properties {
  implicit val mongoFormats: Format[Properties] = ReactiveMongoFormats.mongoEntity({
    implicit val BSONObjectIDFormat: Format[BSONObjectID] = ReactiveMongoFormats.objectIdFormats
    implicit val dateTimeFormat: Format[DateTime] = ReactiveMongoFormats.dateTimeFormats
    implicit val localDateFormat: Format[LocalDate] = ReactiveMongoFormats.localDateFormats
    Format(Json.reads[Properties], Json.writes[Properties])
  })
}

 trait PropertyPeriodOps[T <: Period, P <: PeriodicData] {
  def periods(properties: Properties): Map[PeriodId, T]

  def validatePeriod(period: T, properties: Properties): Either[Errors.Error, Properties]

  def periodExists(periodId: PeriodId, properties: Properties): Boolean

  def period(periodId: PeriodId, properties: Properties): Option[T]

  def setPeriodsTo(periodId: PeriodId, period: T, properties: Properties): Properties

  def update(periodId: PeriodId, periodicData: P, properties: Properties): Properties
}

object PropertyPeriodOps {
  implicit object OtherPeriodOps extends PropertyPeriodOps[OtherProperties, OtherPeriodicData] {

    override def periods(properties: Properties): Map[PeriodId, OtherProperties] = properties.otherBucket.periods

    override def validatePeriod(period: OtherProperties, properties: Properties): Either[Errors.Error, Properties] = {
      val errors = properties.otherBucket.validatePeriod(period, properties.accountingPeriod)
      errors.map(Left(_)).getOrElse(Right(properties))
    }

    override def periodExists(periodId: PeriodId, properties: Properties): Boolean = period(periodId, properties).isDefined

    override def period(periodId: PeriodId, properties: Properties): Option[OtherProperties] =
      properties.otherBucket.periods.get(periodId)

    override def setPeriodsTo(periodId: PeriodId, period: OtherProperties, properties: Properties): Properties =
      properties.copy(otherBucket = properties.otherBucket.copy(periods = properties.otherBucket.periods.updated(periodId, period)))

    override def update(periodId: PeriodId, periodicData: OtherPeriodicData, properties: Properties): Properties = {
      val periodOpt = properties.otherBucket.periods.find(period => period._1.equals(periodId))

      periodOpt.map { period =>
        setPeriodsTo(periodId, period._2.copy(data = periodicData), properties)
      }.get
    }
  }

  implicit object FHLPeriodOps extends PropertyPeriodOps[FHLProperties, FHLPeriodicData] {

    override def periods(properties: Properties): Map[PeriodId, FHLProperties] = properties.fhlBucket.periods

    override def validatePeriod(period: FHLProperties, properties: Properties): Either[Error, Properties] = {
      val errors = properties.fhlBucket.validatePeriod(period, properties.accountingPeriod)
      errors.map(Left(_)).getOrElse(Right(properties))
    }

    override def periodExists(periodId: PeriodId, properties: Properties): Boolean = period(periodId, properties).isDefined

    override def period(periodId: PeriodId, properties: Properties): Option[FHLProperties] =
      properties.fhlBucket.periods.get(periodId)

    override def setPeriodsTo(periodId: PeriodId, period: FHLProperties, properties: Properties): Properties =
      properties.copy(fhlBucket = properties.fhlBucket.copy(periods = properties.fhlBucket.periods.updated(periodId, period)))

    override def update(periodId: PeriodId, periodicData: FHLPeriodicData, properties: Properties): Properties = {
      val periodOpt = properties.fhlBucket.periods.find(period => period._1.equals(periodId))

      periodOpt.map { period =>
        setPeriodsTo(periodId, period._2.copy(data = periodicData), properties)
      }.get
    }
  }
}
