package com.womenwhocode.web

import org.http4s._
import org.http4s.circe._
import org.http4s.client.blaze._
import org.http4s.dsl._
import io.circe.generic.auto._
import io.circe.syntax._

object Webapp {
  val httpClient = PooledHttp1Client()

  case class PostcodeResult(postcode: String, region: String, country: String, codes: CodeDetail)
  case class PostcodeResponse(status: Int, result: PostcodeResult)
  case class CodeDetail(admin_district: String, admin_county: String)
  case class MultiPostcodeResponse(status: Int, result: List[PostcodeResult])

  implicit val decoder = jsonOf[PostcodeResponse]
  implicit val encoder = jsonEncoderOf[PostcodeResponse]
  implicit val multiDecoder = jsonOf[MultiPostcodeResponse]
  implicit val multiEncoder = jsonEncoderOf[MultiPostcodeResponse]
  implicit val postcodeMapEncoder = jsonEncoderOf[Map[String, List[(String, String)]]]

  def get[decoder: EntityDecoder](query: String) =
    httpClient.expect[decoder](s"http://api.postcodes.io/postcodes/$query")

  def responseJson(response: MultiPostcodeResponse) =
    Map("postcodes" -> response.result.map(postcodeRes => (postcodeRes.postcode, postcodeRes.region)))

  val service = HttpService {
    case GET -> Root =>
      Ok(s"Hello")

    case GET -> Root / "locations" =>
      Ok(s"locations endpoint")

    case GET -> Root / "locations" / postcode =>
      val getRequestTask = get[PostcodeResponse](postcode)
      getRequestTask.flatMap(postcodeResponse => Ok(postcodeResponse))

    case GET -> Root / "locations" / postcode / "nearest" =>
        val getRequestTask = get[MultiPostcodeResponse](s"$postcode/nearest")
        getRequestTask.flatMap(response => Ok(responseJson(response)))

  }
}