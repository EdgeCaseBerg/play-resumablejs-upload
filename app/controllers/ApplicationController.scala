package controllers

import play.api._
import play.api.mvc._
import play.api.Logger

object FileUploadController extends Controller {

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