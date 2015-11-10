package whereatApi

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import com.ning.http.client.{AsyncHttpClient, AsyncHttpClientConfig, Response, AsyncCompletionHandler}
import scala.concurrent.duration._

/**
 * Written with love for where@
 * License: GPLv3 (https://www.gnu.org/licenses/gpl-3.0.html)
 */

// usage: JAVA_OPTS="-Dusers=300" /bin/gatling.sh
// (change value of `users=...` to change num of simultaneous users

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


