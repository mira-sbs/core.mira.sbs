package sbs.mira.core.model.map.objective.standard;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.event.match.objective.MiraMatchMonumentDamageEvent;
import sbs.mira.core.model.MiraEventHandlerModel;
import sbs.mira.core.model.MiraPlayerModel;
import sbs.mira.core.model.map.MiraTeamModel;
import sbs.mira.core.model.map.objective.MiraObjectiveCapturableBlockRegion;
import sbs.mira.core.model.utility.Region;

import java.util.ArrayList;
import java.util.List;

public
class MiraObjectiveDestroyMonument<Pulse extends MiraPulse<?, ?>>
  extends MiraObjectiveMonument<Pulse>
  implements MiraObjectiveCapturableBlockRegion
{
  public
  MiraObjectiveDestroyMonument(
    @NotNull Pulse pulse,
    @NotNull String monument_name,
    @NotNull MiraTeamModel build_team,
    @NotNull Material build_material,
    @NotNull Region build_region )
  {
    super( pulse, monument_name, build_team, build_material, build_region );
  }
  
  /**
   * monument blocks can be destroyed by a player physically breaking them -
   * or from an exploding entity that a player was the source of. an example of
   * this is primed tnt - usually with a flint and steel.
   *
   * @param mira_player the player who destroyed the block.
   * @param block       the block that was destroyed.
   * @return false - if the block should be guarded (not destroyed).
   */
  private
  boolean handle_block_destruction( MiraPlayerModel<?> mira_player, Block block )
  {
    if ( !mira_player.has_team( ) )
    {
      return false;
    }
    
    if ( this.monument_team.equals( mira_player.team( ) ) )
    {
      mira_player.bukkit( ).playSound( mira_player.bukkit( ), Sound.ENTITY_CAT_HISS, 1, 1.25F );
      return false;
    }
    
    boolean was_pristine = this.remaining_progress( ) == 100;
    
    this.contribution( mira_player, block );
    
    this.world( ).spawnParticle( Particle.HEART, block.getLocation( ).add( 0.5, 0.5, 0.5 ), 4 );
    this.world( ).playSound(
      block.getLocation( ),
      Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR,
      1.25f,
      1.25f );
    
    this.call_event( new MiraMatchMonumentDamageEvent(
      mira_player,
      this,
      was_pristine,
      block ) );
    
    return true;
  }
  
  @Override
  public
  void activate( @NotNull World world )
  {
    super.activate( world );
    
    for ( Block block : this.remaining_blocks( ) )
    {
      block.setType( this.monument_material );
    }
    
    final MiraObjectiveDestroyMonument<Pulse> self = this;
    
    this.event_handler( new MiraEventHandlerModel<BlockBreakEvent, Pulse>( this.pulse( ) )
    {
      @Override
      @EventHandler (priority = EventPriority.HIGHEST)
      public
      void handle_event( BlockBreakEvent event )
      {
        if ( event.isCancelled( ) )
        {
          return;
        }
        
        Block block = event.getBlock( );
        
        if ( !remaining_blocks.contains( block ) )
        {
          return;
        }
        
        MiraPlayerModel<?> mira_player =
          this.pulse( ).model( ).player( event.getPlayer( ).getUniqueId( ) );
        
        if ( self.handle_block_destruction( mira_player, block ) )
        {
          event.setDropItems( false );
        }
        else
        {
          event.setCancelled( true );
        }
      }
    } );
    
    this.event_handler( new MiraEventHandlerModel<EntityExplodeEvent, Pulse>( this.pulse( ) )
    {
      @Override
      @EventHandler (priority = EventPriority.HIGHEST)
      public
      void handle_event( EntityExplodeEvent event )
      {
        if ( event.isCancelled( ) )
        {
          return;
        }
        
        List<Block> guarded_blocks = new ArrayList<>( );
        
        for ( Block block : event.blockList( ) )
        {
          if ( !self.remaining_blocks.contains( block ) )
          {
            continue;
          }
          
          guarded_blocks.add( block );
          
          Entity entity = event.getEntity( );
          
          if ( entity instanceof TNTPrimed tnt_primed &&
               tnt_primed.getSource( ) instanceof Player player )
          {
            MiraPlayerModel<?> mira_player =
              this.pulse( ).model( ).player( player.getUniqueId( ) );
            
            if ( self.handle_block_destruction( mira_player, block ) )
            {
              block.breakNaturally( new ItemStack( Material.AIR ) );
            }
          }
        }
        
        event.blockList( ).removeAll( guarded_blocks );
      }
    } );
  }
  
  @Override
  @NotNull
  public
  String description( )
  {
    String progress_icon;
    
    if ( this.captured( ) )
    {
      progress_icon = ChatColor.GREEN + "✔";
    }
    else
    {
      progress_icon = ChatColor.RED + "✘";
    }
    
    return this.pulse( ).model( ).message(
      "match.objective.monument.scoreboard",
      this.monument_team.color( ) + this.monument_name,
      progress_icon,
      this.progress_bar( ),
      "%d%%".formatted( this.remaining_progress( ) ) );
  }
}
