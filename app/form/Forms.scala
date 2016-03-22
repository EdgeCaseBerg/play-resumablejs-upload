package form

import play.api.data._
import play.api.data.Forms._

import model._

object Forms {
	def fileUploadInfoForm = Form(
		mapping(
			"resumableChunkNumber" -> number,
			"resumableChunkSize" -> number,
			"resumableTotalSize" -> number,
			"resumableIdentifier" -> nonEmptyText,
			"resumableFilename" -> nonEmptyText
		)(FileUploadInfo.apply)(FileUploadInfo.unapply)
	)
}