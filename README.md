### Project made mostly to play with Scala 3, ZIO 2, ZIO Prelude
Service periodically checks specified [Olx](https://ru.wikipedia.org/wiki/OLX) category links and in case there are new ads - parses them and sends details to Telegram.

#### How to run
+ Fill in all the information in application.conf
+ Set TELEGRAM_API_KEY, APARSER_URL, APARSER_PASSWORD environment variables
+ Run it with `sbt-run`