package controllers

import java.io._

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.util.IOUtils
import play.api.libs.json.Json
import play.api.mvc.{ResponseHeader, SimpleResult, Action, Controller}
import play.api.db._
import anorm._
import play.api.Play.current

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
      val yourAWSCredentials = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY)
      val amazonS3Client = new AmazonS3Client(yourAWSCredentials)
      val currentTime = System.currentTimeMillis()
      amazonS3Client.putObject(bucketName, currentTime.toString, file)
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
    val yourAWSCredentials = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY)
    val amazonS3Client = new AmazonS3Client(yourAWSCredentials)
    val downloadedObject: S3Object = amazonS3Client.getObject(bucketName, timestamp.toString)

    val content = downloadedObject.getObjectContent

    Ok(IOUtils.toByteArray(content))
      .withHeaders(CONTENT_TYPE -> "image/jpeg")
  }

}
