package sbs.mira.core.model.map.objective;

import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.map.MiraObjective;

/**
 * objectives that do not consist of multiple objectives operating in tandem
 * are considered atomic units of structure that cannot be broken down further.
 * these objectives are what the player interacts with - and will tie a name
 * to. objectives that are singular and not composites require verbal tagging.
 *
 * @author jj stephen
 * @version 1.0.1
 * @since 1.0.1
 */
public
interface MiraNamedObjective
  extends MiraObjective
{
  
  /**
   * simple name serving as a label and/or brief descriptor of this objective.
   * for example:
   * i.e. flags: 'Flag A', 'Flag B', 'Courtyard', 'Throne Room', etc.
   * objectives that are not atomic do not require this.
   *
   * @return the name descriptor of this objective.
   */
  @NotNull
  String name( );
  
  /**
   * complex string visualising the state of this objective for a scoreboard.
   * objectives that are not atomic do not require this.
   *
   * @return the scoreboard descriptor of this objective.
   */
  @NotNull
  String description( );
}
