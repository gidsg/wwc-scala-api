package com.womenwhocode.web

import org.http4s._
import org.http4s.circe._
import org.http4s.client._
import org.http4s.client.blaze._
import org.http4s.dsl._
import io.circe.generic.auto._
import io.circe.syntax._

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

  implicit val postcodeMapEncoder = jsonEncoderOf[Map[String, List[(String, String)]]]
  implicit val multiPostcodeReqDecoder = jsonOf[Map[String, List[String]]]
  implicit val multiPostcodeReqEncoder = jsonEncoderOf[Map[String, List[String]]]
  implicit val ListEncoder = jsonEncoderOf[List[String]]
//  implicit val codeEncoder = jsonEncoderOf[CodeDetail]
//  implicit val codeDecoder = jsonOf[CodeDetail]

  implicit val bulkDecoder = jsonOf[BulkPostcodeResponse]
  implicit val bulkPostcodeMapEncoder = jsonEncoderOf[Map[String, List[(String, String, String, String)]]]

  def get[decoder: EntityDecoder](query: String) =
    httpClient.expect[decoder](s"http://api.postcodes.io/postcodes/$query")

  def responseJson(response: MultiPostcodeResponse) =
    Map("postcodes" -> response.result.map(postcodeRes => (postcodeRes.postcode, postcodeRes.region)))
//    Map("results" -> response.result).asJson //Answer to Exercise B

  def responseJson(resp: BulkPostcodeResponse) = {
    Map("postcodes" -> resp.result.map(res =>
      (res.result.postcode, res.result.region, res.result.codes.admin_district, res.result.codes.admin_county))).asJson
  }

  def findPostcodes(postcodes: List[String]) = {
    val target = uri("http://api.postcodes.io/postcodes")
    val body = Map("postcodes" -> postcodes).asJson
    val req = POST(target, body)
    httpClient.expect[BulkPostcodeResponse](req)
  }

  val service = HttpService {
    case GET -> Root =>
      Ok(s"Hello")

    case GET -> Root / "locations" =>
      Ok(s"locations endpoint")

    case GET -> Root / "locations" / postcode =>
      val uri = Uri.uri("http://api.postcodes.io/postcodes/") / postcode
      val res = httpClient.expect(uri)(jsonOf[PostcodeResponse])
      res.flatMap(postcodeResponse => Ok(postcodeResponse))

//      val res = httpClient.expect[PostcodeResponse](uri)
//      res.flatMap(postcodeResponse => Ok(postcodeResponse))

//      val getRequestTask = get[PostcodeResponse](postcode)
//      getRequestTask.flatMap(postcodeResponse => Ok(postcodeResponse))

    case GET -> Root / "locations" / postcode / "nearest" =>
      for {
        nearest <- get[MultiPostcodeResponse](s"$postcode/nearest")
        resp <- Ok(responseJson(nearest))
      } yield resp

    case req @ POST -> Root / "locations" / "bulk" =>
      val bulkRequest = req.as[Map[String, List[String]]]
      val bulkRequestTask = bulkRequest.map(postcodesMap => findPostcodes(postcodesMap.head._2))
      bulkRequestTask.flatMap(postcodes => postcodes.flatMap(resp => Ok(responseJson(resp))))

  }
}

