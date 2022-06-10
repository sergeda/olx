package domain

import zio.prelude.Subtype

object Telegram {
  object TelegramKey extends Subtype[String]
  type TelegramKey = TelegramKey.Type

  object TelegramGroup extends Subtype[String]
  type TelegramGroup = TelegramGroup.Type
}
