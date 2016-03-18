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
		fileSegments.foreach { segment =>
			assertResult(false, "Should not show as complete before upload")(fileUploadService.isPartialUploadComplete(segment))
		}
		//Write test along the lines of: call savePartialFile for each segment, saving down the byte array for that chunk and then checking that the service tracks it correctly and the RAF file itself has the bytes put into place
		fail("not implemented")
	}
}
