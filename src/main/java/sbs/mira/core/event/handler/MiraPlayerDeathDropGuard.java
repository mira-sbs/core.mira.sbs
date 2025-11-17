package sbs.mira.core.event.handler;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.model.MiraEventHandlerModel;

import java.util.function.Predicate;

public
class MiraPlayerDeathDropGuard<Pulse extends MiraPulse<?, ?>>
  extends MiraEventHandlerModel<PlayerDeathEvent, Pulse>
{
  private final @NotNull Predicate<ItemStack> exclude_item_predicate;
  
  public
  MiraPlayerDeathDropGuard(
    @NotNull Pulse pulse,
    @NotNull Predicate<ItemStack> exclude_item_predicate )
  {
    super( pulse );
    this.exclude_item_predicate = exclude_item_predicate;
  }
  
  @Override
  @EventHandler
  public
  void handle_event( PlayerDeathEvent event )
  {
    for ( ItemStack item : event.getDrops( ) )
    {
      if ( exclude_item_predicate.test( item ) )
      {
        item.setType( Material.AIR );
      }
    }
  }
}
