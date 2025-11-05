package sbs.mira.core.model.map.objective;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.model.map.Position;

public
class MiraFlagObjective
  implements MiraCaptureObjective, MiraBlockObjective
{
  private final @NotNull Material flag_material;
  private final @NotNull Position flag_position;
  
  private boolean active;
  private @Nullable World world;
  
  public
  MiraFlagObjective( @NotNull Material flag_material, @NotNull Position flag_position )
  {
    this.flag_material = flag_material;
    this.flag_position = flag_position;
  }
  
  @Override
  public
  void activate( @NotNull World world )
  {
  
  }
  
  @Override
  public
  void deactivate( )
  {
    assert this.world != null;
    this.block( this.world ).setType( Material.BEDROCK );
  }
  
  @Override
  public @NotNull
  Material material( )
  {
    return this.flag_material;
  }
  
  @Override
  public @NotNull
  Position position( )
  {
    return this.flag_position;
  }
  
  @Override
  public
  ChatColor colour( )
  {
    return null;
  }
}
