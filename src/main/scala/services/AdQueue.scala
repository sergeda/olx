package services

import zio._
import domain.Aparser.*

trait AdQueue {
  def take: UIO[Ad]
  def offer(ad: Ad): UIO[Boolean]
}
case class AdQueueLive(queue: Queue[Ad]) extends AdQueue {
  def take: UIO[Ad]               = queue.take
  def offer(ad: Ad): UIO[Boolean] = queue.offer(ad)
}

object AdQueue {
  val layer: URLayer[Queue[Ad], AdQueue] =
    ZLayer.fromFunction[Queue[Ad], AdQueue](AdQueueLive.apply)
}
