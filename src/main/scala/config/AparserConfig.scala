package config

import cats.Show
import config.AparserConfig.{Categories, GetAdDetailsConfig, GetAdsConfig}
import domain.Aparser.{
  AparserPassword,
  AparserUrl,
  JobConfigPreset,
  JobParser,
  JobPreset,
  JobQuery
}
import io.circe.{Encoder, Json}
import cats.implicits.*
import ciris.*
import lt.dvim.ciris.Hocon.*
import zio.Task
import scala.jdk.CollectionConverters._
import com.typesafe.config.Config

case class AparserConfig(
    url: AparserUrl,
    pass: AparserPassword,
    categories: Categories,
    getAdsConfig: GetAdsConfig,
    getAdDetailsConfig: GetAdDetailsConfig
)

object AparserConfig {

  implicit val aparserUrlConfigDecoder: ConfigDecoder[String, AparserUrl] =
    ConfigDecoder[String, String].map(AparserUrl.apply(_))

  implicit val aparserPasswordConfigDecoder
      : ConfigDecoder[String, AparserPassword] =
    ConfigDecoder[String, String].map(AparserPassword.apply(_))

  implicit val aparserPasswordShowInstance: Show[AparserPassword] =
    Show.show[AparserPassword](_ => "****")

  implicit val jobPresetConfigDecoder: ConfigDecoder[String, JobPreset] =
    ConfigDecoder[String, String].map(JobPreset.apply(_))

  implicit val jobParserConfigDecoder: ConfigDecoder[String, JobParser] =
    ConfigDecoder[String, String].map(JobParser.apply(_))

  implicit val jobConfigPresetConfigDecoder
      : ConfigDecoder[String, JobConfigPreset] =
    ConfigDecoder[String, String].map(JobConfigPreset.apply(_))

  def config(config: Config): ConfigValue[Task, AparserConfig] =
    (
      env("APARSER_URL").as[AparserUrl].covary[Task],
      env("APARSER_PASSWORD").as[AparserPassword].secret.covary[Task],
      getCategories(config),
      getAdsConfig(config),
      getAdDetailsConfig(config)
    ).parMapN { (url, password, categories, adsConfig, adDetailsConfig) =>
      AparserConfig(url, password.value, categories, adsConfig, adDetailsConfig)
    }

  def getAdsConfig(config: Config): ConfigValue[Task, GetAdsConfig] =
    val conf = hoconAt(config)("aparser_config.get_ads_config")
    (
      conf("preset").as[JobPreset],
      conf("parser").as[JobParser],
      conf("configPreset").as[JobConfigPreset]
    ).parMapN { GetAdsConfig.apply }

  def getAdDetailsConfig(
      config: Config
  ): ConfigValue[Task, GetAdDetailsConfig] =
    val conf = hoconAt(config)("aparser_config.get_ad_details_config")
    (
      conf("preset").as[JobPreset],
      conf("parser").as[JobParser],
      conf("configPreset").as[JobConfigPreset]
    ).parMapN { GetAdDetailsConfig.apply }

  def getCategories(config: Config): ConfigValue[Task, Categories] =
    ConfigValue.eval(
      Task
        .attempt(
          config.getStringList("aparser_config.categories").asScala.toList
        )
        .map(cats =>
          ConfigValue
            .loaded(ConfigKey("categories"), Categories(cats))
            .covary[Task]
        )
    )

  case class JobConfig(
      preset: JobPreset,
      parser: JobParser,
      configPreset: JobConfigPreset,
      query: JobQuery
  )

  object JobConfig {
    implicit val jobConfigEncoder: Encoder[JobConfig] = (a: JobConfig) =>
      Json.obj(
        ("preset", Json.fromString(a.preset)),
        ("parser", Json.fromString(a.parser)),
        ("configPreset", Json.fromString(a.configPreset)),
        ("query", Json.fromString(a.query))
      )
  }

  case class GetAdsConfig(
      preset: JobPreset,
      parser: JobParser,
      configPreset: JobConfigPreset
  )

  case class GetAdDetailsConfig(
      preset: JobPreset,
      parser: JobParser,
      configPreset: JobConfigPreset
  )

  case class Categories(list: List[String])
}
