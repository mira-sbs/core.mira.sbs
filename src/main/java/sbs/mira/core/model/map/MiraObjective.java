package sbs.mira.core.model.map;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * public interface for map objectives - which drive how a game mode operates.
 * classes implementing this interface form an aggregate relationship between
 * maps and game modes - and therefore matches.
 *
 * @author jj stephen
 * @version 1.0.1
 * @since 1.0.1
 */
public
interface MiraObjective
{
  /**
   * activation must cause the objective to enter its active match state.
   * this involves registering listeners and initialising implementation-
   * specific attributes.
   * objectives should be activated by their associated game mode - whilst
   * their definitions should be declared in the associated map.
   *
   * @param world the world that this objective currently exists in.
   */
  void activate( @NotNull World world );
  
  /**
   * deactivation must cause the objective to enter its "dead" state:
   * i. active event handlers should be unregistered.
   * ii. references to bukkit and mira objects should be dropped.
   * iii. the final state of the objective should be captured for statistics.
   */
  void deactivate( );
  
  /**
   * objective definitions are declared within the context of a map.
   * therefore - objectives exist before a game mode is chosen - and may behave
   * differently depending on what mode is ultimately chosen.
   * the world they reside in is transient and exists only for the lifetime of
   * the match being played.
   *
   * @return the match world that this objective currently exists in.
   */
  @NotNull
  World world( );
}
