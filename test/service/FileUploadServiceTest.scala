package service

import org.scalatest._
import java.io.File
import java.nio.file.Files
import java.nio.charset.Charset
import java.nio.file.StandardOpenOption
import model._

object FileToFileUploadInfos {
	def apply(f: File): Seq[FileUploadInfo] = {
		val chunks = scala.collection.mutable.ListBuffer.empty[FileUploadInfo]
		val bytes = Files.readAllBytes(f.toPath())
		val chunkSize = 2
		var amountLeft = bytes.length
		var chunkNo = 1
		while (amountLeft > 0) {
			var currentChunkSize = Math.abs((bytes.length - (chunkNo * chunkSize)) % chunkSize)
			if (currentChunkSize == 0) currentChunkSize = chunkSize
			println(currentChunkSize)
			val fileInfo = FileUploadInfo(
				chunkNo,
				currentChunkSize,
				bytes.length,
				"tempXXX",
				f.getName()
			)
			chunkNo += 1
			amountLeft -= currentChunkSize
			chunks += fileInfo
		}
		chunks.toSeq
	}
}

class FileUploadServiceTest extends FlatSpec {

	def withFile(testCode: (File, FileUploadService) => Any) {
		val file = File.createTempFile("/tmp", ".tmpfile")
		val lines = (for (i <- 0 to 10) yield i).mkString
		Files.write(file.toPath, lines.getBytes, StandardOpenOption.WRITE);
		val fileUploadService = new FileUploadService("/tmp")
		try {
			testCode(file, fileUploadService)
		} finally file.delete()
	}

	"The FileUploadService" should "handle parts of a file" in withFile { (tmpFile, fileUploadService) =>
		val fileSegments = FileToFileUploadInfos(tmpFile)
		println(fileSegments)
		val allBytes = Files.readAllBytes(tmpFile.toPath())
		fileSegments.foreach { segment =>
			assertResult(false, "Should not show as complete before upload")(fileUploadService.isPartialUploadComplete(segment))
			val bytesToUpload = allBytes.drop((segment.resumableChunkNumber - 1) * segment.resumableChunkSize).take(segment.resumableChunkSize)
			fileUploadService.savePartialFile(bytesToUpload, segment)
			val partialRAF = Files.readAllBytes(new File(fileUploadService.fileNameFor(segment)).toPath())
			val uploadedBytes = partialRAF.drop((segment.resumableChunkNumber - 1) * segment.resumableChunkSize).take(segment.resumableChunkSize)
			assertResult(bytesToUpload)(uploadedBytes)
			assertResult(true)(fileUploadService.isPartialUploadComplete(segment))
			assertResult(segment.resumableChunkNumber == fileSegments.takeRight(1)(0).resumableChunkNumber)(fileUploadService.isUploadComplete(segment))
		}
		val rafBytes = Files.readAllBytes(new File(fileUploadService.fileNameFor(fileSegments(0))).toPath())
		assertResult(allBytes)(rafBytes)
	}
}
