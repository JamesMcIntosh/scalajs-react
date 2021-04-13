package japgolly.scalajs.react

import japgolly.scalajs.react.internal.NewMacroUtils
import japgolly.scalajs.react.internal.NewMacroUtils.{CompileTimeConfig, CompileTimeString}

trait ScalaJsReactConfig {
  def automaticComponentName(name: String): String
  def modifyComponentName(name: => String): String
  def reusabilityOverride: ScalaJsReactConfig.ReusabilityOverride | Null
}

object ScalaJsReactConfig {

  trait ReusabilityOverride {
    def apply[P: Reusability, C <: Children, S: Reusability, B, U <: UpdateSnapshot]: ScalaComponent.Config[P, C, S, B, U, U]
  }

  object ReusabilityOverride {
    def identity: ReusabilityOverride = new ReusabilityOverride {
      override def apply[P: Reusability, C <: Children, S: Reusability, B, U <: UpdateSnapshot] =
        internal.identityFn
    }
  }

  private inline val KeyConfigClass  = "japgolly.scalajs.react.config.class"
  private inline def KeyCompNameAuto = "japgolly.scalajs.react.compname.auto"
  private inline def KeyCompNameAll  = "japgolly.scalajs.react.compname.all"
  // TODO: ^^ s/def/val/ after RC1

  transparent inline def Instance: ScalaJsReactConfig =
    ${ CompileTimeConfig[ScalaJsReactConfig](KeyConfigClass, 'Defaults) }

  object Defaults extends ScalaJsReactConfig {
    import Util.ComponentName.*
    import scala.scalajs.LinkingInfo.productionMode

    override transparent inline def automaticComponentName(name: String) =
      inline CompileTimeConfig.getTrimLowerCaseNonBlank(KeyCompNameAuto) match {
        case Some("blank") => ""
        case Some("short") => stripPath(stripComponentSuffix(name))
        case Some("full")  => stripComponentSuffix(name)
        case None          => stripComponentSuffix(name)
        case Some(x)       =>
          NewMacroUtils.inlineWarn(s"Invalid value for $KeyCompNameAuto: $x.\nValid values are: full | short | blank.")
          stripComponentSuffix(name)
      }

    override transparent inline def modifyComponentName(name: => String): String =
      inline CompileTimeConfig.getTrimLowerCaseNonBlank(KeyCompNameAll) match {
        case Some("blank") => ""
        case None          => name
        case Some(x)       =>
          NewMacroUtils.inlineWarn(s"Invalid value for $KeyCompNameAll: $x.\nValid values are: blank.")
          name
      }

    private[react] var reusabilityOverrideVar: ReusabilityOverride | Null =
      null

    override inline def reusabilityOverride: ReusabilityOverride | Null =
      if productionMode then null else reusabilityOverrideVar

    // /** Calls to [[Reusability.shouldComponentUpdate]] can be overridden to use the provided logic.
    //   *
    //   * Rather than call this directly yourself, you probably want to call one of the following instead:
    //   *
    //   * - `ReusabilityOverlay.overrideGloballyInDev()`
    //   * - `Reusability.disableGloballyInDev()`
    //   */
    inline def unsafeOverrideReusability(inline f: ReusabilityOverride): Unit =
      if productionMode then () else reusabilityOverrideVar = f
  }

  // TODO doc
  trait Defaults extends ScalaJsReactConfig {

    override def automaticComponentName(name: String): String =
      Defaults.automaticComponentName(name)

    override def modifyComponentName(name: => String): String =
      Defaults.modifyComponentName(name)

    override def reusabilityOverride: ReusabilityOverride =
      Defaults.reusabilityOverride
  }

  object Util {
    object ComponentName {
      import CompileTimeString.*

      transparent inline def stripComponentSuffix(name: String): String =
        replaceFirst(name, "(?i)\\.?comp(?:onent)?$", "")

      transparent inline def stripPath(name: String): String =
        replaceFirst(name, "^.+\\.", "")
    }
  }

  // TODO: move into tests
  private class ProofThatDefaultsAreComplete extends Defaults
}

// TODO: delete below and document everything above
trait Reusability[A]
object ScalaComponent { type Config[P, C <: Children, S, B, U <: UpdateSnapshot, X <: UpdateSnapshot] = japgolly.scalajs.react.component.builder.ComponentBuilder.Config[P, C, S, B, U, X] }
//     // Overriding behaviour of Reusability.shouldComponentUpdate
