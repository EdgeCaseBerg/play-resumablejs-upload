package controllers

import play.api._
import play.api.mvc._
import play.api.Logger

import service._
import form._

import java.io.File
import java.nio.file.Files

object FileUploadController extends Controller {

	val fileUploadService = new FileUploadService("tmp/", SingleInstanceFilePartSaver)

	def index = Action { implicit request =>
		Ok(views.html.index())
	}

	def upload = Action(parse.multipartFormData) { implicit request =>
		Forms.fileUploadInfoForm.bindFromRequest.fold(
			formWithErrors => {
				BadRequest(formWithErrors.errors.mkString("\n"))
			},
			fileUploadInfo => {
				request.body.file("file") match {
					case None => BadRequest("No file")
					case Some(file) =>
						val bytes = Files.readAllBytes(file.ref.file.toPath())
						fileUploadService.savePartialFile(bytes, fileUploadInfo)
						file.ref.clean()
						Ok
				}
			}
		)
	}

	def uploadTest = Action { implicit request =>
		Forms.fileUploadInfoForm.bindFromRequest.fold(
			formWithErrors => {
				BadRequest(formWithErrors.errors.mkString("\n"))
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