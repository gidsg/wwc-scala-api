package com.womenwhocode.web

import org.http4s._
import org.http4s.dsl._

object Webapp {
  val service = HttpService {
    case GET -> Root =>
      Ok(s"Hello")
  }
}