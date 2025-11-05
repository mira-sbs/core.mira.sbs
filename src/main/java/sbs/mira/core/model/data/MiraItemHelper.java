package sbs.mira.core.model.data;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import sbs.mira.core.MiraModel;
import sbs.mira.core.model.MiraPlayerModel;
import sbs.mira.core.MiraPulse;

import java.util.ArrayList;

/**
 * This class handles cruicial inventory and
 * item-related prodecures as documented below.
 * <p>
 * Created by Josh on 18/04/2017.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see ItemStack
 * @since 1.0
 */
@SuppressWarnings("unused")
public
class MiraItemHelper
  extends MiraModel<MiraPulse<?, ?>>
{
  
  public
  MiraItemHelper( MiraPulse<?, ?> main)
  {
    super(main);
  }
  
  /**
   * Checks if an inventory is empty.
   *
   * @param inv Inventory to check.
   * @return Whether or not it is empty.
   * @see Inventory
   * <p>
   * Currently not used, but may used in the future?
   * (-> no maps I added currently use this function)
   */
  public
  boolean isInventoryEmpty(Inventory inv)
  {
    for (ItemStack item : inv.getContents())
    {
      {
        if (item != null)
        {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * completely resets a player's inventory and state.
   *
   * @param wp the target to clear.
   * @see Player
   */
  public
  void clear( MiraPlayerModel<?> wp)
  {
    Player target = wp.crafter();
    target.closeInventory();
    for (PotionEffect pe : target.getActivePotionEffects())
    {
      target.removePotionEffect(pe.getType());
    }
    target.getInventory().clear();
    target.getInventory().setArmorContents(new ItemStack[4]);
    target.setExp(0);
    target.setLevel(0);
    target.setHealth(20);
    target.setSaturation(20F);
    target.setFoodLevel(20);
    target.setMaxHealth(20);
  }
  
  /**
   * modifies an ItemStack's ItemMeta.
   *
   * @param stack the ItemStack to change.
   * @param name  the ItemStack's name.
   * @param lore  the ItemStack's lore.
   */
  public
  ItemStack changeItem(ItemStack stack, String name, Object[] lore)
  {
    ArrayList<String> loreList = new ArrayList<>();
    for (Object st : lore)
    {
      loreList.add(st.toString());
    }
    return changeItem(stack, name, loreList);
  }
  
  /**
   * ORIGINAL METHOD.
   * Modifies an ItemStack's ItemMeta.
   *
   * @param stack The ItemStack to change.
   * @param name  The ItemStack's name.
   * @param lore  The ItemStack's lore.
   */
  private
  ItemStack changeItem(ItemStack stack, String name, ArrayList<String> lore)
  {
    ItemMeta meta = stack.getItemMeta();
    if (name != null)
    {
      meta.setDisplayName(ChatColor.RED + name);
    }
    if (lore != null)
    {
      meta.setLore(lore);
    }
    stack.setItemMeta(meta);
    return stack;
  }
  
  /**
   * ORIGINAL METHOD.
   * modifies an ItemStack's ItemMeta.
   * use this method if you do not want to apply lore.
   *
   * @param stack the ItemStack to change.
   * @param name  the ItemStack's name.
   *              <p>
   *              currently not used, but may used in the future?
   *              (-> no maps I added currently use this function)
   */
  public
  ItemStack changeItem(ItemStack stack, String name)
  {
    ItemMeta meta = stack.getItemMeta();
    if (name != null)
    {
      meta.setDisplayName(ChatColor.RED + name);
    }
    stack.setItemMeta(meta);
    return stack;
    // For statement documentation, check the above function.
  }
  
  /**
   * colors an item depending on the user's current team.
   * ignores non-leather armor.
   *
   * @param armor the armor piece to color.
   * @param currentTeam the user's current team.
   * @return the colored armor.
   * <p>
   *
  private
  ItemStack colorArmor(ItemStack armor, WarTeam currentTeam)
  {
  if (armor.getType().toString().startsWith("LEATHER_"))
  {
  LeatherArmorMeta meta = (LeatherArmorMeta) armor.getItemMeta(); // Gets the leather armor's specific meta.
  meta.setColor(convertChatToDye(currentTeam.getTeamColor())); // Sets the color of the leather armor.
  armor.setItemMeta(meta); // Apply our changes!
  }
  return armor;
  }*/
  
  /**
   * Colors supplied armor (if leather) and applies it
   * to the user automatically according to team color.
   *
   * @param dp    The player to apply the armor to.
   * @param armor The armor supplied.
   *
  public
  void applyArmorAcccordingToTeam(MiraPlayer dp, Material[] armor)
  {
  for (Material toApply : armor)
  {
  ItemStack result = colorArmor(new ItemStack(toApply), dp.getCurrentTeam());
  switch (toApply.toString().split("_")[1])
  {
  case "HELMET":
  dp.crafter().getInventory().setHelmet(result);
  break;
  case "CHESTPLATE":
  dp.crafter().getInventory().setChestplate(result);
  break;
  case "LEGGINGS":
  dp.crafter().getInventory().setLeggings(result);
  break;
  case "BOOTS":
  dp.crafter().getInventory().setBoots(result);
  break;
  }
  }
  }*/
  
  /**
   * Returns the skull of a player.
   *
   * @param name The name of the player.
   * @return The skull.
   * @see SkullMeta
   * <p>
   * Currently not used, but may used in the future?
   * (-> no maps I added currently use this function)
   */
  public
  ItemStack giveSkull(String name)
  {
   /* ItemStack skull = new ItemStack(Material.SK, 1, (short) 3); // Creates an ItemStack of player skull.
    SkullMeta meta = (SkullMeta) skull.getItemMeta(); // Gets the skull item's specific meta.
    meta.setOwner(name); // Sets the skull to a player's IGN for their skin.
    skull.setItemMeta(meta); // Apply our changes!
    return skull;*/
    return null;
  }
  
  /**
   * creates a potion.
   *
   * @param type      potion effect type.
   * @param duration  duration.
   * @param amplifier strength.
   * @return the potion.
   * <p>
   */
  public
  ItemStack createPotion(PotionEffectType type, int duration, int amplifier, int amount)
  {
    ItemStack POTION = new ItemStack(Material.POTION, amount); // Creates a potion with no ingredients.
    PotionMeta meta = (PotionMeta) POTION.getItemMeta(); // Gets the potion's specific meta.
    PotionEffect effect = new PotionEffect(type, duration, amplifier); // Create the custom effect.
    meta.addCustomEffect(effect, true); // Add the custom effect.
    meta.setDisplayName(ChatColor.WHITE + "Potion of " + pulse()
      .model( )
      .strings()
      .potionEffect(effect)); // Don't show it as uncraftable.
    POTION.setItemMeta(meta); // Apply our changes!
    return POTION;
  }
  
  /**
   * creates a tipped arrow.
   *
   * @param type      potion effect type.
   * @param duration  duration.
   * @param amplifier strength.
   * @return the arrow.
   */
  public
  ItemStack createTippedArrow(PotionEffectType type, int duration, int amplifier, int amount)
  {
    ItemStack ARROW = new ItemStack(Material.TIPPED_ARROW, amount);
    PotionMeta meta = (PotionMeta) ARROW.getItemMeta();
    PotionEffect effect = new PotionEffect(type, duration, amplifier);
    meta.addCustomEffect(effect, true);
    meta.setColor(type.getColor());
    meta.setDisplayName(ChatColor.WHITE + "Potion of " + pulse().model( ).strings( ).potionEffect( effect ) );
    ARROW.setItemMeta(meta);
    return ARROW;
  }
  
  /**
   * converts a Minecraft "Chat Color" into the most
   * appropriate "Dye Color" possible for team armor.
   *
   * @param color the ChatColor to convert.
   * @return the matching Color.
   */
  private
  Color convertChatToDye(ChatColor color)
  {
    return switch (color)
    {
      case AQUA -> Color.AQUA;
      case BLACK -> Color.BLACK;
      case BLUE -> Color.BLUE;
      case DARK_AQUA -> Color.TEAL;
      case DARK_BLUE -> Color.NAVY;
      case DARK_GRAY, GRAY -> Color.GRAY;
      case DARK_GREEN -> Color.GREEN;
      case DARK_PURPLE -> Color.PURPLE;
      case DARK_RED -> Color.MAROON;
      case GOLD -> Color.ORANGE;
      case GREEN -> Color.LIME;
      case LIGHT_PURPLE -> Color.FUCHSIA;
      case RED -> Color.RED;
      case YELLOW -> Color.YELLOW;
      default -> Color.WHITE;
    };
  }
}
