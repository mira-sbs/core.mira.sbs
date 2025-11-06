package sbs.mira.core.utility;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This class handles mildly helpful string functions
 * needed for user-friendliness.
 * <p>
 * Created by Josh on 18/04/2017.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see ItemStack
 * @since 1.0
 */
public final
class MiraStringUtility
{
  
  private static final Map<PotionEffectType, String> effects = new HashMap<>( );
  
  {
    effects.put( PotionEffectType.ABSORPTION, "Absorption" );
    effects.put( PotionEffectType.BLINDNESS, "Blindness" );
    effects.put( PotionEffectType.NAUSEA, "Nausea" );
    effects.put( PotionEffectType.RESISTANCE, "Resistance" );
    effects.put( PotionEffectType.HASTE, "Haste" );
    effects.put( PotionEffectType.FIRE_RESISTANCE, "Fire Resistance" );
    effects.put( PotionEffectType.INSTANT_DAMAGE, "Instant Damage" );
    effects.put( PotionEffectType.INSTANT_HEALTH, "Instant Health" );
    effects.put( PotionEffectType.HEALTH_BOOST, "Health Boost" );
    effects.put( PotionEffectType.HUNGER, "Hunger" );
    effects.put( PotionEffectType.STRENGTH, "Strength" );
    effects.put( PotionEffectType.INVISIBILITY, "Invisibility" );
    effects.put( PotionEffectType.JUMP_BOOST, "Jump Boost" );
    effects.put( PotionEffectType.NIGHT_VISION, "Night Vision" );
    effects.put( PotionEffectType.POISON, "Poison" );
    effects.put( PotionEffectType.REGENERATION, "Regeneration" );
    effects.put( PotionEffectType.SATURATION, "Saturation" );
    effects.put( PotionEffectType.SLOWNESS, "Slowness" );
    effects.put( PotionEffectType.MINING_FATIGUE, "Mining Fatigue" );
    effects.put( PotionEffectType.SPEED, "Speed" );
    effects.put( PotionEffectType.WATER_BREATHING, "Water Breathing" );
    effects.put( PotionEffectType.WEAKNESS, "Weakness" );
    effects.put( PotionEffectType.WITHER, "Wither" );
  }
  
  /**
   * Turns an array of Strings into a sentence.
   * <p>
   * i.e. ['1', '2', '3', '4']
   * -> "1, 2, 3 and 4"
   *
   * @param array An array of words.
   * @return A sentence.
   */
  public static
  String verbal_list( List<?> array )
  {
    if ( array.isEmpty( ) )
    {
      return "None";
    }
    StringBuilder format = new StringBuilder( );
    if ( array.size( ) == 1 )
    {
      return array.getFirst( ).toString( );
    }
    int i = 1;
    while ( i <= array.size( ) )
    {
      if ( i == array.size( ) )
      {
        format.append( ChatColor.WHITE ).append( " and " ).append( array.get( i - 1 ).toString( ) );
      }
      else if ( i == 1 )
      {
        format = new StringBuilder( array.getFirst( ).toString( ) );
      }
      else
      {
        format.append( ", " ).append( array.get( i - 1 ).toString( ) );
      }
      i++;
    }
    return format.toString( );
  }
  
  /**
   * Performs the same as above but outputs a TextComponent result.
   *
   * @return The winner format.
   * <p>
   * public
   * TextComponent winnerFormat(List<WarTeam> array)
   * {
   * if (array.size() == 0)
   * {
   * return new TextComponent("No One");
   * }
   * TextComponent result = new TextComponent();
   * if (array.size() == 1)
   * {
   * return array.get(0).getHoverInformation();
   * }
   * int i = 1;
   * while (i <= array.size())
   * {
   * if (i == array.size())
   * {
   * result.addExtra(ChatColor.WHITE + " and ");
   * result.addExtra(array.get(i - 1).getHoverInformation());
   * }
   * else if (i == 1)
   * {
   * result = new TextComponent(array.get(0).getHoverInformation());
   * }
   * else
   * {
   * result.addExtra(ChatColor.WHITE + ", ");
   * result.addExtra(array.get(i - 1).getHoverInformation());
   * }
   * i++;
   * }
   * return result;
   * }
   */
  
  public static
  long generate_random_world_id( long previous_world_id )
  {
    // number between 11111-99999.
    long result = new Random( ).nextInt( 90000 ) + 10000;
    
    if ( previous_world_id == result )
    {
      return generate_random_world_id( previous_world_id );
    }
    
    return result;
  }
  
  /**
   * @param seconds the number of seconds to format.
   * @return the number of seconds - formatted as `mm:ss` and ignoring any hour portion.
   */
  public static @NotNull
  String time_ss_to_mm_ss( int seconds )
  {
    if ( seconds < 0 )
    {
      seconds = -seconds;
    }
    
    NumberFormat two_digit_format = new DecimalFormat( "00" );
    
    int hh_modulo = seconds % 3600;
    
    int mm = hh_modulo / 60;
    int ss = hh_modulo % 60;
    
    return "%s:%s".formatted( two_digit_format.format( mm ), two_digit_format.format( ss ) );
  }
  
  /**
   * Is it second, or seconds?
   * Is it amount, or amounts?
   * <p>
   * i.e. 1 seconds -> 1 second,
   * 2 seconds -> 2 seconds.
   *
   * @param amount The amount.
   * @return The plural.
   */
  public
  String plural( int amount )
  {
    return amount == 1 ? " " : "s ";
  }
  
  public static
  String pretty_potion_effect( PotionEffect effect )
  {
    return effects.get( effect.getType( ) ) +
           " " +
           int_to_roman_numeral( effect.getAmplifier( ) + 1 );
  }
  
  private static
  String int_to_roman_numeral( int number )
  {
    return switch ( number )
    {
      case 10 -> "X";
      case 9 -> "IX";
      case 8 -> "VIII";
      case 7 -> "VII";
      case 6 -> "VI";
      case 5 -> "V";
      case 4 -> "IV";
      case 3 -> "III";
      case 2 -> "II";
      case 1 -> "I";
      default -> "X+";
    };
  }
}
