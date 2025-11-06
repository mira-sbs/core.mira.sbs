package sbs.mira.core.utility;


import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.v1_21_R6.CraftServer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.model.data.world.EmptyChunkGenerator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
class MiraWorldUtility
{
  private final static String WORLD_REGION_DIRECTORY_NAME = "region";
  private final static String WORLD_REGION_FILE_EXTENSION = ".mca";
  private final static String[] WORLD_LEVEL_FILE_NAMES = { "level.dat", "uid.dat" };
  
  /**
   * valid world data includes `.mca` and `.dat` files.
   * `.mca` files are hosted in the `region` directory.
   * other directories like `data` and `playerdata` are not useful.
   *
   * @return true - if this is a valid minecraft world file.
   */
  public static
  boolean is_world_directory( File world_directory )
  {
    String world_directory_path = world_directory.getAbsolutePath( );
    
    for ( String world_file_name : WORLD_LEVEL_FILE_NAMES )
    {
      if ( !Files.exists( Path.of( world_directory_path, world_file_name ) ) )
      {
        return false;
      }
    }
    
    File region_directory = new File( world_directory_path, WORLD_REGION_DIRECTORY_NAME );
    
    if ( !region_directory.exists( ) || !region_directory.isDirectory( ) )
    {
      return false;
    }
    
    File[] region_files = region_directory.listFiles( );
    
    if ( region_files == null )
    {
      return false;
    }
    
    for ( File child_file : region_files )
    {
      if ( !is_world_region_file( child_file ) )
      {
        return false;
      }
    }
    
    return true;
  }
  
  /**
   * minecraft world region (`.mca`) files must be contained with a "region" directory.
   *
   * @param world_region_file the file to be checked.
   * @return true - if the file is a valid minecraft world region file.
   */
  public static
  boolean is_world_region_file( File world_region_file )
  {
    return world_region_file.exists( ) &&
           world_region_file.isFile( ) &&
           world_region_file.getName( ).endsWith( WORLD_REGION_FILE_EXTENSION ) &&
           world_region_file.getParentFile( ).getName( ).equals( WORLD_REGION_DIRECTORY_NAME );
  }
  
  /**
   * `level.dat` & `uid.dat` are also considered valid minecraft world files.
   *
   * @param file the file to check.
   * @return true - if provided file is a valid minecraft world level data file.
   */
  public static
  boolean is_world_file( File file )
  {
    return Arrays.stream( WORLD_LEVEL_FILE_NAMES ).anyMatch( level_file_name->file.getName( ).equals(
      level_file_name ) );
  }
  
  /**
   *
   * @param world_label world "names" are so vibe coder coded.
   * @param empty       bro stop or ur gonna fill the world with all ur hot air.
   * @param seed        IM INTERESTED??
   * @return
   * @see EmptyChunkGenerator
   */
  public static
  World loads( String world_label, boolean empty, long seed )
  {
    WorldCreator creator = new WorldCreator( world_label ).seed( seed );
    
    if ( empty )
    {
      creator.generator( new EmptyChunkGenerator( ) );
    }
    
    return creator.createWorld( );
  }
  
  public static
  World loads( String world_label, boolean empty )
  {
    return loads( world_label, empty, 0xfdffdeadfadecbfL );
  }
  
  /**
   * also saves just in case the artist is an adhd'er,
   * and forgot to save their masterpiece.
   *
   * @param to_store   welcome to the repository bucko. -_-
   * @param repo_label want a new name? >_<
   * @return true if that mf'er is now in the flippin' worlds repository.
   * @throws java.lang.IllegalStateException bruh.. are you really trying to overwrite without consent?
   */
  public static
  void stores(
    @NotNull World to_store,
    @NotNull String repo_path,
    @NotNull String repo_label,
    boolean allow_overwrite )
  throws IOException, IllegalStateException
  {
    to_store.save( );
    
    String world_label = to_store.getName( );
    
    MiraFileUtility.copies(
      Path.of( Bukkit.getWorldContainer( ).getAbsolutePath( ), world_label ),
      Path.of( repo_path, repo_label ),
      allow_overwrite,
      ( file->is_world_file( file ) || is_world_region_file( file ) ),
      ( MiraWorldUtility::is_world_directory ) );
  }
  
