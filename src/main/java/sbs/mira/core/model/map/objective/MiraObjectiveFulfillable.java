package sbs.mira.core.model.map.objective;

import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.map.MiraObjective;

import java.util.Collection;

/**
 * public interface for map objectives with a requirement to be fulfilled.
 * multiple teams should compete to fulfill the requirement - to be rewarded any
 * number of points as a result.
 * in the instance that the objective
 *
 * @author jj stephen
 * @author nibs
 * @version 1.0.1
 * @since 1.0.1
 */
public
interface MiraObjectiveFulfillable
  extends MiraObjective
{
  /**
   * the objective must be able to indicate whether it is currently fulfilled.
   * this can be due to a team reaching 100% progress towards fulfillment -
   * or due to the match expiring after reaching the maximum duration.
   *
   * @return true - if the objective has been fulfilled with winner(s) determined.
   */
  boolean fulfilled( );
  
  /**
   * the objective can be fulfilled manually - for example, the match timer
   * expiring, or by manual command (rarely).
   * in this case, the objective was not fulfilled by any team.
   * the winner must be determined based on which team(s) made the most
   * progress - plural, as there may be a tie.
   * in the case of a multi-team tie, all tied teams win.
   */
  void manual_fulfil( );
  
  /**
   * the typical end result is that only one team (against one or more opposing
   * teams) will fulfil this objective before the match timer expires.
   * multiple teams can be declared as winners if their final progress towards
   * fulfilment are equal - this only happens when the match timer expires.
   * winning team(s) should be declared upon deactivation of the objective -
   * if they haven't been already.
   *
   * @return the winning team(s), by label.
   * @throws IllegalStateException winner(s) not yet declared.
   */
  @NotNull
  Collection<String> winning_teams( )
  throws IllegalStateException;
}
