package service

import model._

case class FilePart(data: Array[Byte], fileInfo: FileUploadInfo)

trait FilePartSaver {
	def get(key: String): Option[FilePart]

	def contains(key: String): Boolean

	def put(key: String, filePart: FilePart): Boolean
}

object SingleInstanceFilePartSaver extends FilePartSaver {
	val uploadedParts: ConcurrentMap[String, Set[FileUploadInfo]] = new ConcurrentHashMap(8, 0.9f, 1)

	def get(key: String) = {
		if (uploadedParts.containsKey(key)) {
			Some(uploadedParts.get(key))
		} else {
			None
		}
	}

	def contains(key: String) = uploadedParts.contains(key)

	def put(key: String, filePart: FilePart) = {
		uploadedParts.put(key, filePart.fileInfo) //Because this is backed by a single instance we don't store the byte array here
		uploadedParts.containsKey(key)
	}
}