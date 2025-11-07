package sbs.mira.core.model.map.objective;

import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraModel;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.model.utility.Position;

public abstract
class MiraObjectiveFlag<Pulse extends MiraPulse<?, ?>>
  extends MiraModel<Pulse>
  implements MiraObjectiveCaptureBlock
{
  private final @NotNull Material flag_material;
  private final @NotNull Position flag_position;
  
  private boolean active;
  
  protected @Nullable World
    world;
  
  public
  MiraObjectiveFlag(
    @NotNull Pulse pulse,
    @NotNull Material flag_material,
    @NotNull Position flag_position )
  {
    super( pulse );
    
    this.flag_material = flag_material;
    this.flag_position = flag_position;
  }
  
  @Override
  public
  void activate( @NotNull World world )
  {
    if ( active )
    {
      throw new IllegalStateException( "flag objective already active?" );
    }
    
    this.active = true;
    this.world = world;
    this.block( world ).setType( this.flag_material );
  }
  
  @Override
  public
  void deactivate( )
  {
    if ( !active )
    {
      throw new IllegalStateException( "flag objective not active?" );
    }
    
    assert this.world != null;
    
    this.block( this.world ).setType( Material.BEDROCK );
    this.world = null;
    this.active = false;
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
  public @NotNull
  World world( )
  {
    assert this.world != null;
    
    return this.world;
  }
}
