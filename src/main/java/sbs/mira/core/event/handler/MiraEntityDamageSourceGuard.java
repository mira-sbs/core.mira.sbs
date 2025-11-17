package sbs.mira.core.event.handler;

import org.bukkit.damage.DamageSource;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.model.MiraEventHandlerModel;

import java.util.function.Predicate;

public
class MiraEntityDamageSourceGuard<Pulse extends MiraPulse<?, ?>>
  extends MiraEventHandlerModel<EntityDamageEvent, Pulse>
{
  private final @NotNull Predicate<DamageSource> damage_source_predicate;
  
  public
  MiraEntityDamageSourceGuard(
    @NotNull Pulse pulse,
    @NotNull Predicate<DamageSource> damage_source_predicate )
  
  {
    super( pulse );
    
    this.damage_source_predicate = damage_source_predicate;
  }
  
  @Override
  @EventHandler
  public
  void handle_event( EntityDamageEvent event )
  {
    if ( this.damage_source_predicate.test( event.getDamageSource( ) ) )
    {
      event.setCancelled( true );
    }
  }
}
