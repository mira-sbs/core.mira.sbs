package sbs.mira.core.model.map.objective;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.event.match.*;
import sbs.mira.core.model.MiraEventHandlerModel;
import sbs.mira.core.model.MiraPlayerModel;
import sbs.mira.core.model.map.MiraTeamModel;
import sbs.mira.core.model.utility.Position;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public
class MiraObjectiveCaptureFlag<Pulse extends MiraPulse<?, ?>>
  extends MiraObjectiveFlag<Pulse>
  implements MiraTeamObjective
{
  private final static String DESCRIPTION_FORMAT = "    %s[%s] █";
  private final static String STOLEN_DESCRIPTION_FORMAT = "    %s[%s] ▓ !—> [%s]";
  
  @NotNull
  private final String flag_name;
  @NotNull
  private final String flag_team_label;
  @NotNull
  private final ChatColor flag_team_color;
  @NotNull
  private final Material flag_material;
  @NotNull
  private final Position flag_position;
  
  @NotNull
  private final Map<String, LinkedList<UUID>> flag_captures;
  @NotNull
  private final Map<String, LinkedList<UUID>> flag_drops;
  
  private boolean allow_quick_steal;
  private boolean flag_stolen;
  @Nullable
  private MiraPlayerModel<?> flag_holder;
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  public
  MiraObjectiveCaptureFlag(
    @NotNull Pulse pulse,
    @NotNull String name,
    @NotNull String team_label,
    @NotNull ChatColor team_color,
    @NotNull Material flag_material,
    @NotNull Position flag_position )
  {
    super( pulse, flag_material, flag_position );
    
    this.allow_quick_steal = false;
    this.flag_name = name;
    this.flag_team_label = team_label;
    this.flag_team_color = team_color;
    this.flag_material = flag_material;
    this.flag_position = flag_position;
    this.flag_captures = new HashMap<>( );
    this.flag_drops = new HashMap<>( );
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  @Override
  public
  void activate( @NotNull World world )
  {
    super.activate( world );
    
    this.event_handler( new MiraFlagStealEventHandler( this.pulse( ) ) );
    this.event_handler( new MiraFlagDropUponLeaveEventHandler( this.pulse( ) ) );
    this.event_handler( new MiraFlagDropUponDeathEventHandler( this.pulse( ) ) );
  }
  
  @Override
  public
  void deactivate( )
  {
    super.deactivate( );
    
    this.flag_holder = null;
  }
  
  @Override
  public @NotNull
  String name( )
  {
    return "";
  }
  
  @Override
  public @NotNull
  String description( )
  {
    if ( this.flag_stolen )
    {
      assert this.flag_holder != null;
      
      return String.format(
        STOLEN_DESCRIPTION_FORMAT,
        this.flag_team_color,
        this.flag_team_label,
        this.flag_holder.name( ) );
    }
    else
    {
      return String.format( DESCRIPTION_FORMAT, this.flag_team_label, this.flag_team_color );
    }
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  @Override
  public @NotNull
  String team_label( )
  {
    return this.flag_team_label;
  }
  
  @Override
  public @NotNull
  ChatColor team_color( )
  {
    return this.flag_team_color;
  }
  
  @Override
  public @NotNull
  Material material( )
  {
    return this.flag_material;
  }
  
  @Override
  public @NotNull
  Position position( )
  {
    return this.flag_position;
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  public
  void allow_quick_steal( )
  {
    if ( this.allow_quick_steal )
    {
      throw new IllegalStateException( "quick steal has already been allowed?" );
    }
    
    this.allow_quick_steal = true;
    
    new MiraFlagQuickStealEventHandler( this.pulse( ) );
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  public
  void try_steal( MiraPlayerModel<?> mira_player )
  {
    if ( this.flag_stolen )
    {
      mira_player.messages( "this flag has already been stolen!" );
      
      return;
    }
    
    if ( !mira_player.has_team( ) )
    {
      return;
    }
    
    MiraTeamModel team = mira_player.team( );
    
    if ( team.label( ).equals( this.flag_team_label ) )
    {
      mira_player.messages( "you can't steal your own flag - defend it!" );
      
      return;
    }
    
    MiraMatchFlagStealEvent flag_steal_event = new MiraMatchFlagStealEvent( mira_player, this );
    
    this.call_event( flag_steal_event );
    
    if ( flag_steal_event.isCancelled( ) )
    {
      return;
    }
    
    this.flag_stolen = true;
    this.flag_holder = mira_player;
    
    String flag_steal_message_format = "%s has stolen %s's flag!";
    Bukkit.broadcastMessage( flag_steal_message_format.formatted(
      mira_player.display_name( ),
      team.coloured_display_name( ) ) );
    
    this.block( ).setType( Material.CRYING_OBSIDIAN );
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  public
  void dropped( )
  {
    if ( !this.flag_stolen )
    {
      throw new IllegalStateException( "cannot drop flag if not stolen?" );
    }
    
    assert this.flag_holder != null;
    
    MiraPlayerModel<?> mira_player = this.flag_holder;
    MiraTeamModel team = mira_player.team( );
    
    
    this.call_event( new MiraMatchFlagDropEvent( mira_player, this ) );
    
    this.flag_stolen = false;
    this.flag_holder = null;
    
    this.flag_drops.putIfAbsent( team.label( ), new LinkedList<>( ) );
    this.flag_drops.get( team.label( ) ).add( mira_player.uuid( ) );
    
    this.block( ).setType( this.flag_material );
    
    for ( Player target : Bukkit.getOnlinePlayers( ) )
    {
      target.playSound( target.getLocation( ), Sound.ENTITY_IRON_GOLEM_HURT, 1F, 1.5F );
    }
    
    String flag_dropped_message_format = "%s has dropped %s's flag!";
    Bukkit.broadcastMessage( flag_dropped_message_format.formatted(
      mira_player.display_name( ),
      team.coloured_display_name( ) ) );
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  public
  void try_capture( MiraObjectiveBuildMonument<?> monument )
  {
    if ( !this.flag_stolen )
    {
      throw new IllegalStateException( "cannot capture flag if not stolen?" );
    }
    
    assert this.flag_holder != null;
    
    MiraPlayerModel<?> mira_player = this.flag_holder;
    MiraTeamModel team = mira_player.team( );
    
    MiraMatchFlagCaptureEvent flag_capture_event =
      new MiraMatchFlagCaptureEvent( mira_player, this, monument, this.block( ) );
    
    this.call_event( flag_capture_event );
    
    if ( flag_capture_event.isCancelled( ) )
    {
      return;
    }
    
    this.block( ).setType( this.flag_material );
    
    this.flag_stolen = false;
    this.flag_holder = null;
    
    for ( Player target : Bukkit.getOnlinePlayers( ) )
    {
      world( ).playSound( target.getLocation( ), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1.66F );
    }
    
    this.flag_captures.putIfAbsent( team.label( ), new LinkedList<>( ) );
    this.flag_captures.get( team.label( ) ).add( mira_player.uuid( ) );
    
    String flag_captured_message_format = "%s has captured %s's flag!";
    Bukkit.broadcastMessage( flag_captured_message_format.formatted(
      mira_player.display_name( ),
      team.coloured_display_name( ) ) );
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  private
  class MiraFlagStealEventHandler
    extends MiraEventHandlerModel<BlockBreakEvent, Pulse>
  {
    protected
    MiraFlagStealEventHandler( @NotNull Pulse pulse )
    {
      super( pulse );
    }
    
    @Override
    public
    void handle_event( BlockBreakEvent event )
    {
      Block block = event.getBlock( );
      
      if ( !block.getLocation( ).equals( flag_position.location( block.getWorld( ), false ) ) )
      {
        return;
      }
      
      event.setCancelled( true );
      
      try_steal( this.pulse( ).model( ).player( event.getPlayer( ).getUniqueId( ) ) );
    }
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  private
  class MiraFlagQuickStealEventHandler
    extends MiraEventHandlerModel<PlayerInteractEvent, Pulse>
  {
    protected
    MiraFlagQuickStealEventHandler( @NotNull Pulse pulse )
    {
      super( pulse );
    }
    
    @Override
    public
    void handle_event( PlayerInteractEvent event )
    {
      if ( !allow_quick_steal )
      {
        return;
      }
      
      if ( event.getAction( ) != Action.LEFT_CLICK_BLOCK )
      {
        return;
      }
      
      Block block_clicked = event.getClickedBlock( );
      
      if ( block_clicked == null )
      {
        return;
      }
      
      if ( !block_clicked.getLocation( ).equals( flag_position.location( world( ), false ) ) )
      {
        return;
      }
      
      event.setCancelled( true );
      
      try_steal( this.pulse( ).model( ).player( event.getPlayer( ).getUniqueId( ) ) );
    }
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  private
  class MiraFlagDropUponLeaveEventHandler
    extends MiraEventHandlerModel<MiraMatchPlayerLeaveTeamEvent, Pulse>
  {
    
    protected
    MiraFlagDropUponLeaveEventHandler( @NotNull Pulse pulse )
    {
      super( pulse );
    }
    
    @Override
    public
    void handle_event( MiraMatchPlayerLeaveTeamEvent event )
    {
      if ( flag_holder == event.player( ) )
      {
        dropped( );
      }
    }
  }
  
  /*——————————————————————————————————————————————————————————————————————————*/
  
  private
  class MiraFlagDropUponDeathEventHandler
    extends MiraEventHandlerModel<MiraMatchPlayerDeathEvent, Pulse>
  {
    
    protected
    MiraFlagDropUponDeathEventHandler( @NotNull Pulse pulse )
    {
      super( pulse );
    }
    
    @Override
    public
    void handle_event( MiraMatchPlayerDeathEvent event )
    {
      if ( flag_holder == event.killed( ) )
      {
        dropped( );
      }
    }
  }
}
