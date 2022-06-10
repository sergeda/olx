package services

import config.AparserConfig
import config.AparserConfig.JobConfig
import io.circe.*
import io.circe.syntax.*
import services.Aparser.Errors.*
import sttp.client3.circe.*
import sttp.client3.*
import zio.Console.printLine
import zio.prelude.Subtype
import zio.*
import sttp.client3.logging.Logger
import utils.AparserUtils
import domain.Aparser.*

import java.io.IOException
import java.time.{LocalDateTime, OffsetDateTime}
import scala.collection.mutable
import scala.concurrent.duration.{Duration as _, *}
import scala.jdk.CollectionConverters.*

trait Aparser {
  def getAds(jobQuery: JobQuery): IO[AparserError, Vector[IO[ParsingError, Ad]]]

  def getAdDetails(jobQuery: JobQuery): IO[AparserError, AdDetail]
}

object Aparser {
  object Errors {
    sealed trait AparserError

    case class HttpRequestFailed(message: String) extends AparserError

    sealed trait ParsingError extends AparserError

    case class ConvertToAparserResponseFailure(message: String, value: String)
        extends ParsingError

    case class GetAdIdFailed(input: AdUrl) extends ParsingError

    case class BuildAdPublishedFailed(input: String) extends ParsingError

    case class BuildAdOwnerRegisteredFailed(input: String) extends ParsingError

    case object EmptyLineInGetDetail extends ParsingError
  }

  val layer: ZLayer[
    AparserConfig with SttpBackend[Task, Any],
    Nothing,
    Aparser
  ] =
    ZLayer {
      for {
        backend       <- ZIO.service[SttpBackend[Task, Any]]
        aparserConfig <- ZIO.service[AparserConfig]
      } yield AparserLive(backend, aparserConfig)
    }

  val dummyLayer: ZLayer[Any, Nothing, Aparser] = ZLayer(
    ZIO.succeed(new DummyAparser())
  )
}

case class AparserLive(
    backend: SttpBackend[Task, Any],
    aparserConfig: AparserConfig
) extends Aparser {
  def getAds(
      jobQuery: JobQuery
  ): IO[AparserError, Vector[IO[ParsingError, Ad]]] =
    val request: RequestT[Identity, Either[String, String], Any] = basicRequest
      .post(uri"${aparserConfig.url}")
      .body(
        AparserRequest(
          aparserConfig.pass,
          RequestType("oneRequest"),
          JobConfig(
            aparserConfig.getAdsConfig.preset,
            aparserConfig.getAdsConfig.parser,
            aparserConfig.getAdsConfig.configPreset,
            jobQuery
          )
        ).asJson
      )
    backend
      .send(request)
      .zipLeft(ZIO.logInfo(s"getAds: Got response from Aparser"))
      .mapError { e => HttpRequestFailed(e.getMessage) }
      .flatMap(resp => parseGetAdsResponse(resp))

  def getAdDetails(jobQuery: JobQuery): IO[AparserError, AdDetail] =
    val request: RequestT[Identity, Either[String, String], Any] = basicRequest
      .post(uri"${aparserConfig.url}")
      .body(
        AparserRequest(
          aparserConfig.pass,
          RequestType("oneRequest"),
          JobConfig(
            aparserConfig.getAdDetailsConfig.preset,
            aparserConfig.getAdDetailsConfig.parser,
            aparserConfig.getAdDetailsConfig.configPreset,
            jobQuery
          )
        ).asJson
      )
    backend
      .send(request)
      .mapError { e => HttpRequestFailed(e.getMessage) }
      .flatMap(parseGetAdDetailsResponse)

  private def parseGetAdDetailsResponse(
      response: Response[Either[String, String]]
  ): IO[ParsingError, AdDetail] =
    response.body match {
      case Left(value) => IO.fail(ConvertToAparserResponseFailure(value, ""))
      case Right(value) =>
        (for {
          aparserResponse <- AparserUtils.extractAparserResponse(value)
          lines = AparserUtils.extractLines(aparserResponse)
          result <- lines.headOption.fold(IO.fail(EmptyLineInGetDetail))(
            AparserUtils.createAdDetail
          )
        } yield result)
    }

  private def parseGetAdsResponse(
      response: Response[Either[String, String]]
  ): IO[ConvertToAparserResponseFailure, Vector[IO[GetAdIdFailed, Ad]]] =
    response.body match {
      case Left(value) => IO.fail(ConvertToAparserResponseFailure(value, ""))
      case Right(value) =>
        for {
          aparserResponse <- AparserUtils.extractAparserResponse(value)
          lines = AparserUtils.extractLines(aparserResponse)
          result <- ZIO
            .logInfo("Parsed GetAds response")
            .as(lines.map(AparserUtils.createAd))
        } yield result
    }

}

class DummyAparser extends Aparser {
  val url  = AdUrl("http://olx.ua/ad/dummy")
  val adId = AdId("dummyAd")
  def getAds(
      jobQuery: JobQuery
  ): IO[AparserError, Vector[IO[ParsingError, Ad]]] =
    IO.succeed(Vector(IO.succeed(Ad(url, adId))))

  def getAdDetails(jobQuery: JobQuery): IO[AparserError, AdDetail] =
    IO.succeed(
      AdDetail(
        adUrl = url,
        adId = adId,
        adPublished = AdPublished(LocalDateTime.now()),
        adOwnerRegistered = AdOwnerRegistered(LocalDateTime.now()),
        adOwner = AdOwner("James Joeh"),
        adCost = AdCost("34.56"),
        adCurrency = AdCurrency("UAH"),
        adPhoto = AdPhoto("http://link-to-photo.jpg"),
        adOwnerPhone = AdOwnerPhone("0673456478"),
        adLocation = AdLocation("Lviv"),
        adTitle = AdTitle("Rent my apartment")
      )
    )
}
