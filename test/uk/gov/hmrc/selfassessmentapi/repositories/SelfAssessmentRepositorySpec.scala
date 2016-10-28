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

import org.joda.time.{DateTime, LocalDate}
import org.scalatest.BeforeAndAfterEach
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.selfassessmentapi.MongoEmbeddedDatabase
import uk.gov.hmrc.selfassessmentapi.controllers.api.blindperson.BlindPerson
import uk.gov.hmrc.selfassessmentapi.controllers.api.charitablegiving.CharitableGiving
import uk.gov.hmrc.selfassessmentapi.controllers.api.childbenefit.ChildBenefit
import uk.gov.hmrc.selfassessmentapi.controllers.api.pensioncontribution.{PensionContribution, PensionSaving}
import uk.gov.hmrc.selfassessmentapi.controllers.api.studentsloan.StudentLoan
import uk.gov.hmrc.selfassessmentapi.controllers.api.taxrefundedorsetoff.TaxRefundedOrSetOff
import uk.gov.hmrc.selfassessmentapi.repositories.domain.SelfAssessment

import scala.concurrent.ExecutionContext.Implicits.global

class SelfAssessmentRepositorySpec extends MongoEmbeddedDatabase with BeforeAndAfterEach {

  private val mongoRepository = new SelfAssessmentMongoRepository

  override def beforeEach() {
    await(mongoRepository.drop)
    await(mongoRepository.ensureIndexes)
  }

  val saUtr = generateSaUtr()

  "touch" should {
    "create self assessment record if it does not exists" in {
      await(mongoRepository.touch(saUtr, taxYear))

      val sa = await(mongoRepository.findBy(saUtr, taxYear))

      sa match {
        case Some(created) =>
          await(mongoRepository.touch(saUtr, taxYear))
          val updated = await(mongoRepository.findBy(saUtr, taxYear)).get
          updated.createdDateTime shouldEqual created.createdDateTime
        case None => fail("SA not created")
      }
    }

    "update last modified if it does exist" in {
      val sa1 = SelfAssessment(BSONObjectID.generate,
                               saUtr,
                               taxYear,
                               DateTime.now().minusMonths(1),
                               DateTime.now().minusWeeks(1))

      await(mongoRepository.insert(sa1))
      await(mongoRepository.touch(saUtr, taxYear))

      val sa = await(mongoRepository.findBy(saUtr, taxYear))

      sa match {
        case Some(updated) => updated.lastModifiedDateTime.isAfter(sa1.lastModifiedDateTime) shouldEqual true
        case None => fail("SA does not exist")
      }
    }
  }

