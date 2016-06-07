package actors

import model._
sealed trait UploadResponses

case class FilePartAdded() extends UploadResponses

case class FilePartExists() extends UploadResponses

case class FileComplete() extends UploadResponses