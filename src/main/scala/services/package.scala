import java.time.{LocalDateTime, OffsetDateTime}
import scala.util.Try

package object services {

  def getTime(string: String): Option[LocalDateTime] =
    Try(OffsetDateTime.parse(string)).toOption.map(_.toLocalDateTime)
}
