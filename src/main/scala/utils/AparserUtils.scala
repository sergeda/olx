package utils

import io.circe.parser.parse
import io.circe.{HCursor, Json}
import services._
import services.Aparser.Errors._
import zio.IO
import domain.Aparser.*

import java.time.OffsetDateTime
import scala.jdk.CollectionConverters.*

object AparserUtils {

  def extractAparserResponse(
      input: String
  ): IO[ConvertToAparserResponseFailure, AparserResponse] =
    val doc             = parse(input).getOrElse(Json.Null)
    val cursor: HCursor = doc.hcursor
    (for {
      data <- cursor.downField("data").get[String]("resultString")
    } yield AparserResponse(data)).fold(
      e => IO.fail(ConvertToAparserResponseFailure(e.message, input)),
      result => IO.succeed(result)
    )

  def extractLines(aparserResponse: AparserResponse): Vector[Line] =
    aparserResponse.lines().toList.asScala.toVector.map(str => Line(str))

  def getAdId(url: AdUrl): IO[GetAdIdFailed, AdId] =
    val r = """-([^-.]+)\.htm""".r
    r.findFirstMatchIn(url)
      .map(_.group(1))
      .map(id => AdId(id))
      .map(IO.succeed)
      .getOrElse(IO.fail(GetAdIdFailed(url)))

  def isCurrent(today: OffsetDateTime, adDetail: AdDetail): Boolean =
    today.toLocalDate == AdPublished.unwrap(adDetail.adPublished).toLocalDate

  def isPaid(url: AdUrl): Boolean = AdUrl.unwrap(url).endsWith("promoted")

  def createAd(line: Line): IO[GetAdIdFailed, Ad] =
    val data: Array[String] = line.split(",").map(_.replace(""""""", ""))
    getAdId(AdUrl(data(0))).map { adId =>
      Ad(
        adUrl = AdUrl(data(0)),
        adId = adId
      )
    }

  def createAdDetail(line: Line): IO[ParsingError, AdDetail] =
    val data: Array[String] = line.split(",").map(_.replace(""""""", ""))
    for {
      adId <- getAdId(AdUrl(data.last))
      adPublished <- getTime(data(9))
        .map(t => AdPublished(t))
        .fold(IO.fail(BuildAdPublishedFailed(data(9))))(IO.succeed)
      adOwnerRegistered <- getTime(data(3))
        .map(t => AdOwnerRegistered(t))
        .fold(IO.fail(BuildAdOwnerRegisteredFailed(data(3))))(IO.succeed)
    } yield AdDetail(
      adUrl = AdUrl(data(12).dropRight(2)),
      adId = adId,
      adPublished = adPublished,
      adOwnerRegistered = adOwnerRegistered,
      adCurrency = AdCurrency(data(7)),
      adCost = AdCost(data(6)),
      adOwner = AdOwner(data(2)),
      adTitle = AdTitle(data(1)),
      adPhoto = AdPhoto(data(10)),
      adOwnerPhone = AdOwnerPhone(data(4)),
      adLocation = AdLocation(data(5))
    )

}
