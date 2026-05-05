/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package models.sdes

import play.api.libs.json.*

enum Algorithm:
  case MD5, SHA1, SHA2, SHA256, SHA512

object Algorithm {

  def apply(algorithm: String): Algorithm = algorithm.toLowerCase match {
    case "md5"     => MD5
    case "sha1"    => SHA1
    case "sha2"    => SHA2
    case "sha-256" => SHA256
    case "sha-512" => SHA512
    case _         => throw new IllegalArgumentException(s"Unsupported algorithm $algorithm")
  }

  given Writes[Algorithm] = Writes[Algorithm] {
    case MD5    => JsString("md5")
    case SHA1   => JsString("SHA1")
    case SHA2   => JsString("SHA2")
    case SHA256 => JsString("SHA-256")
    case SHA512 => JsString("SHA-512")
  }

  given Reads[Algorithm] = Reads[Algorithm] {
    case JsString(s) =>
      s.toLowerCase match {
        case "md5"     => JsSuccess(MD5)
        case "sha1"    => JsSuccess(SHA1)
        case "sha2"    => JsSuccess(SHA2)
        case "sha-256" => JsSuccess(SHA256)
        case "sha-512" => JsSuccess(SHA512)
        case _         => JsError(s"Unexpected Algorithm: $s")
      }
    case other =>
      JsError(s"Expected JSON string for Algorithm, got ${other.getClass.getSimpleName}")
  }
}
