package sbs.mira.core;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.model.MiraPlayerModel;

public final
class MiraMockPlayer
  extends MiraPlayerModel<MiraMockPulse>
{
  
  public
  MiraMockPlayer( @NotNull Player player, @NotNull MiraMockPulse pulse )
  {
    super( player, pulse );
  }
  
  @Override
  public
  void update( )
  {
  
  }
}
