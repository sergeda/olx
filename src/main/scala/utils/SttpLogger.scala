package utils

import sttp.client3.logging.{LogLevel, Logger}
import zio.{Task, ZIO}
import zio.logging.*

class SttpLogger extends Logger[Task] {
  def apply(level: LogLevel, message: => String) = level match {
    case LogLevel.Trace => ZIO.logTrace(message)
    case LogLevel.Debug => ZIO.logDebug(message)
    case LogLevel.Info  => ZIO.logInfo(message)
    case LogLevel.Warn  => ZIO.logWarning(message)
    case LogLevel.Error => ZIO.logError(message)
  }
  def apply(level: LogLevel, message: => String, t: Throwable) = level match {
    case LogLevel.Trace => ZIO.logTrace(message)
    case LogLevel.Debug => ZIO.logDebug(message)
    case LogLevel.Info  => ZIO.logInfo(message)
    case LogLevel.Warn  => ZIO.logWarning(message)
    case LogLevel.Error => ZIO.logError(message)
  }
}
