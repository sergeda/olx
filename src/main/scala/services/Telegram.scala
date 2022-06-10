package services

import config.TelegramConfig
import sttp.client3.*
import zio.Console.printLine
import sttp.{capabilities => cpb}
import zio.*

import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.{Duration as _, *}
import scala.util.Try
import domain.Aparser.*

trait Telegram {
  def sendMessage(message: AdDetail): Task[Unit]
}

case class TelegramLive(
    backend: SttpBackend[Task, Any],
    telegramConfig: TelegramConfig
) extends Telegram {

  override def sendMessage(message: AdDetail): Task[Unit] = {
    val request = basicRequest
      .get(
        uri"https://api.telegram.org/bot${telegramConfig.telegramApiKey}/sendMessage?chat_id=${telegramConfig.telegramGroupId}&text=${format(message)}"
      )
    backend.send(request).unit
  }

  private def format(adDetail: AdDetail): String =
    s"""${adDetail.adTitle}
       |${adDetail.adPhoto}
       |Цена: ${adDetail.adCost} ${adDetail.adCurrency}
       |Автор: ${adDetail.adOwner}
       |На OLX с: ${adDetail.adOwnerRegistered}
       |Телефон: ${adDetail.adOwnerPhone}
       |Опубликовано: ${adDetail.adPublished}
       |Расположение: ${adDetail.adLocation}
       |${adDetail.adUrl}
       |
     """.stripMargin
}

class DummyTelegram extends Telegram {
  def sendMessage(message: AdDetail): Task[Unit] =
    ZIO.logInfo(message.toString).unit
}

object Telegram {
  val layer: ZLayer[SttpBackend[
    Task,
    cpb.zio.ZioStreams & cpb.WebSockets
  ] & TelegramConfig, Throwable, Telegram] =
    ZLayer.fromFunction[SttpBackend[
      Task,
      cpb.zio.ZioStreams & cpb.WebSockets
    ], TelegramConfig, Telegram](TelegramLive.apply)

  val dummyLayer: ZLayer[Any, Nothing, Telegram] =
    ZLayer(ZIO.succeed(new DummyTelegram()))
}
