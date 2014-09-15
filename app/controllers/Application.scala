package controllers

import anorm._
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.util.IOUtils
import play.api.Play.current
import play.api.db._
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

object Application extends Controller {
  private val AWS_ACCESS_KEY = "AKIAJMABWBEQQ3WRFCYA"
  private val AWS_SECRET_KEY = "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"
  private val bucketName = "lockscreens"
  private val latestVersionFile = "latestVersion"

  def index = Action {
    Ok(views.html.index())
  }

  def latestVersion = Action {
    DB.withConnection { implicit connection =>
      val result = SQL("Select max(id) as latest FROM LatestVersion").apply().head
      Ok(Json.obj("status" -> "OK", "version" -> result[Long]("latest")))
    }
  }

  def upload = Action(parse.multipartFormData) { request =>
    request.body.file("picture").map { picture =>
      import java.io.File
      val filename = picture.filename
      val contentType = picture.contentType
      val file = new File("/tmp/picture")
      picture.ref.moveTo(file, true)
      val currentTime = System.currentTimeMillis()
      getAmazonClient.putObject(bucketName, currentTime.toString, file)
      DB.withConnection { implicit c =>
        val id: Int = SQL("insert into LatestVersion(id) values ({id})")
          .on('id -> currentTime.toLong).executeUpdate()
      }
      Ok("File uploaded: " + currentTime.toString)
    }.getOrElse {
      Redirect(routes.Application.index).flashing(
        "error" -> "Missing file"
      )
    }
  }

  def download(timestamp: Long) = Action {
    val downloadedObject = getAmazonClient.getObject(bucketName, timestamp.toString).getObjectContent
    Ok(IOUtils.toByteArray(downloadedObject))
      .withHeaders(CONTENT_TYPE -> "image/jpeg")
  }

  def getAmazonClient = {
    val yourAWSCredentials = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY)
    new AmazonS3Client(yourAWSCredentials)
  }
}
