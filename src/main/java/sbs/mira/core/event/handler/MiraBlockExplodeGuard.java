package sbs.mira.core.event.handler;

import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.model.MiraEventHandlerModel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public
class MiraBlockExplodeGuard<Pulse extends MiraPulse<?, ?>>
  extends MiraEventHandlerModel<EntityExplodeEvent, Pulse>
{
  private final @Nullable Predicate<Block> block_guard_predicate;
  
  public
  MiraBlockExplodeGuard( @NotNull Pulse pulse, @NotNull Predicate<Block> block_guard_predicate )
  {
    super( pulse );
    
    this.block_guard_predicate = block_guard_predicate;
  }
  
  public
  MiraBlockExplodeGuard( @NotNull Pulse pulse )
  {
    super( pulse );
    
    this.block_guard_predicate = null;
  }
  
  @Override
  public
  void handle_event( EntityExplodeEvent event )
  {
    if ( this.block_guard_predicate == null )
    {
      event.blockList( ).clear( );
      
      return;
    }
    
    List<Block> to_remove = new ArrayList<>( );
    
    for ( Block block : event.blockList( ) )
    {
      if ( block_guard_predicate.test( block ) )
      {
        to_remove.add( block );
      }
    }
    event.blockList( ).removeAll( to_remove );
  }
}
