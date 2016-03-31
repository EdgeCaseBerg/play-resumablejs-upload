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
		val lines = (for (i <- 0 to 1000) yield i).mkString
		Files.write(file.toPath, lines.getBytes, StandardOpenOption.WRITE);
		val fileUploadService = new FileUploadService("/tmp")
		try {
			testCode(file, fileUploadService)
		} finally file.delete()
	}

	"The FileUploadService" should "handle all parts of a file" in withFile { (tmpFile, fileUploadService) =>
		val fileSegments = FileToFileUploadInfos(tmpFile)
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

	it should "handle out of order file parts" in withFile { (tmpFile, fileUploadService) =>
		val fileSegments = FileToFileUploadInfos(tmpFile)
		val allBytes = Files.readAllBytes(tmpFile.toPath())
		val (evenSegments, oddSegments) = fileSegments.partition(_.resumableChunkNumber % 2 == 0)
		evenSegments.foreach { segment =>
			val bytesToUpload = allBytes.drop((segment.resumableChunkNumber - 1) * segment.resumableChunkSize).take(segment.resumableChunkSize)
			fileUploadService.savePartialFile(bytesToUpload, segment)
			val partialRAF = Files.readAllBytes(new File(fileUploadService.fileNameFor(segment)).toPath())
			val uploadedBytes = partialRAF.drop((segment.resumableChunkNumber - 1) * segment.resumableChunkSize).take(segment.resumableChunkSize)
			assertResult(bytesToUpload)(uploadedBytes)
			assertResult(true)(fileUploadService.isPartialUploadComplete(segment))
		}
		oddSegments.foreach { segment =>
			val bytesToUpload = allBytes.drop((segment.resumableChunkNumber - 1) * segment.resumableChunkSize).take(segment.resumableChunkSize)
			fileUploadService.savePartialFile(bytesToUpload, segment)
			val partialRAF = Files.readAllBytes(new File(fileUploadService.fileNameFor(segment)).toPath())
			val uploadedBytes = partialRAF.drop((segment.resumableChunkNumber - 1) * segment.resumableChunkSize).take(segment.resumableChunkSize)
			assertResult(bytesToUpload)(uploadedBytes)
			assertResult(true)(fileUploadService.isPartialUploadComplete(segment))
		}
		val rafBytes = Files.readAllBytes(new File(fileUploadService.fileNameFor(fileSegments(0))).toPath())
		assertResult(allBytes)(rafBytes)
	}

	it should "handle uploading the same part twice" in withFile { (tmpFile, fileUploadService) =>
		val fileSegments = FileToFileUploadInfos(tmpFile)
		val allBytes = Files.readAllBytes(tmpFile.toPath())
		val (evenSegments, oddSegments) = fileSegments.partition(_.resumableChunkNumber % 2 == 0)
		evenSegments.foreach { segment =>
			val bytesToUpload = allBytes.drop((segment.resumableChunkNumber - 1) * segment.resumableChunkSize).take(segment.resumableChunkSize)
			fileUploadService.savePartialFile(bytesToUpload, segment)
			val partialRAF = Files.readAllBytes(new File(fileUploadService.fileNameFor(segment)).toPath())
			val uploadedBytes = partialRAF.drop((segment.resumableChunkNumber - 1) * segment.resumableChunkSize).take(segment.resumableChunkSize)
			assertResult(bytesToUpload)(uploadedBytes)
			assertResult(true)(fileUploadService.isPartialUploadComplete(segment))
		}
		oddSegments.foreach { segment =>
			val bytesToUpload = allBytes.drop((segment.resumableChunkNumber - 1) * segment.resumableChunkSize).take(segment.resumableChunkSize)
			fileUploadService.savePartialFile(bytesToUpload, segment)
			val partialRAF = Files.readAllBytes(new File(fileUploadService.fileNameFor(segment)).toPath())
			val uploadedBytes = partialRAF.drop((segment.resumableChunkNumber - 1) * segment.resumableChunkSize).take(segment.resumableChunkSize)
			assertResult(bytesToUpload)(uploadedBytes)
			assertResult(true)(fileUploadService.isPartialUploadComplete(segment))
		}
		val rafBytes = Files.readAllBytes(new File(fileUploadService.fileNameFor(fileSegments(0))).toPath())
		assertResult(allBytes)(rafBytes)
		evenSegments.foreach { segment =>
			val bytesToUpload = allBytes.drop((segment.resumableChunkNumber - 1) * segment.resumableChunkSize).take(segment.resumableChunkSize)
			fileUploadService.savePartialFile(bytesToUpload, segment)
			val partialRAF = Files.readAllBytes(new File(fileUploadService.fileNameFor(segment)).toPath())
			val uploadedBytes = partialRAF.drop((segment.resumableChunkNumber - 1) * segment.resumableChunkSize).take(segment.resumableChunkSize)
			assertResult(bytesToUpload)(uploadedBytes)
			assertResult(true)(fileUploadService.isPartialUploadComplete(segment))
		}
		val rafBytes2 = Files.readAllBytes(new File(fileUploadService.fileNameFor(fileSegments(0))).toPath())
		assertResult(rafBytes)(rafBytes2)
	}

	it should "reject uploaded bytes with missing parts" in withFile { (tmpFile, fileUploadService) =>
		val fileSegments = FileToFileUploadInfos(tmpFile)
		val allBytes = Files.readAllBytes(tmpFile.toPath())
		fileSegments.foreach { segment =>
			assertResult(false, "Should not show as complete before upload")(fileUploadService.isPartialUploadComplete(segment))
			val bytesToUpload = allBytes.drop((segment.resumableChunkNumber - 1) * segment.resumableChunkSize).take(segment.resumableChunkSize)
			fileUploadService.savePartialFile(bytesToUpload.drop(1), segment)
			assertResult(false, "Should not show as complete before upload")(fileUploadService.isPartialUploadComplete(segment))
		}
		assertResult(false)(new File(fileUploadService.fileNameFor(fileSegments(0))).exists())
	}
}
