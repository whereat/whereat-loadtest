package whereat_api

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * Written with love for where@
 * License: GPLv3 (https://www.gnu.org/licenses/gpl-3.0.html)
 */


// usage: /bin/gatling.sh JAVA_OPTS

class Base extends Simulation {

  val nbUsers = Integer.getInteger("users", 10)

  val httpConf = http
    .baseURL("https://api-dev.whereat.io")
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
//  .exec(http("Cleanup").post("/locations/erase"))

  setUp(scn.inject(atOnceUsers(nbUsers))).protocols(httpConf)

}
