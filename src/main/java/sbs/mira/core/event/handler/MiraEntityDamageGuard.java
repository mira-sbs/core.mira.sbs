package sbs.mira.core.event.handler;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.model.MiraEventHandlerModel;

import java.util.function.Predicate;

public
class MiraEntityDamageGuard<Pulse extends MiraPulse<?, ?>>
  extends MiraEventHandlerModel<EntityDamageEvent, Pulse>
{
  private final @Nullable Predicate<Entity> entity_guard_predicate;
  
  public
  MiraEntityDamageGuard( @NotNull Pulse pulse, @NotNull Predicate<Entity> entity_guard_predicate )
  {
    super( pulse );
    
    this.entity_guard_predicate = entity_guard_predicate;
  }
  
  public
  MiraEntityDamageGuard( @NotNull Pulse pulse )
  {
    super( pulse );
    
    this.entity_guard_predicate = null;
  }
  
  @Override
  public
  void handle_event( EntityDamageEvent event )
  {
    if ( this.entity_guard_predicate == null ||
         this.entity_guard_predicate.test( event.getEntity( ) ) )
    {
      event.setCancelled( true );
    }
  }
}
