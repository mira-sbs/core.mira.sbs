package sbs.mira.core.utility;


import org.bukkit.ChatColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Objects;
import java.util.Random;

/**
 * This class handles all procedures or functions
 * relating to Spigot entities, such as players,
 * monsters, animals, etc.
 * <p>
 * Created by Josh on 23/04/2017.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see Entity
 * @since 1.0
 */
public
class MiraEntityUtility
{
  private final static Random rng = new Random( );
  
  /**
   * Spawns a firework with a random shape, but defined color.
   * This will spawn at the location specified. This procedure
   * should be called when necessary to pretty up things.
   *
   * @param location The location for the firework to be spawned at.
   * @param color    The color of the firework.
   */
  public static
  void spawn_firework( Location location, ChatColor color )
  {
    Firework firework = ( Firework ) Objects.requireNonNull( location.getWorld( ) ).spawnEntity(
      location,
      EntityType.FIREWORK_ROCKET );
    
    FireworkMeta firework_meta = firework.getFireworkMeta( );
    firework_meta.addEffect( FireworkEffect.builder( )
      .flicker( rng.nextBoolean( ) )
      .withColor( MiraItemUtility.color_from_chat( color ) )
      .withFade( MiraItemUtility.color_from_index( rng.nextInt( 17 ) + 1 ) )
      .with( FireworkEffect.Type.values( )[ rng.nextInt( FireworkEffect.Type.values( ).length ) ] )
      .trail( rng.nextBoolean( ) )
      .build( ) );
    firework_meta.setPower( rng.nextInt( 2 ) + 1 );
    firework.setFireworkMeta( firework_meta );
  }
  
  /**
   * Spawns a firework with a random shape and random color.
   * This will spawn at the location specified. This procedure
   * should be called when necessary to pretty up things.
   *
   * @param location The location for the firework to be spawned at.
   */
  public static
  void spawn_firework( Location location )
  {
    MiraEntityUtility.spawn_firework(
      location,
      ChatColor.values( )[ rng.nextInt( ChatColor.values( ).length ) ] );
  }
}