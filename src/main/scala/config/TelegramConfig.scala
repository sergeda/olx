package config

import ciris.ConfigDecoder
import com.typesafe.config.Config
import ciris.*
import cats.implicits.catsSyntaxTuple2Parallel
import domain.Telegram.{TelegramGroup, TelegramKey}
import lt.dvim.ciris.Hocon.*
import zio.Task

case class TelegramConfig(
    telegramApiKey: TelegramKey,
    telegramGroupId: TelegramGroup
)

object TelegramConfig {

  implicit val keyConfigDecoder: ConfigDecoder[String, TelegramKey] =
    ConfigDecoder[String, String].map(TelegramKey.apply(_))

  implicit val groupConfigDecoder: ConfigDecoder[String, TelegramGroup] =
    ConfigDecoder[String, String].map(TelegramGroup.apply(_))

  def config(config: Config): ConfigValue[Task, TelegramConfig] =
    val conf = hoconAt(config)("telegram_config")
    (
      env("TELEGRAM_API_KEY").as[TelegramKey],
      conf("group_id").as[TelegramGroup]
    ).parMapN { TelegramConfig.apply }
}
