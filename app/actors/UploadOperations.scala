package actors

import model._

sealed trait UploadOperations

case class AddFilePart(bytes: Array[Byte], fileInfo: FileUploadInfo) extends UploadOperations

case class CheckFilePart(fileInfo: FileUploadInfo) extends UploadOperations

case class CheckIsComplete(fileInfo: FileUploadInfo) extends UploadOperations