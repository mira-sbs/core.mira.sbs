package sbs.mira.core.event.handler;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.model.MiraEventHandlerModel;

import java.util.function.Predicate;

public
class MiraBlockPlaceGuard<Pulse extends MiraPulse<?, ?>>
  extends MiraEventHandlerModel<BlockPlaceEvent, Pulse>
{
  private final @Nullable Predicate<Block> block_guard_predicate;
  
  public
  MiraBlockPlaceGuard( @NotNull Pulse pulse, @NotNull Predicate<Block> block_guard_predicate )
  {
    super( pulse );
    
    this.block_guard_predicate = block_guard_predicate;
  }
  
  public
  MiraBlockPlaceGuard( @NotNull Pulse pulse )
  {
    super( pulse );
    
    this.block_guard_predicate = null;
  }
  
  @Override
  public
  void handle_event( BlockPlaceEvent event )
  {
    if ( this.block_guard_predicate == null ||
         this.block_guard_predicate.test( event.getBlock( ) ) )
    {
      event.setCancelled( true );
    }
  }
}
