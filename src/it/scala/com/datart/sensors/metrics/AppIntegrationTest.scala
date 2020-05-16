package com.datart.sensors.metrics

import better.files._
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatest.matchers.should.Matchers

class AppIntegrationTest extends AsyncWordSpec with Matchers {

  "A Main" can {

    "execute for given args" should {

      "run correctly Monix `Task` implementation for args indicating correct directory" in {

        MainMonixTaskImpl.main(Array(Resource.getUrl("valid_inputs/csv").getPath)) shouldBe (())
      }

      "fail Monix `Task` implementation for invalid args" in {
        an[RuntimeException] shouldBe thrownBy(MainMonixTaskImpl.main(Array.empty))
      }

      "run correctly Cats Effect `IO` implementation for args indicating correct directory" in {

        MainCatsEffectIOImpl.main(Array(Resource.getUrl("valid_inputs/csv").getPath)) shouldBe (())
      }

      "fail Cats Effect `IO` implementation for invalid args" in {
        an[RuntimeException] shouldBe thrownBy(MainCatsEffectIOImpl.main(Array.empty))
      }

      "run correctly `Future` implementation for args indicating correct directory" in {

        MainFutureImpl.main(Array(Resource.getUrl("valid_inputs/csv").getPath)) shouldBe (())
      }

      "fail `Future` implementation for invalid args" in {
        an[RuntimeException] shouldBe thrownBy(MainFutureImpl.main(Array.empty))
      }

      "run correctly ZIO `Task` implementation for args indicating correct directory" in {

        MainZIOTaskImpl.main(Array(Resource.getUrl("valid_inputs/csv").getPath)) shouldBe (())
      }

      "fail ZIO `Task` implementation for invalid args" in {
        an[RuntimeException] shouldBe thrownBy(MainZIOTaskImpl.main(Array.empty))
      }
    }
  }
}
