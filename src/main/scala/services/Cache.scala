package services

import services.Cache.CacheError
import zio.cache.{Lookup, Cache as ZCache}
import zio.*

import scala.concurrent.duration.{Duration as _, *}

trait Cache {
  def put(key: String): IO[CacheError, String]
  def contains(key: String): UIO[Boolean]
}

case class CacheLive(underlying: ZCache[String, CacheError, String])
    extends Cache {
  def put(key: String): IO[CacheError, String] = underlying.get(key)
  def contains(key: String): UIO[Boolean]      = underlying.contains(key)
}

object Cache {
  sealed trait CacheError
  private val cacheLayer: URLayer[ZCache[String, CacheError, String], Cache] =
    ZLayer.fromFunction[ZCache[String, CacheError, String], Cache](
      CacheLive.apply
    )

  private val lookup: String => ZIO[Any, CacheError, String] = (key: String) =>
    ZIO.succeed(key)
  def layer(duration: Duration): ZLayer[Any, Nothing, Cache] = ZLayer(
    ZCache.make[String, Any, CacheError, String](
      1000000,
      duration,
      Lookup(lookup)
    )
  ) >>> cacheLayer
}
