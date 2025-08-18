package sbs.mira.core.helper;


import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.v1_21_R6.CraftServer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraModule;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.helper.world.HyperBreathChunkGenerator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * this class means the world to us.
 * created on 2017-04-18.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @version 1.0.1
 * @see org.bukkit.World
 * @since 1.0.0
 */
public final
class MiraWorldHelper
  extends MiraModule<MiraPulse<?, ?>>
{
  // TODO: make builder classes so we don't have to make 4 function calls to access file helper lol.
  private final MiraFileHelper files;
  private final CraftServer server;
  
  private final String repo_path;
  
  /**
   *
   * @param pulse
   */
  public
  MiraWorldHelper( MiraPulse<?, ?> pulse )
  {
    super( pulse );
    
    this.files = pulse( ).master( ).files( );
    this.server = ( CraftServer ) pulse( ).plugin( ).getServer( );
    this.repo_path = pulse( ).master( ).config( ).get( "mira.file_paths.world_repository" );
  }
  
  public
  void breathe( )
  {
  }
  
  public
  @NotNull
  File repo( )
  {
    return new File( repo_path );
  }
  
  /**
   *
   * @param world_label    world "names" are so vibe coder coded.
   * @param hyper_breather bro stop or ur gonna fill the world with all ur hot air.
   * @param seed           IM INTERESTED??
   * @return
   * @see sbs.mira.core.helper.world.HyperBreathChunkGenerator
   */
  public
  World loads( String world_label, boolean hyper_breather, long seed )
  {
    pulse( ).plugin( ).log( "[worlds] loads '%s'...".formatted( world_label ) );
    
    WorldCreator creator = new WorldCreator( world_label ).seed( seed );
    
    if ( hyper_breather )
    {
      creator.generator( new HyperBreathChunkGenerator( ) );
    }
    
    return server.createWorld( creator );
  }
  
  public
  World loads( String world_label, boolean hyper_breather )
  {
    return this.loads( world_label, hyper_breather, 0xfdffdeadfadecbfL );
  }
  
  /**
   * also saves just in case the artist is an adhd'er,
   * and forgot to save their masterpiece.
   *
   * @param to_store welcome to the repository bucko. -_-
   * @param repo_label want a new name? >_<
   * @return true if that mf'er is now in the flippin' worlds repository.
   * @throws java.lang.IllegalStateException bruh.. are you really trying to overwrite without consent?
   */
  public
  void stores( @NotNull World to_store, @NotNull String repo_label, boolean allow_overwrite ) throws IOException, IllegalStateException
  {
    to_store.save( );
    
    String world_label = to_store.getName( );
    
    this.pulse( ).master( ).files( ).copies(
      Path.of( server.getWorldContainer( ).getAbsolutePath( ), world_label ),
      Path.of( repo_path, repo_label ),
      allow_overwrite
    );
    
  }
  
  /**
   * that world in the world repository ain't no good where it is.
   * move it into the server directory. we can use it then.
   *
   * @param repository_world_label we got the repo folder.
   * @param target_world_label     how bout that pesky world folder?
   */
  public
  boolean remembers( @NotNull String repository_world_label, @NotNull String target_world_label )
  throws IOException
  {
    pulse( ).plugin( ).log(
      "[worlds] copying '%s' from repository into '%s'...".formatted(
        repository_world_label,
        target_world_label
      )
    );
    
    if ( !repo( ).exists( ) )
    {
      return false;
    }
    
    // todo: add whitelist to only copy across `level.dat`, `uid.dat`, and `*.mca` files.
    this.pulse( ).master( ).files( ).copies(
      Path.of( repo_path, repository_world_label ),
      Path.of( server.getWorldContainer( ).getAbsolutePath( ), target_world_label ),
      false
    );
    
    return true;
  }
  
  /**
   * discarded like jj's last hyper-scope-creep changes before making the `git commit`.
   * forgotten even.
   *
   * @param world_label the world to restore, using the 5-digit ID.
   */
  public
  void forgets( @NotNull String world_label ) throws IllegalStateException, IOException
  {
    pulse( ).plugin( ).log( "[worlds] forgetting '%s'...".formatted( world_label ) );
    
    if ( server.getWorld( world_label ) != null )
    {
      this.unloads( Objects.requireNonNull( pulse( ).plugin( ).getServer( ).getWorld( world_label ) ), false );
    }
    
    this.files.deletes( new File( server.getWorldContainer( ).getAbsolutePath( ), world_label ) );
  }
  
  
  /**
   * hey there silly world.
   * we r gonna unload u whether u like it or not.
   * we'll decide whether it's worth remembering your suffering up until this point.
   * brendon urie style ig.. ://
   * (manually removes reference from CraftServer if `Bukkit.unloadWorld()` fails.)
   *
   * @param victim        the living, breathing entity we are ripping from the fabric of existence without consent.
   * @param lemme_save_rq true if this round of otherworldly suffering should be committed to file storage.
   * @see java.lang.ReflectiveOperationException
   */
  public
  void unloads( @NotNull World victim, boolean lemme_save_rq ) throws IllegalStateException
  {
    String world_label = victim.getName( );
    List<Player> evictees = victim.getPlayers( );
    
    // the world will not unload if there are any players still within it.
    pulse( ).plugin( ).log(
      "[worlds] unloading '%s' and evicting %d players(s)...".formatted(
        world_label,
        evictees.size( )
      )
    );
    
    for ( Player evictee : evictees )
    {
      if ( !evictee.teleport( server.getWorlds( ).getFirst( ).getSpawnLocation( ) ) )
      {
        pulse( ).plugin( ).log(
          "[worlds] unable to evict %s from '%s', kicking...".formatted(
            world_label,
            evictee.getName( )
          )
        );
        evictee.kickPlayer(
          "we were unable to evict you from world '%s' during unloading, so we kicked you instead lol.".formatted(
            world_label
          )
        );
      }
    }
    
    if ( !server.unloadWorld( victim, lemme_save_rq ) )
    {
      pulse( )
        .plugin( )
        .log( "[worlds] unable to unload '%s', attempting manual dereference...".formatted( world_label ) );
      
      try
      {
        Field worlds_field = CraftServer.class.getDeclaredField( "worlds" );
        worlds_field.setAccessible( true );
        
        ( ( Map<?, ?> ) worlds_field.get( server ) ).remove( victim.getName( ).toLowerCase( ) );
        
        worlds_field.setAccessible( false );
      }
      catch ( ReflectiveOperationException e )
      {
        pulse( ).plugin( ).log( "[worlds] catastrophic failure; did you reflect correctly?" );
        
        throw new IllegalStateException( e );
      }
    }
    
  }
}