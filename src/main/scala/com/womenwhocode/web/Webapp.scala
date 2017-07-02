package com.womenwhocode.web

import org.http4s._
import org.http4s.dsl._
import org.http4s.client.blaze._
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import io.circe.generic.auto._
import io.circe._
import org.http4s.circe._
import io.circe.syntax._

import scalaz.concurrent.Task


object Webapp {
val httpClient = PooledHttp1Client()
  case class CodeDetail(admin_district: String, admin_county: String)
  case class PostcodeResult(postcode: String, country: String, region: String, codes: CodeDetail)
  case class PostcodeResponse(status: Int, result: PostcodeResult)
  case class MultiPostcodeResponse(status: Int, result: List[PostcodeResult])
  implicit val decoder = jsonOf[PostcodeResponse]
  implicit val encoder = jsonEncoderOf[PostcodeResponse]
  implicit val multiDecoder = jsonOf[MultiPostcodeResponse]
  implicit val multiEncoder = jsonEncoderOf[MultiPostcodeResponse]
  implicit val nearestEncoder = jsonEncoderOf[Map[String, List[(String, String)]]]
  implicit val multiPostcodeReqDecoder = jsonOf[Map[String, List[String]]]
  implicit val multiPostcodeReqEncoder = jsonEncoderOf[Map[String, List[String]]]

  def findPostcodes(postcodes: List[String]) = Ok(postcodes.asJson)



  def get[decoder: EntityDecoder](query: String) = httpClient.expect[decoder](s"http://api.postcodes.io/$query")
  val service = HttpService {
    case GET -> Root =>
      Ok(s"Hello")

    case GET -> Root / "locations" / postcode =>
      val getRequestTask = get[PostcodeResponse](s"postcodes/$postcode")
      getRequestTask.flatMap(postcodeResponse => Ok(postcodeResponse)
      )

    case GET -> Root / "random" =>
      val getRequestTask = get[PostcodeResponse]("random/postcodes")
      getRequestTask.flatMap(postcodeResponse => Ok(postcodeResponse)
      )

    case GET -> Root / "nearest" / postcode =>
      val getRequestTask = get[MultiPostcodeResponse](s"postcodes/$postcode/nearest")
      getRequestTask.flatMap(multiPostcodeResponse => Ok(Map("postcode" -> multiPostcodeResponse.result.map(r => (r.postcode, r.region))))
      )

    case req @ POST -> Root / "locations" / "bulk" =>
      val bulkRequest = req.as[Map[String, List[String]]]
      bulkRequest.flatMap (input => findPostcodes(input.head._2))
  }


}