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