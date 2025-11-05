package sbs.mira.core.model;

import org.bukkit.craftbukkit.v1_21_R6.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraModel;
import sbs.mira.core.MiraPulse;

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
class MiraPlayerModel<Pulse extends MiraPulse<?, ?>>
  extends MiraModel<Pulse>
{
  
  protected final @NotNull CraftPlayer player;
  
  /**
   * @param player is for me?
   * @param pulse  anchorrr.
   */
  public
  MiraPlayerModel( @NotNull CraftPlayer player, @NotNull Pulse pulse )
  {
    super( pulse );
    this.player = player;
  }
  
  /**
   * @see CraftPlayer
   */
  public @NotNull
  CraftPlayer crafter( )
  {
    return player;
  }
  
  public @NotNull
  UUID uuid( )
  {
    return this.player.getUniqueId( );
  }
  
  /**
   * @see org.bukkit.entity.Player#sendMessage(String)
   */
  public
  void messages( @NotNull String content )
  {
    this.crafter( ).sendMessage( content );
  }
  
  /**
   * @return the current in game name of this mira stan.
   * @see Player#getName()
   */
  public
  String name( )
  {
    return player.getName( );
  }
  
  /**
   * @return formatted "display" name with formatting enabled+encouraged.
   * @see Player#getName()
   */
  public
  String display_name( )
  {
    return player.getDisplayName( );
  }
}
