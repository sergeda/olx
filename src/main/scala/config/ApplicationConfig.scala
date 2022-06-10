package config

import com.typesafe.config.Config
import cats.implicits.catsSyntaxTuple2Parallel
import ciris.*
import zio.Task

case class ApplicationConfig(
    telegramConfig: TelegramConfig,
    aparserConfig: AparserConfig
)

object ApplicationConfig {
  def config(config: Config): ConfigValue[Task, ApplicationConfig] =
    (TelegramConfig.config(config), AparserConfig.config(config))
      .parMapN(ApplicationConfig.apply)
}
