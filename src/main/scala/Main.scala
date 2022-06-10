import com.typesafe.config.{Config, ConfigFactory}
import services.*
import sttp.capabilities.WebSockets
import sttp.capabilities.zio.ZioStreams
import sttp.client3.SttpBackend
import sttp.client3.httpclient.zio.HttpClientZioBackend
import sttp.client3.logging.LoggingBackend
import config.*
import utils.SttpLogger
import zio.{System => _, *}
import zio.Console.printLine
import zio.stream.{ZPipeline, ZSink, ZStream}
import zio.logging.*
import sttp.client3.logging.Logger
import domain.Aparser.*
import zio.interop.catz._

import java.io.{FileInputStream, IOException}
import scala.concurrent.duration.{Duration as _, *}

object Main extends ZIOAppDefault {

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
    val program =
      for {
        manager <- ZIO.service[ProcessManager]
        _ <- ZIO.collectAllParDiscard(
          ZIO.replicate(5)(manager.runGetAdDetail)
        )
        _ <- manager.runGetAds
      } yield ()

    val applicationConfigLayer: ZLayer[Any, Throwable, ApplicationConfig] =
      ZLayer(
        Task
          .attempt(ConfigFactory.load())
          .flatMap(conf => ApplicationConfig.config(conf).load[Task])
      )

    val telegramConfigLayer =
      applicationConfigLayer.map(conf => ZEnvironment(conf.get.telegramConfig))

    val aparserConfigLayer =
      applicationConfigLayer.map(conf => ZEnvironment(conf.get.aparserConfig))

    val telegramLayer =
      (telegramConfigLayer ++ ZLayer(HttpClientZioBackend())) >>>
        Telegram.dummyLayer

    val cacheLayer: ZLayer[Any, Nothing, Cache] =
      Cache.layer(Duration.fromScala(30.days))
    val adQueueLayer = ZLayer(Queue.unbounded[Ad]) >>> AdQueue.layer
    val aparserLayer =
      (aparserConfigLayer ++ ZLayer(HttpClientZioBackend())) >>>
        Aparser.dummyLayer

    val schedulerLayer =
      (aparserConfigLayer ++ aparserLayer ++ cacheLayer ++ telegramLayer ++ adQueueLayer) >>> ProcessManager.layer

    val readyToRun = program.provide(
      Console.live ++ Clock.live ++ telegramLayer ++ aparserLayer ++ cacheLayer ++ schedulerLayer
    )
    readyToRun
}
