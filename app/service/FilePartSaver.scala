package service

import model._

case class FilePart(data: Array[Byte], fileInfo: FileUploadInfo)

trait FilePartSaver {
	def get(key: String): Set[FilePart]

	def contains(key: String): Boolean

	def put(key: String, fileParts: Set[FilePart]): Boolean
}

import java.util.concurrent.{ ConcurrentMap, ConcurrentHashMap }

object SingleInstanceFilePartSaver extends FilePartSaver {
	val uploadedParts: ConcurrentMap[String, Set[FilePart]] = new ConcurrentHashMap(8, 0.9f, 1)

	def get(key: String) = {
		if (uploadedParts.containsKey(key)) {
			uploadedParts.get(key)
		} else {
			Set.empty[FilePart]
		}
	}

	def contains(key: String) = uploadedParts.containsKey(key)

	def put(key: String, fileParts: Set[FilePart]) = {
		uploadedParts.put(key, fileParts) //Because this is backed by a single instance we don't store the byte array here
		uploadedParts.containsKey(key)
	}
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._

object MemcacheBackedFilePartSaver extends FilePartSaver {
	import shade.memcached._
	import java.io._
	import scala.reflect.ClassTag
	import scala.util.control.NonFatal

	lazy val memcached = Memcached(Configuration("127.0.0.1:11211")) //todo make configurable

	implicit object UserCodec extends Codec[Set[FilePart]] {
		def using[T <: Closeable, R](obj: T)(f: T => R): R =
			try
				f(obj)
			finally
				try obj.close() catch {
					case NonFatal(_) => // does nothing
				}

		def serialize(value: Set[FilePart]): Array[Byte] =
			using(new ByteArrayOutputStream()) { buf =>
				using(new ObjectOutputStream(buf)) { out =>
					out.writeObject(value)
					out.close()
					buf.toByteArray
				}
			}

		def deserialize(data: Array[Byte]): Set[FilePart] =
			using(new ByteArrayInputStream(data)) { buf =>
				val in = new GenericCodecObjectInputStream(ClassTag(classOf[Set[FilePart]]), buf)
				using(in) { inp =>
					inp.readObject().asInstanceOf[Set[FilePart]]
				}
			}
	}

	def get(key: String) = {
		memcached.awaitGet[Set[FilePart]](key) match {
			case parts: Some[Set[FilePart]] => parts.get
			case None => Set.empty[FilePart]
		}
	}

	def contains(key: String) = memcached.awaitGet[Set[FilePart]](key).isDefined

	def put(key: String, fileParts: Set[FilePart]) = {
		val parts = memcached.transformAndGet[Set[FilePart]](key, 1.minute) {
			case existing: Some[Set[FilePart]] => existing.get ++ fileParts
			case None => fileParts
		}
		Await.result(parts, Duration.Inf)
		contains(key)
	}

}