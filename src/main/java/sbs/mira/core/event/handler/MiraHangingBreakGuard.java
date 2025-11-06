package sbs.mira.core.event.handler;

import org.bukkit.entity.Hanging;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.model.MiraEventHandlerModel;

import java.util.function.Predicate;

public
class MiraHangingBreakGuard<Pulse extends MiraPulse<?, ?>>
  extends MiraEventHandlerModel<HangingBreakEvent, Pulse>
{
  private final @Nullable Predicate<Hanging> hanging_guard_predicate;
  
  public
  MiraHangingBreakGuard( @NotNull Pulse pulse, @NotNull Predicate<Hanging> hanging_guard_predicate )
  {
    super( pulse );
    
    this.hanging_guard_predicate = hanging_guard_predicate;
  }
  
  public
  MiraHangingBreakGuard( @NotNull Pulse pulse )
  {
    super( pulse );
    
    this.hanging_guard_predicate = null;
  }
  
  @Override
  public
  void handle_event( HangingBreakEvent event )
  {
    if ( this.hanging_guard_predicate == null ||
         this.hanging_guard_predicate.test( event.getEntity( ) ) )
    {
      event.setCancelled( true );
    }
  }
}
