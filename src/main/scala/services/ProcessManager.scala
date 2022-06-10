package services

import config.AparserConfig
import config.AparserConfig.Categories
import services.Aparser.Errors.*
import utils.AparserUtils
import zio.Console.printLine
import zio.*
import domain.Aparser.*

import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.{Duration as _, *}

trait ProcessManager {
  def runGetAdDetail: URIO[Clock, Fiber.Runtime[Nothing, Nothing]]

  def runGetAds: URIO[Clock, Long]
}

case class ProcessManagerLive(
    aparser: Aparser,
    cache: Cache,
    telegram: Telegram,
    adQueue: AdQueue,
    categories: Categories,
    semaphore: Semaphore
) extends ProcessManager {

  def runGetAds: URIO[Clock, Long] = {
    val result: ZIO[Any, AparserError, List[IO[ParsingError, Ad]]] = ZIO
      .foreachPar(categories.list)(category =>
        aparser.getAds(JobQuery(category)).map(_.toList)
      )
      .withParallelism(8)
      .map(_.flatten)
    result
      .flatMap(list => ZIO.foreachPar(list)(processGetAd))
      .zipLeft(ZIO.logInfo(s"Called Get ads"))
      .catchAll(e => ZIO.logError(e.toString).as(List.empty[Unit]))
      .schedule(Schedule.spaced(Duration.fromScala(5.seconds)))
  }

  def runGetAdDetail: URIO[Clock, Fiber.Runtime[Nothing, Nothing]] =
    (for {
      ad <- adQueue.take
      _ <- processGetAdsDetails(ad).zipLeft(
        ZIO.logInfo(s"Called get ad details for ad $ad")
      )
    } yield ()).forever.fork

  private def processGetAd(adComputation: IO[ParsingError, Ad]): UIO[Unit] =
    adComputation
      .flatMap { ad =>
        semaphore.withPermit(for {
          alreadyInCache <- cache.contains(ad.adId)
          _ <- ZIO.ifZIO(
            ZIO.succeed(alreadyInCache || AparserUtils.isPaid(ad.adUrl))
          )(
            ZIO.unit,
            for {
              _ <- adQueue.offer(ad) <* ZIO.logInfo(
                s"Added new ad to queue $ad"
              )
              _ <- cache
                .put(ad.adId)
                .catchAll(er => ZIO.logInfo("Can't add ad to cache"))
            } yield ()
          )
        } yield ())
      }
      .catchAll(e => ZIO.logError(e.toString))

  private def processGetAdsDetails(ad: Ad): URIO[Clock, Unit] =
    (for {
      adDetails <- aparser
        .getAdDetails(JobQuery(ad.adUrl))
      now <- Clock.currentDateTime
      _ <- ZIO.ifZIO(ZIO.succeed(AparserUtils.isCurrent(now, adDetails)))(
        ZIO
          .logInfo(s"Sending to Telegram ${adDetails}")
          .zipLeft(
            telegram
              .sendMessage(adDetails)
              .catchAll(er =>
                ZIO.logError(
                  s"Failed to send message to Telegram. Error: ${er.getMessage}"
                )
              )
          ),
        ZIO.unit
      )
    } yield ())
      .catchAll(er => ZIO.logError(s"Failed to get Ad Details. Error: $er"))

}

object ProcessManager {
  val layer: ZLayer[
    Telegram & Cache & Aparser & AdQueue & AparserConfig,
    Nothing,
    ProcessManager
  ] = ZLayer(
    for {
      aparser       <- ZIO.service[Aparser]
      cache         <- ZIO.service[Cache]
      telegram      <- ZIO.service[Telegram]
      adQueue       <- ZIO.service[AdQueue]
      aparserConfig <- ZIO.service[AparserConfig]
      semaphore     <- Semaphore.make(1)
    } yield ProcessManagerLive(
      aparser,
      cache,
      telegram,
      adQueue,
      aparserConfig.categories,
      semaphore
    )
  )
}
