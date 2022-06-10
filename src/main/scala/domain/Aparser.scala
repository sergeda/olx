package domain

import config.AparserConfig.JobConfig
import io.circe.{Encoder, Json}
import zio.prelude.Subtype

import java.time.LocalDateTime

object Aparser {

  object AparserUrl extends Subtype[String]

  type AparserUrl = AparserUrl.Type

  object AparserPassword extends Subtype[String]

  type AparserPassword = AparserPassword.Type

  object JobPreset extends Subtype[String]

  type JobPreset = JobPreset.Type

  object JobConfigPreset extends Subtype[String]

  type JobConfigPreset = JobConfigPreset.Type

  object JobParser extends Subtype[String]

  type JobParser = JobParser.Type

  object JobQuery extends Subtype[String]

  type JobQuery = JobQuery.Type

  object RequestType extends Subtype[String]

  type RequestType = RequestType.Type

  case class AparserRequest(
      password: AparserPassword,
      action: RequestType,
      data: JobConfig
  )

  object AparserRequest {
    implicit val requestEncoder: Encoder[AparserRequest] =
      (a: AparserRequest) =>
        Json.obj(
          ("password", Json.fromString(a.password)),
          ("action", Json.fromString(a.action)),
          ("data", implicitly[Encoder[JobConfig]].apply(a.data))
        )
  }

  object AdUrl extends Subtype[String]

  type AdUrl = AdUrl.Type

  object AdId extends Subtype[String]

  type AdId = AdId.Type

  object AdTitle extends Subtype[String]

  type AdTitle = AdTitle.Type

  object AdPhoto extends Subtype[String]

  type AdPhoto = AdPhoto.Type

  object AdOwner extends Subtype[String]

  type AdOwner = AdOwner.Type

  object AdOwnerPhone extends Subtype[String]

  type AdOwnerPhone = AdOwnerPhone.Type

  object AdLocation extends Subtype[String]

  type AdLocation = AdLocation.Type

  object AdCost extends Subtype[String]

  type AdCost = AdCost.Type

  object AdCurrency extends Subtype[String]

  type AdCurrency = AdCurrency.Type

  object AdOwnerRegistered extends Subtype[LocalDateTime]

  type AdOwnerRegistered = AdOwnerRegistered.Type

  object AdPublished extends Subtype[LocalDateTime]

  type AdPublished = AdPublished.Type

  case class Ad(adUrl: AdUrl, adId: AdId)

  case class AdDetail(
      adUrl: AdUrl,
      adId: AdId,
      adPublished: AdPublished,
      adOwnerRegistered: AdOwnerRegistered,
      adOwner: AdOwner,
      adCost: AdCost,
      adCurrency: AdCurrency,
      adPhoto: AdPhoto,
      adOwnerPhone: AdOwnerPhone,
      adLocation: AdLocation,
      adTitle: AdTitle
  )

  object AparserResponse extends Subtype[String]

  type AparserResponse = AparserResponse.Type

  object Line extends Subtype[String]

  type Line = Line.Type
}
