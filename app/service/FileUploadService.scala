package service

import java.io.{ File, RandomAccessFile }
import model._

import java.util.concurrent.{ ConcurrentMap, ConcurrentHashMap }
import scala.collection.JavaConversions._

class FileUploadService(serviceSavePath: String, filePartSave: FilePartSaver) {

	val basePath = if (serviceSavePath.endsWith("/")) {
		serviceSavePath
	} else { serviceSavePath + "/" }

	def fileNameFor(fileInfo: FileUploadInfo) = {
		s"${basePath}${fileInfo.resumableIdentifier}-${fileInfo.resumableFilename}"
	}

	def savePartialFile(filePart: Array[Byte], fileInfo: FileUploadInfo) {
		if (filePart.length != fileInfo.resumableChunkSize) {
			return
		}
		val partialFile = new RandomAccessFile(fileNameFor(fileInfo), "rw")
		val offset = (fileInfo.resumableChunkNumber - 1) * fileInfo.resumableChunkSize

		try {
			partialFile.seek(offset)
			partialFile.write(filePart, 0, filePart.length)
		} finally {
			partialFile.close()
		}

		val key = fileNameFor(fileInfo)
		if (filePartSave.contains(key)) {
			val partsUploaded = filePartSave.get(key)
			filePartSave.put(key, partsUploaded + FilePart(filePart, fileInfo))
		} else {
			filePartSave.put(key, Set(FilePart(filePart, fileInfo)))
		}
	}

	def isPartialUploadComplete(fileInfo: FileUploadInfo): Boolean = {
		/* Todo: Should we open the RAF and check it? */
		val key = fileNameFor(fileInfo)
		filePartSave.contains(key) && filePartSave.get(key).contains(fileInfo)
	}

	def isUploadComplete(fileInfo: FileUploadInfo): Boolean = {
		val key = fileNameFor(fileInfo)
		val possibleFinishedFile = new RandomAccessFile(key, "r")
		val fileLength = possibleFinishedFile.length()
		fileLength == fileInfo.resumableTotalSize && filePartSave.contains(key) && filePartSave.get(key).size == fileInfo.totalChunks.toInt
	}

}