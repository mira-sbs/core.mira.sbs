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
class MiraBlockBreakDropGuard<Pulse extends MiraPulse<?, ?>>
  extends MiraEventHandlerModel<BlockBreakEvent, Pulse>
{
  private final @Nullable Predicate<Block> block_drop_guard_predicate;
  
  public
  MiraBlockBreakDropGuard(
    @NotNull Pulse pulse,
    @NotNull Predicate<Block> block_drop_guard_predicate )
  {
    super( pulse );
    
    this.block_drop_guard_predicate = block_drop_guard_predicate;
  }
  
  public
  MiraBlockBreakDropGuard( @NotNull Pulse pulse )
  {
    super( pulse );
    
    this.block_drop_guard_predicate = null;
  }
  
  @Override
  @EventHandler
  public
  void handle_event( BlockBreakEvent event )
  {
    if ( this.block_drop_guard_predicate == null ||
         this.block_drop_guard_predicate.test( event.getBlock( ) ) )
    {
      event.setDropItems( false );
    }
  }
}