  /**
   * that world in the world repository ain't no good where it is.
   * move it into the server directory. we can use it then.
   *
   * @param repository_world_label we got the repo folder.
   * @param target_world_label     how bout that pesky world folder?
   */
  public static
  boolean remembers(
    @NotNull String repo_path,
    @NotNull String repository_world_label,
    @NotNull String target_world_label )
  throws IOException
  {
    File repo_file = new File( repo_path );
    
    if ( !repo_file.exists( ) )
    {
      return false;
    }
    
    /*pulse( ).plugin( ).log( "[worlds] copies repo world '%s' into local world '%s'...".formatted(
      repository_world_label,
      target_world_label ) );*/
    
    MiraFileUtility.copies(
      Path.of( repo_path, repository_world_label ),
      Path.of( Bukkit.getWorldContainer( ).getAbsolutePath( ), target_world_label ),
      false,
      ( file->is_world_file( file ) || is_world_region_file( file ) ),
      ( MiraWorldUtility::is_world_directory ) );
    
    return true;
  }
  
  /**
   * discarded like jj's last hyper-scope-creep changes before making the `git commit`.
   * forgotten even.
   *
   * @param world_label the world to restore, using the 5-digit ID.
   */
  public static
  void discards( @NotNull String world_label )
  throws IllegalStateException, IOException
  {
    //pulse( ).plugin( ).log( "[worlds] discards '%s'...".formatted( world_label ) );
    
    @Nullable World world = Bukkit.getWorld( world_label );
    
    if ( world != null )
    {
      MiraWorldUtility.unloads( world, false );
    }
    
    MiraFileUtility.deletes( new File(
      Bukkit.getWorldContainer( ).getAbsolutePath( ),
      world_label ) );
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
  static
  void unloads( @NotNull World victim, boolean lemme_save_rq )
  throws IllegalStateException
  {
    String world_label = victim.getName( );
    List<Player> evictees = victim.getPlayers( );
    
    // the world will not unload if there are any players still within it.
    /*pulse( ).plugin( ).log( "[worlds] evicts %d players(s) from '%s' and then unloads...".formatted(
      evictees.size( ),
      world_label ) );*/
    
    for ( Player evictee : evictees )
    {
      if ( !evictee.teleport( Bukkit.getWorlds( ).getFirst( ).getSpawnLocation( ) ) )
      {
        /*pulse( ).plugin( ).log( "[worlds] unable to evict %s from '%s', kicking...".formatted(
        world_label,
          evictee.getName( ) ) )*/
        evictee.kickPlayer(
          "we were unable to evict you from world '%s' during unloading, so we kicked you instead lol.".formatted(
            world_label ) );
      }
    }
    
    if ( !Bukkit.unloadWorld( victim, lemme_save_rq ) )
    {
      /*pulse( ).plugin( ).log( "[worlds] unable to unload '%s', attempting manual dereference...".formatted(
        world_label ) );*/
      
      try
      {
        Field worlds_field = CraftServer.class.getDeclaredField( "worlds" );
        worlds_field.setAccessible( true );
        
        ( ( Map<?, ?> ) worlds_field.get( Bukkit.getServer( ) ) ).remove( victim.getName( ).toLowerCase( ) );
        
        worlds_field.setAccessible( false );
      }
      catch ( ReflectiveOperationException exception )
      {
        //pulse( ).plugin( ).log( "[worlds] catastrophic failure; did you reflect correctly?" );
        
        throw new IllegalStateException( exception );
      }
    }
    
  }
}