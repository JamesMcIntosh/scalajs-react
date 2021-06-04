package downstream

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import scala.scalajs.js.annotation._

object Pumpkin {

  @JSExportTopLevel("PUMP_KIN")
  val Component = ScalaComponent.builder[String]
    .render { $ =>
      Globals.pumpkinRenders += 1
      <.div("Hello ", $.props)
    }
    .configure(Reusability.shouldComponentUpdate)
    .build
}
