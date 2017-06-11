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
      // TODO: redo step 1 without encoder, then with encoder
//      val uri = Uri.uri("http://api.postcodes.io/postcodes/") / postcode
//      val res = httpClient.expect(uri)(jsonOf[PostcodeResponse])
//      res.flatMap(postcodeResponse => Ok(postcodeResponse))

//      val res = httpClient.expect[PostcodeResponse](uri)
//      res.flatMap(postcodeResponse => Ok(postcodeResponse))

      val getRequestTask = get[PostcodeResponse](postcode)
      getRequestTask.flatMap(postcodeResponse => Ok(postcodeResponse))

    case GET -> Root / "locations" / postcode / "nearest" =>
      for {
        nearest <- get[MultiPostcodeResponse](s"$postcode/nearest")
        resp <- Ok(responseJson(nearest))
      } yield resp
  }
}