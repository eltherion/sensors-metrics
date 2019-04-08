package com.datart.sensors.metrics

import better.files._
import org.scalatest._

class AppIntegrationTest extends AsyncWordSpec with Matchers {

  "A Main" can {

    "execute for given args" should {

      "run correctly for args indicating correct directory" in {

        Main.main(Array(Resource.getUrl("valid_inputs/csv").getPath)) shouldBe (())
      }

      "fail for invalid args" in {
        an[RuntimeException] shouldBe thrownBy(Main.main(Array.empty))
      }
    }
  }
}