  "findBy" should {
    "return records matching utr and tax year" in {
      val utr2: SaUtr = generateSaUtr()
      val sa1 = SelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now(), DateTime.now())
      val sa2 = SelfAssessment(BSONObjectID.generate, utr2, taxYear, DateTime.now(), DateTime.now())

      await(mongoRepository.insert(sa1))
      await(mongoRepository.insert(sa2))

      val records = await(mongoRepository.findBy(saUtr, taxYear))

      records.size shouldBe 1
      records.head.saUtr shouldEqual sa1.saUtr
    }
  }

  "findOlderThan" should {
    "return records modified older than the lastModifiedDate" in {
      val sa1 = SelfAssessment(BSONObjectID.generate,
                               saUtr,
                               taxYear,
                               DateTime.now().minusMonths(1),
                               DateTime.now().minusMonths(1))
      val sa2 = SelfAssessment(BSONObjectID.generate,
                               generateSaUtr(),
                               taxYear,
                               DateTime.now().minusWeeks(2),
                               DateTime.now().minusWeeks(2))
      val sa3 = SelfAssessment(BSONObjectID.generate,
                               generateSaUtr(),
                               taxYear,
                               DateTime.now().minusDays(1),
                               DateTime.now().minusDays(1))

      await(mongoRepository.insert(sa1))
      await(mongoRepository.insert(sa2))
      await(mongoRepository.insert(sa3))

      val records = await(mongoRepository.findOlderThan(DateTime.now().minusWeeks(1)))

      records.size shouldBe 2
      records.head.saUtr shouldEqual sa1.saUtr
      records.last.saUtr shouldEqual sa2.saUtr
    }
  }

  "delete" should {
    "only delete records matching utr and tax year" in {
      val utr2: SaUtr = generateSaUtr()
      val sa1 = SelfAssessment(BSONObjectID.generate, saUtr, taxYear, DateTime.now(), DateTime.now())
      val sa2 = SelfAssessment(BSONObjectID.generate, utr2, taxYear, DateTime.now(), DateTime.now())

      await(mongoRepository.insert(sa1))
      await(mongoRepository.insert(sa2))

      await(mongoRepository.delete(saUtr, taxYear))

      val records = await(mongoRepository.findBy(utr2, taxYear))

      records.size shouldBe 1
      records.head.saUtr shouldEqual sa2.saUtr
    }
  }

  "find annual summary" should {
    "return an pension contribution matching utr and tax year" in {
      val pensionContributions = PensionContribution.example()
      await(mongoRepository.PensionContributionRepository.createOrUpdate(saUtr, taxYear, pensionContributions))
      val records = await(mongoRepository.PensionContributionRepository.find(saUtr, taxYear))
      records.size shouldBe 1
      records shouldEqual Some(pensionContributions)
    }

    "return a charitable giving matching utr and tax year" in {
      val charitableGiving = CharitableGiving.example()
      await(mongoRepository.CharitableGivingRepository.createOrUpdate(saUtr, taxYear, charitableGiving))
      val records = await(mongoRepository.CharitableGivingRepository.find(saUtr, taxYear))
      records.size shouldBe 1
      records shouldEqual Some(charitableGiving)
    }

    "return a blind person matching utr and tax year" in {
      val blindPerson = BlindPerson.example()
      await(mongoRepository.BlindPersonRepository.createOrUpdate(saUtr, taxYear, blindPerson))
      val records = await(mongoRepository.BlindPersonRepository.find(saUtr, taxYear))
      records.size shouldBe 1
      records shouldEqual Some(blindPerson)
    }

    "return a student loan matching utr and tax year" in {
      val studentLoan = StudentLoan.example()
      await(mongoRepository.StudentLoanRepository.createOrUpdate(saUtr, taxYear, studentLoan))
      val records = await(mongoRepository.StudentLoanRepository.find(saUtr, taxYear))
      records.size shouldBe 1
      records shouldEqual Some(studentLoan)
    }

    "return a tax refunded or set off matching utr and tax year" in {
      val taxRefundedOrSetOff = TaxRefundedOrSetOff.example()
      await(mongoRepository.TaxRefundedOrSetOffRepository.createOrUpdate(saUtr, taxYear, taxRefundedOrSetOff))
      val records = await(mongoRepository.TaxRefundedOrSetOffRepository.find(saUtr, taxYear))
      records.size shouldBe 1
      records shouldEqual Some(taxRefundedOrSetOff)
    }

    "return a child benefit matching utr and tax year" in {
      val childBenefit = ChildBenefit.example()
      await(mongoRepository.ChildBenefitsRepository.createOrUpdate(saUtr, taxYear, childBenefit))
      val records = await(mongoRepository.ChildBenefitsRepository.find(saUtr, taxYear))
      records.size shouldBe 1
      records shouldEqual Some(childBenefit)
    }

    "return a combination of annual summaries" in {
      val childBenefit = ChildBenefit.example()
      val blindPerson = BlindPerson.example()
      val pensionContribution = PensionContribution.example()

      await(mongoRepository.ChildBenefitsRepository.createOrUpdate(saUtr, taxYear, childBenefit))
      await(mongoRepository.BlindPersonRepository.createOrUpdate(saUtr, taxYear, blindPerson))
      await(mongoRepository.PensionContributionRepository.createOrUpdate(saUtr, taxYear, pensionContribution))

      val records = await(mongoRepository.findBy(saUtr, taxYear))

      records.isDefined shouldBe true
      records.get.childBenefit shouldBe Some(childBenefit)
      records.get.blindPerson shouldBe Some(blindPerson)
      records.get.pensionContribution shouldBe Some(pensionContribution)
    }
  }

  "create or update" should {
    "update the pension contribution matching utr and tax year" in {
      val pensionContributions = PensionContribution(ukRegisteredPension = Some(10000.00))
      await(mongoRepository.PensionContributionRepository.createOrUpdate(saUtr, taxYear, pensionContributions))
      val pensionContributions2 = PensionContribution(ukRegisteredPension = Some(50000.00),
                                                      pensionSavings = Some(PensionSaving(Some(500.00), Some(500.00))))

      await(mongoRepository.PensionContributionRepository.createOrUpdate(saUtr, taxYear, pensionContributions2))

      val records = await(mongoRepository.PensionContributionRepository.find(saUtr, taxYear))
      records shouldEqual Some(pensionContributions2)
    }

    "update the charitable giving matching utr and tax year" in {
      val charitableGiving = CharitableGiving.example().copy(landProperties = None)
      await(mongoRepository.CharitableGivingRepository.createOrUpdate(saUtr, taxYear, charitableGiving))
      val charitableGiving2 = CharitableGiving.example()

      await(mongoRepository.CharitableGivingRepository.createOrUpdate(saUtr, taxYear, charitableGiving2))

      val records = await(mongoRepository.CharitableGivingRepository.find(saUtr, taxYear))
      records shouldEqual Some(charitableGiving2)
    }

    "update the blind person matching utr and tax year" in {
      val blindPerson = BlindPerson.example().copy(wantSpouseToUseSurplusAllowance = None)
      await(mongoRepository.BlindPersonRepository.createOrUpdate(saUtr, taxYear, blindPerson))
      val blindPerson2 = BlindPerson.example()

      await(mongoRepository.BlindPersonRepository.createOrUpdate(saUtr, taxYear, blindPerson2))

      val records = await(mongoRepository.BlindPersonRepository.find(saUtr, taxYear))
      records shouldEqual Some(blindPerson2)
    }

    "update the student loan matching utr and tax year" in {
      val studentLoan = StudentLoan.example().copy(deductedByEmployers = None)
      await(mongoRepository.StudentLoanRepository.createOrUpdate(saUtr, taxYear, studentLoan))
      val studentLoan2 = StudentLoan.example()

      await(mongoRepository.StudentLoanRepository.createOrUpdate(saUtr, taxYear, studentLoan2))

      val records = await(mongoRepository.StudentLoanRepository.find(saUtr, taxYear))
      records shouldEqual Some(studentLoan2)
    }

    "update the tax refunded or set off matching utr and tax year" in {
      val taxRefundedOrSetOff = TaxRefundedOrSetOff(amount = 50)
      await(mongoRepository.TaxRefundedOrSetOffRepository.createOrUpdate(saUtr, taxYear, taxRefundedOrSetOff))
      val taxRefundedOrSetOff2 = TaxRefundedOrSetOff(amount = 500.25)

      await(mongoRepository.TaxRefundedOrSetOffRepository.createOrUpdate(saUtr, taxYear, taxRefundedOrSetOff2))

      val records = await(mongoRepository.TaxRefundedOrSetOffRepository.find(saUtr, taxYear))
      records shouldEqual Some(taxRefundedOrSetOff2)
    }

    "update the child benefit matching utr and tax year" in {
      val childBenefit = ChildBenefit.example().copy(dateBenefitStopped = None)
      await(mongoRepository.ChildBenefitsRepository.createOrUpdate(saUtr, taxYear, childBenefit))
      val childbenefit2 = ChildBenefit.example().copy(dateBenefitStopped = Some(LocalDate.now()))

      await(mongoRepository.ChildBenefitsRepository.createOrUpdate(saUtr, taxYear, childbenefit2))

      val records = await(mongoRepository.ChildBenefitsRepository.find(saUtr, taxYear))
      records shouldEqual Some(childbenefit2)
    }
  }

}
