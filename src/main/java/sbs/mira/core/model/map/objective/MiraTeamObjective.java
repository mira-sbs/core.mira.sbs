package sbs.mira.core.model.map.objective;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.map.MiraObjective;

/**
 * public interface for map objectives that are specifically team-based.
 * these objectives extend the general concept of a mira objective by
 * associating an objective with a particular team label and therefore color.
 *
 * @author jj stephen
 * @author nibs
 * @version 1.0.1
 * @since 1.0.1
 */
public
interface MiraTeamObjective
  extends MiraObjective
{
  /**
   * this label is used to distinguish teams within the game mode to link
   * objectives to a specific team identity.
   *
   * @return the label identifying the team associated with this objective.
   */
  @NotNull
  String team_label( );
  
  /**
   * @return the chat color representing the team's identity.
   */
  @NotNull
  ChatColor team_color( );
}
