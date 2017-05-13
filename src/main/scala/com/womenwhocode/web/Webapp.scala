package com.womenwhocode.web

import org.http4s._
import org.http4s.circe.{jsonOf, jsonEncoderOf}
import io.circe.generic.auto._
import org.http4s.client.blaze.PooledHttp1Client
import org.http4s.dsl._

object Webapp {

  val httpClient = PooledHttp1Client()

  case class PostcodeResult(postcode: String, country: String, region: String, codes: CodeDetail)
  case class PostcodeResponse(status: Int, result: PostcodeResult)
  case class CodeDetail(admin_district: String, admin_county: String)

  implicit val decoder = jsonOf[PostcodeResponse]
  implicit val encoder = jsonEncoderOf[PostcodeResponse]

  val service = HttpService {
    case GET -> Root =>
      Ok(s"Hello")

    case GET -> Root / "locations" =>
      Ok(s"locations endpoint")

    case GET -> Root / "locations" / postcode =>
      val getRequestTask = httpClient.expect[PostcodeResponse](s"http://api.postcodes.io/postcodes/${postcode}")
      getRequestTask.flatMap(json => Ok(json))
  }
}