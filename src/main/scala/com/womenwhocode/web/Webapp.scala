package com.womenwhocode.web

import org.http4s._
import org.http4s.dsl._

object Webapp {
  val service = HttpService {
    case GET -> Root =>
      Ok(s"Hello")

    case GET -> Root / "locations" =>
      Ok(s"locations endpoint")

    case GET -> Root / "locations" / postcode =>
      Ok(s"locations endpoint ${postcode}")
  }
}