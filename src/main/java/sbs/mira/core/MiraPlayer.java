package sbs.mira.core;

import org.bukkit.craftbukkit.v1_21_R6.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * oh look another mira stan.
 * created on 2017-03-21.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @version 1.0.1
 * @since 1.0.0
 */
public abstract
class MiraPlayer<Pulse extends MiraPulse<?, ?>>
  extends MiraModule<Pulse>
{
  
  protected final @NotNull CraftPlayer player;
  
  /**
   * @param player is for me?
   * @param pulse  anchorrr.
   */
  public
  MiraPlayer(@NotNull CraftPlayer player, @NotNull Pulse pulse)
  {
    super(pulse);
    this.player = player;
  }
  
  /**
   * @see CraftPlayer
   */
  @NotNull
  public
  CraftPlayer crafter()
  {
    return player;
  }
  
  /**
   * @param message The message to send to the player.
   * @see Player#sendMessage(String)
   */
  public
  void dm(String message)
  {
    player.sendMessage(message);
  }
  
  /**
   * @return the current in game name of this mira stan.
   * @see Player#getName()
   */
  public
  String name()
  {
    return player.getName();
  }
  
  /**
   * @return formatted "display" name with formatting enabled+encouraged.
   * @see Player#getName()
   */
  public
  String display_name()
  {
    return player.getDisplayName();
  }
  
  public
  UUID uuid()
  {
    return player.getUniqueId();
  }
}
