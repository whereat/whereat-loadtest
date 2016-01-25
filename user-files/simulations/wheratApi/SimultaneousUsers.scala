/**
  *
  * Copyright (c) 2015-present, Total Location Test Paragraph.
  * All rights reserved.
  *
  * This file is part of Where@. Where@ is free software:
  * you can redistribute it and/or modify it under the terms of
  * the GNU General Public License (GPL), either version 3
  * of the License, or (at your option) any later version.
  *
  * Where@ is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. For more details,
  * see the full license at <http://www.gnu.org/licenses/gpl-3.0.en.html>
  *
  */

package whereatApi

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import com.ning.http.client.{AsyncHttpClient, AsyncHttpClientConfig, Response, AsyncCompletionHandler}
import scala.concurrent.duration._

/**
 * usage: JAVA_OPTS="-Dusers=300" bin/gatling.sh
 * (change value of `users=...` to change num of simultaneous users
 */

class SimultaneousUsers extends Simulation {

  val baseUrl = "https://api-dev.whereat.io"
  val nbUsers = Integer.getInteger("users", 10)
  val httpConf = http
    .baseURL(baseUrl)
    .acceptHeader("application/json,text/html")
    .contentTypeHeader("application/json")

  val ids = Iterator.from(1).map(i ⇒ Map("userId" → i))

  val scn = scenario("5 Sec Interval")
    .feed(ids)
    .repeat(10, "n")(
      exec(http("Request ${n}")
        .post("/locations/update")
        .body(StringBody("""
          |{
          |  "lastPing": -1,
          |  "location": {
          |    "id": "${userId}",
          |    "lat": 40.7092529,
          |    "lon": -74.0112551,
          |    "time": 1505606400000
          |  }
          |}""".stripMargin)))
      .pause(5 seconds))

  setUp(scn.inject(atOnceUsers(nbUsers))).protocols(httpConf)

  after {
    val client = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().build())
    val request = client.preparePost("https://api-dev.whereat.io/locations/erase").build()
    client.executeRequest(request, new AsyncCompletionHandler[Response]() {
      override def onCompleted(response: Response) = {
        println(response.getResponseBody); response
      }
    })
  }
}


