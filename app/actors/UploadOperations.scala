package actors

import model._

sealed trait UploadOperations

case class AddFilePart(val bytes: Array[Byte], val fileInfo: FileUploadInfo) extends UploadOperations

case class CheckFilePart(val fileInfo: FileUploadInfo) extends UploadOperations

case class CheckIsComplete(val fileInfo: FileUploadInfo) extends UploadOperations

sealed trait UploadResponses