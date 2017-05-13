package com.womenwhocode.web

import org.http4s._
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import io.circe.generic.auto._
import org.http4s.client.blaze.PooledHttp1Client
import org.http4s.dsl._

import org.json4s.native.Serialization


object Webapp {

  val httpClient = PooledHttp1Client()

  case class PostcodeResult(postcode: String, country: String, region: String, codes: CodeDetail)
  case class PostcodeResponse(status: Int, result: PostcodeResult)
  case class CodeDetail(admin_district: String, admin_county: String)
  case class MultiPostcodeResponse(status: Int, result: List[PostcodeResult])

  implicit val formats = org.json4s.DefaultFormats

  implicit val decoder = jsonOf[PostcodeResponse]
  implicit val encoder = jsonEncoderOf[PostcodeResponse]
  implicit val multiDecoder = jsonOf[MultiPostcodeResponse]
  implicit val multiEncoder = jsonEncoderOf[MultiPostcodeResponse]

  val service = HttpService {
    case GET -> Root =>
      Ok(s"Hello")

    case GET -> Root / "locations" =>
      Ok(s"locations endpoint")

    case GET -> Root / "locations" / postcode =>
      val getRequestTask = httpClient.expect[PostcodeResponse](s"http://api.postcodes.io/postcodes/${postcode}")
      getRequestTask.flatMap(json => Ok(json))

    case GET -> Root / "locations" / postcode / "nearest" =>
      def responseJson(response: MultiPostcodeResponse) = {
        Serialization.write(Map("postcodes" -> response.result.map(postcodeRes => postcodeRes.postcode)))
      }

      val getRequestTask = httpClient.expect[MultiPostcodeResponse](s"http://api.postcodes.io/postcodes/${postcode}/nearest")
      getRequestTask.flatMap(response => Ok(responseJson(response)))
  }
}