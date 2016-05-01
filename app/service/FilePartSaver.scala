package service

import model._

case class FilePart(data: Array[Byte], fileInfo: FileUploadInfo)

trait FilePartSaver {
	def get(key: String): Option[FilePart]

	def contains(key: String): Boolean

	def put(key: String, filePart: FilePart): Boolean
}