package sbs.mira.core.model.map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public
class MiraMapOld_DELETEME
{
  
  /**
   * Creates a map-specific gadget that should be declared final.
   *
   * @param material Material to make the gadget from.
   * @param data     ItemStack data, if any.
   * @param name     Name of gadget.
   * @param lore     Gadget description. (multiple lines allowed)
   */
  protected
  ItemStack createGadget( Material material, int amount, int data, String name, String... lore )
  {
    //TODO: pls make use item, but can't be used in constructors
    ArrayList<String> loreList = new ArrayList<>( );
    for ( Object st : lore )
    {
      loreList.add( st.toString( ) );
    }
    ItemStack result = new ItemStack( material, amount, ( short ) data );
    ItemMeta meta = result.getItemMeta( );
    meta.setDisplayName( ChatColor.BLUE + name );
    meta.setLore( loreList );
    result.setItemMeta( meta );
    result.addUnsafeEnchantment( Enchantment.INFINITY, 1 );
    
    return result;
  }
  
  /**
   * Tries to take an ItemStack from a player's inventory, and returns
   * true if this was successful.
   *
   * @param event  Interaction event.
   * @param toTake Item to take.
   * @return Was this successful?
   */
  protected
  boolean useGadget( PlayerEvent event, EquipmentSlot hand, ItemStack toTake, boolean autoTake )
  {
    PlayerInventory inv = event.getPlayer( ).getInventory( );
    // Only take one of the gadget.
    toTake = toTake.clone( );
    
    ItemStack inHand;
    switch ( hand )
    {
      case HAND:
        inHand = inv.getItemInMainHand( );
        break;
      case OFF_HAND:
        inHand = inv.getItemInOffHand( );
        break;
      default:
        return false;
    }
    toTake.setDurability( inHand.getDurability( ) ); // Equalize the durabilities
    toTake.setAmount(
      inHand.getAmount( ) ); // Set the comparison amount equal to the amount in their hand.
    
    if ( inHand.isSimilar( toTake ) )
    {
      if ( autoTake )
      {
        inHand.setAmount( inHand.getAmount( ) - 1 );
      }
      return true;
    }
    else
    {
      return false;
    }
  }
}
