package controllers

import java.io._

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.S3Object
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

object Application extends Controller {
  val AWS_ACCESS_KEY = "AKIAJMABWBEQQ3WRFCYA"
  val AWS_SECRET_KEY = "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"
  val bucketName = "lockscreens22"          // specifying bucket name

  def index = Action {
    Ok(views.html.index())
  }

  def latestVersion = Action {
    Ok(Json.obj("status" ->"OK", "version" -> "1"))
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
      amazonS3Client.putObject(bucketName, System.currentTimeMillis().toString, file)
      Ok("File uploaded")
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

    val reader = new BufferedInputStream(downloadedObject.getObjectContent)
    val file: File = new File("/tmp/" + timestamp.toString)
    val writer = new BufferedOutputStream(new FileOutputStream(file))

    Iterator
      .continually (reader.read)
      .takeWhile (-1 !=)
      .foreach (writer.write)

    Ok.sendFile(file)
  }

}
