package com.womenwhocode.web

import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.blaze.PooledHttp1Client
import org.http4s.dsl._

import scalaz.concurrent.Task

object Webapp {
  val httpClient = PooledHttp1Client()

  case class PostcodeResult(postcode: String, country: String, region: String, codes: CodeDetail)
  case class PostcodeResponse(result: PostcodeResult)
  case class CodeDetail(admin_district: String, admin_county: String)
  case class MultiPostcodeResponse(status: Int, result: List[PostcodeResult])
  case class BulkPostcodeResponse(status: Int, result: List[PostcodeResponse])

  implicit val decoder = jsonOf[PostcodeResponse]
  implicit val encoder = jsonEncoderOf[PostcodeResponse]
  implicit val multiDecoder = jsonOf[MultiPostcodeResponse]
  implicit val multiEncoder = jsonEncoderOf[MultiPostcodeResponse]
  implicit val bulkDecoder = jsonOf[BulkPostcodeResponse]
  //implicit val postcodeMapEncoder = jsonEncoderOf[Map[String, List[String]]] //Solution: to using an encoder instead of Serialization directly
  implicit val postcodeMapEncoder = jsonEncoderOf[Map[String, List[(String, String)]]]
  implicit val multiPostcodeReqDecoder = jsonOf[Map[String, List[String]]]
  implicit val multiPostcodeReqEncoder = jsonEncoderOf[Map[String, List[String]]]
  implicit val ListEncoder = jsonEncoderOf[List[String]]

  def responseJson(response: MultiPostcodeResponse) =
    Map("postcodes" -> response.result.map(postcodeRes => (postcodeRes.postcode, postcodeRes.region)))

  def responseJson(response: BulkPostcodeResponse) =
    Map("postcodes" -> response.result.map(postcodeRes => (postcodeRes.result.postcode, postcodeRes.result.region)))

  def get[A: EntityDecoder](query: String) =
    httpClient.expect[A](s"http://api.postcodes.io/postcodes/$query")


  def findPostcodes(postcodes: List[String]) = {
    import org.http4s.client._
    val req = POST(uri("http://api.postcodes.io/postcodes"), Map("postcodes" -> postcodes).asJson)
    httpClient.expect[BulkPostcodeResponse](req)
  }

  val service = HttpService {

    case GET -> Root =>
      Ok(s"Hello")

    case GET -> Root / "locations" =>
      Ok(s"locations endpoint")

    case GET -> Root / "locations" / postcode =>
      Ok(get[PostcodeResponse](postcode))

    case GET -> Root / "locations" / postcode / "nearest" =>
      for {
        nearest <- get[MultiPostcodeResponse](s"$postcode/nearest")
        resp <- Ok(responseJson(nearest))
      } yield resp

    case req @ POST -> Root / "locations" / "bulk" =>
      for {
        json <- req.as[Map[String, List[String]]]
        postcodes <- findPostcodes(json.head._2)
        resp <- Ok(responseJson(postcodes))
      } yield resp

  }
}