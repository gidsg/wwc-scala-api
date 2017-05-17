package com.womenwhocode.web

import org.http4s._
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.headers.`Content-Type`
import io.circe.generic.auto._
import org.http4s.client.blaze.PooledHttp1Client
import org.http4s.dsl._

import scalaz.concurrent.Task

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
  //implicit val postcodeMapEncoder = jsonEncoderOf[Map[String, List[String]]] //Solution: to using an encoder instead of Serialization directly
  implicit val postcodeMapEncoder = jsonEncoderOf[Map[String, List[(String, String)]]]
  implicit val multiPostcodeReqDecoder = jsonOf[Map[String, List[String]]]
  implicit val multiPostcodeReqEncoder = jsonEncoderOf[Map[String, List[String]]]
  implicit val ListEncoder = jsonEncoderOf[List[String]]

  def responseJson(response: MultiPostcodeResponse) = {
    Map("postcodes" -> response.result.map(postcodeRes => (postcodeRes.postcode, postcodeRes.region)))
  }

  val service = HttpService {

    case GET -> Root =>
      Ok(s"Hello")

    case GET -> Root / "locations" =>
      Ok(s"locations endpoint")

    case GET -> Root / "locations" / postcode =>
      val getRequestTask = httpClient.expect[PostcodeResponse](s"http://api.postcodes.io/postcodes/${postcode}")
      getRequestTask.flatMap(json => Ok(json))

    case GET -> Root / "locations" / postcode / "nearest" =>
      val getRequestTask = httpClient.expect[MultiPostcodeResponse](s"http://api.postcodes.io/postcodes/${postcode}/nearest")
      getRequestTask.flatMap(response => Ok(responseJson(response)))

      //TODO: 1. Fix decoder issue with response, 2. don't use unsafePerformSync
    case req @ POST -> Root / "locations" / "bulk" =>
      def findNearestPostcodes(postcodes: List[String]) = {
        val body = Map("postcodes" -> postcodes)
        val headers = Headers(`Content-Type`(MediaType.`application/json`, Charset.`UTF-8`))
        val request = Request(method = Method.POST, uri = Uri.uri("http://api.postcodes.io/postcodes"), headers = headers)
          .withBody(body)
          .asInstanceOf[Task[Request]]
        val postRequestTask = httpClient.expect[MultiPostcodeResponse](request)
        postRequestTask.flatMap(response => Ok(responseJson(response)))
      }

      findNearestPostcodes(req.as[Map[String, List[String]]].unsafePerformSync.head._2)
  }
}