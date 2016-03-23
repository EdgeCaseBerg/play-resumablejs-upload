package controllers

import play.api._
import play.api.mvc._
import play.api.Logger

import service._
import form._

object FileUploadController extends Controller {

	val fileUploadService = new FileUploadService("tmp/")

	def index = Action { implicit request =>
		Ok(views.html.index())
	}

	def upload = Action {
		BadRequest("Ok")
	}

	def uploadTest = Action { implicit request =>
		Forms.fileUploadInfoForm.bindFromRequest.fold(
			formWithErrors => {
				BadRequest(formWithErrors.toString)
			},
			fileUploadInfo => {
				if (fileUploadService.isPartialUploadComplete(fileUploadInfo)) {
					Ok
				} else {
					NotFound
				}
			}
		)
	}

}