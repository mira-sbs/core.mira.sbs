package sbs.mira.core.event.handler;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.model.MiraEventHandlerModel;

import java.util.function.Predicate;

public
class MiraBlockBreakGuard<Pulse extends MiraPulse<?, ?>>
  extends MiraEventHandlerModel<BlockBreakEvent, Pulse>
{
  private final @Nullable Predicate<Block> block_guard_predicate;
  
  public
  MiraBlockBreakGuard( @NotNull Pulse pulse, @NotNull Predicate<Block> block_guard_predicate )
  {
    super( pulse );
    
    this.block_guard_predicate = block_guard_predicate;
  }
  
  public
  MiraBlockBreakGuard( @NotNull Pulse pulse )
  {
    super( pulse );
    
    this.block_guard_predicate = null;
  }
  
  @Override
  @EventHandler
  public
  void handle_event( BlockBreakEvent event )
  {
    if ( this.block_guard_predicate == null ||
         this.block_guard_predicate.test( event.getBlock( ) ) )
    {
      event.setCancelled( true );
    }
  }
}
