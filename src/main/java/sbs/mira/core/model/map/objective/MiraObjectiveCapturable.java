package sbs.mira.core.model.map.objective;

import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.map.MiraObjective;
import sbs.mira.core.model.map.MiraTeamModel;

/**
 * public interface for map objectives that are held by a team - defensively -
 * to be protected from offending teams.
  *
 * @author jj stephen
 * @author nibs
 * @version 1.0.1
 * @since 1.0.1
 */
public
interface MiraObjectiveCapturable
  extends MiraObjective
{
  /**
   * the defending team must prevent this objective from being captured by
   * the enemy.
   * specific configurations are determined by the specific map being played.
   *
   * @return the team who owns and defends this objective.
   */
  @NotNull
  MiraTeamModel team( );
}
