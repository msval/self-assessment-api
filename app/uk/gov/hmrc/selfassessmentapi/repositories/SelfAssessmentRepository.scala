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

package uk.gov.hmrc.selfassessmentapi.repositories

import org.joda.time.{DateTime, DateTimeZone}
import play.modules.reactivemongo.MongoDbConnection
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.{BSONBoolean, BSONDateTime, BSONDocument, BSONDouble, BSONElement, BSONInteger, BSONNull, BSONObjectID, BSONString, BSONValue, Producer}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats
import uk.gov.hmrc.mongo.{AtomicUpdate, ReactiveRepository}
import uk.gov.hmrc.selfassessmentapi.controllers.api.blindperson.BlindPerson
import uk.gov.hmrc.selfassessmentapi.controllers.api.charitablegiving.CharitableGiving
import uk.gov.hmrc.selfassessmentapi.controllers.api.childbenefit.ChildBenefit
import uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution.PensionContribution
import uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan.StudentLoan
import uk.gov.hmrc.selfassessmentapi.controllers.api.taxrefundedorsetoff.TaxRefundedOrSetOff
import uk.gov.hmrc.selfassessmentapi.controllers.api.TaxYear
import uk.gov.hmrc.selfassessmentapi.repositories.domain.SelfAssessment

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SelfAssessmentRepository extends MongoDbConnection {
  private lazy val repository = new SelfAssessmentMongoRepository

  def apply() = repository
}

