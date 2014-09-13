package controllers

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import play.api.mvc.{Controller, Action}
import awscala._, s3._
import iam._

object Application extends Controller {
  implicit val s3 = S3()
  implicit val iam = IAM()
  val AWS_ACCESS_KEY = "AKIAI7PGQWZSCORB66SQ"
  val AWS_SECRET_KEY = "Zm4oHZgN47eFJCMwPUUZq/hZZBY5CavWlErxE03b"
  val bucketName = "neelkanthbucket"          // specifying bucket name

  def index = Action {
    val yourAWSCredentials = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY)
    val amazonS3Client = new AmazonS3Client(yourAWSCredentials)
    amazonS3Client.createBucket(bucketName)


    Ok(views.html.index("Your new application is ready."))
  }

}
