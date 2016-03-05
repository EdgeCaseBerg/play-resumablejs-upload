package controllers

import play.api._
import play.api.mvc._
import play.api.Logger

import service._

object FileUploadController extends Controller {

	val fileUploadService = new FileUploadService("tmp/")

	def index = Action { implicit request =>
		Ok(views.html.index())
	}

	def upload = Action {
		BadRequest("Ok")
	}

	def uploadTest = Action {
		BadRequest("Ok")
	}

}