class SelfAssessmentMongoRepository(implicit mongo: () => DB)
  extends ReactiveRepository[SelfAssessment, BSONObjectID]("selfAssessments",
    mongo,
    domainFormat = SelfAssessment.mongoFormats,
    idFormat = ReactiveMongoFormats.objectIdFormats)
    with AtomicUpdate[SelfAssessment] {
  self =>

  override def indexes: Seq[Index] =
    Seq(Index(Seq(("saUtr", Ascending), ("taxYear", Ascending)), name = Some("sa_utr_taxyear"), unique = true),
      Index(Seq(("lastModifiedDateTime", Ascending)), name = Some("sa_last_modified"), unique = false))

  def touch(saUtr: SaUtr, taxYear: TaxYear) = {
    for {
      result <- atomicUpsert(
        BSONDocument("saUtr" -> BSONString(saUtr.toString), "taxYear" -> BSONString(taxYear.toString)),
        touchModifier()
      )
    } yield ()
  }

  private def touchModifier(): BSONDocument = {
    val now = DateTime.now(DateTimeZone.UTC)
    BSONDocument(
      setOnInsert(now),
      "$set" -> BSONDocument(lastModifiedDateTimeModfier(now))
    )
  }

  private def updateAnnualSummary(saUtr: SaUtr, taxYear: TaxYear, summary: BSONDocument): Future[Boolean] = {
    val now = DateTime.now(DateTimeZone.UTC)

    for {
      result <- atomicUpsert(BSONDocument("saUtr" -> saUtr.utr, "taxYear" -> taxYear.taxYear),
        BSONDocument(
          setOnInsert(now),
          "$set" -> summary
        ))
    } yield result.writeResult.ok
  }

  private def setOnInsert(dateTime: DateTime): Producer[BSONElement] =
    "$setOnInsert" -> BSONDocument("createdDateTime" -> BSONDateTime(dateTime.getMillis))

  private def lastModifiedDateTimeModfier(dateTime: DateTime): Producer[BSONElement] =
    "lastModifiedDateTime" -> BSONDateTime(dateTime.getMillis)

  def findBy(saUtr: SaUtr, taxYear: TaxYear): Future[Option[SelfAssessment]] = {
    find(
      "saUtr" -> BSONString(saUtr.toString),
      "taxYear" -> BSONString(taxYear.toString)
    ).map(_.headOption)
  }

  def findOlderThan(lastModified: DateTime): Future[Seq[SelfAssessment]] = {
    find(
      "lastModifiedDateTime" -> BSONDocument("$lt" -> BSONDateTime(lastModified.getMillis))
    )
  }

  def delete(saUtr: SaUtr, taxYear: TaxYear): Future[Boolean] = {
    for (option <- remove("saUtr" -> saUtr.utr, "taxYear" -> taxYear.taxYear)) yield option.n > 0
  }

  def isInsertion(suppliedId: BSONObjectID, returned: SelfAssessment): Boolean = suppliedId.equals(returned.id)

  object ChildBenefitsRepository extends AnnualSummaryRepository[ChildBenefit] {
    override def find(saUtr: SaUtr, taxYear: TaxYear): Future[Option[ChildBenefit]] =
      self.findBy(saUtr, taxYear).map(_.flatMap(_.childBenefit))

    override def createOrUpdate(saUtr: SaUtr, taxYear: TaxYear, childBenefit: ChildBenefit): Future[Boolean] = {
      val bsonDoc = BSONDocument(
        lastModifiedDateTimeModfier(DateTime.now(DateTimeZone.UTC)),
          "childBenefit" -> BSONDocument(
            Seq("amount" -> BSONDouble(childBenefit.amount.doubleValue()),
              "numberOfChildren" -> BSONInteger(childBenefit.numberOfChildren),
              "dateBenefitStopped" -> childBenefit.dateBenefitStopped
                .map(x => BSONString(x.toString))
                .getOrElse(BSONNull))))

      self.updateAnnualSummary(saUtr, taxYear, bsonDoc)
    }
  }

  object TaxRefundedOrSetOffRepository extends AnnualSummaryRepository[TaxRefundedOrSetOff] {
    override def find(saUtr: SaUtr, taxYear: TaxYear): Future[Option[TaxRefundedOrSetOff]] =
      self.findBy(saUtr, taxYear).map(_.flatMap(_.taxRefundedOrSetOff))

    override def createOrUpdate(saUtr: SaUtr, taxYear: TaxYear, taxRefundedOrSetOff: TaxRefundedOrSetOff): Future[Boolean] = {
      val bsonDoc = BSONDocument(
        lastModifiedDateTimeModfier(DateTime.now(DateTimeZone.UTC)),
        "taxRefundedOrSetOff" -> BSONDocument(Seq(
          "amount" -> BSONDouble(taxRefundedOrSetOff.amount.doubleValue()))))

      self.updateAnnualSummary(saUtr, taxYear, bsonDoc)
    }
  }

  object StudentLoanRepository extends AnnualSummaryRepository[StudentLoan] {
    override def find(saUtr: SaUtr, taxYear: TaxYear): Future[Option[StudentLoan]] =
      self.findBy(saUtr, taxYear).map(_.flatMap(_.studentLoan))

    override def createOrUpdate(saUtr: SaUtr, taxYear: TaxYear, loan: StudentLoan): Future[Boolean] = {
      val bsonDoc = BSONDocument(
        lastModifiedDateTimeModfier(DateTime.now(DateTimeZone.UTC)),
        "studentLoan" -> BSONDocument(Seq(
          "planType" -> BSONString(loan.planType.toString),
          "deductedByEmployers" -> loan.deductedByEmployers.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull)
        )))

      self.updateAnnualSummary(saUtr, taxYear, bsonDoc)
    }
  }

  object BlindPersonRepository extends AnnualSummaryRepository[BlindPerson] {
    override def find(saUtr: SaUtr, taxYear: TaxYear): Future[Option[BlindPerson]] =
      self.findBy(saUtr, taxYear).map(_.flatMap(_.blindPerson))

    override def createOrUpdate(saUtr: SaUtr, taxYear: TaxYear, blindPerson: BlindPerson): Future[Boolean] = {
      val bsonDoc = BSONDocument(
        lastModifiedDateTimeModfier(DateTime.now(DateTimeZone.UTC)),
        "blindPerson" -> BSONDocument(Seq(
          "country" -> blindPerson.country.map(x => BSONString(x.toString)).getOrElse(BSONNull),
          "registrationAuthority" -> blindPerson.registrationAuthority.map(BSONString).getOrElse(BSONNull),
          "spouseSurplusAllowance" -> blindPerson.spouseSurplusAllowance.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
          "wantSpouseToUseSurplusAllowance" -> blindPerson.wantSpouseToUseSurplusAllowance.map(BSONBoolean).getOrElse(BSONNull))))

      self.updateAnnualSummary(saUtr, taxYear, bsonDoc)
    }
  }

  object CharitableGivingRepository extends AnnualSummaryRepository[CharitableGiving] {
    override def find(saUtr: SaUtr, taxYear: TaxYear): Future[Option[CharitableGiving]] =
      self.findBy(saUtr, taxYear).map(_.flatMap(_.charitableGiving))

    override def createOrUpdate(saUtr: SaUtr, taxYear: TaxYear, charitableGiving: CharitableGiving): Future[Boolean] = {
      val bsonDoc = BSONDocument(
        lastModifiedDateTimeModfier(DateTime.now(DateTimeZone.UTC)),
        "charitableGiving" -> BSONDocument(Seq(
          "giftAidPayments" -> charitableGiving.giftAidPayments.map(giftAid =>
            BSONDocument(Seq(
              "totalInTaxYear" -> giftAid.totalInTaxYear.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
              "oneOff" -> giftAid.oneOff.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
              "toNonUkCharities" -> giftAid.toNonUkCharities.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
              "carriedBackToPreviousTaxYear" -> giftAid.carriedBackToPreviousTaxYear.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
              "carriedFromNextTaxYear" -> giftAid.carriedFromNextTaxYear.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull)
            ))).getOrElse(BSONNull),
          "sharesSecurities" -> charitableGiving.sharesSecurities.map(sharesAndSecs =>
            BSONDocument(Seq(
              "totalInTaxYear" -> BSONDouble(sharesAndSecs.totalInTaxYear.doubleValue()),
              "toNonUkCharities" -> sharesAndSecs.toNonUkCharities.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull)
            ))).getOrElse(BSONNull),
          "landProperties" -> charitableGiving.landProperties.map(landProperties =>
            BSONDocument(Seq(
              "totalInTaxYear" -> BSONDouble(landProperties.totalInTaxYear.doubleValue()),
              "toNonUkCharities" -> landProperties.toNonUkCharities.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull)
            ))).getOrElse(BSONNull))))

      self.updateAnnualSummary(saUtr, taxYear, bsonDoc)
    }
  }

  object PensionContributionRepository extends AnnualSummaryRepository[PensionContribution] {
    override def find(saUtr: SaUtr, taxYear: TaxYear): Future[Option[PensionContribution]] =
      self.findBy(saUtr, taxYear).map(_.flatMap(_.pensionContribution))

    override def createOrUpdate(saUtr: SaUtr, taxYear: TaxYear, pensionContribution: PensionContribution): Future[Boolean] = {
      val bsonDoc = BSONDocument(
        lastModifiedDateTimeModfier(DateTime.now(DateTimeZone.UTC)),
        "pensionContribution" -> BSONDocument(
          Seq(
            "ukRegisteredPension" -> pensionContribution.ukRegisteredPension.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
            "retirementAnnuity" -> pensionContribution.retirementAnnuity.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
            "employerScheme" -> pensionContribution.employerScheme.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
            "overseasPension" -> pensionContribution.overseasPension.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull),
            "pensionSavings" -> BSONDocument(Seq(
              "excessOfAnnualAllowance" -> pensionContribution.pensionSavings.map(_.excessOfAnnualAllowance.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull)).getOrElse(BSONNull),
              "taxPaidByPensionScheme" -> pensionContribution.pensionSavings.map(_.taxPaidByPensionScheme.map(x => BSONDouble(x.doubleValue())).getOrElse(BSONNull)).getOrElse(BSONNull)
            ))
      )))

      self.updateAnnualSummary(saUtr, taxYear, bsonDoc)
    }
  }

}
