package com.example

import com.example.Domain.User

import spray.json.DefaultJsonProtocol

object JsonFormats  {
  import DefaultJsonProtocol._

  implicit val userJsonFormat = jsonFormat2(User)

}