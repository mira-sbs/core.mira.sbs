package sbs.mira.core.helper;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraModule;
import sbs.mira.core.MiraPulse;

import java.io.*;
import java.nio.file.Path;
import java.util.Objects;

public
class MiraFileHelper
  extends MiraModule<MiraPulse<?, ?>>
{
  /**
   *
   * @param pulse
   */
  public
  MiraFileHelper( @NotNull MiraPulse<?, ?> pulse )
  {
    super( pulse );
  }
  
  public final static String REGION_FILE_EXTENSION = ".mca";
  public final static String[] WORLD_FILE_NAMES = { "level.dat", "uid.dat" };
  
  /**
   * valid world data includes `.mca` and `.dat` files.
   * `.mca` files are hosted in the `region` directory.
   * other directories like `data` and `playerdata` are not useful.
   *
   * @return true - if this is a valid minecraft world file.
   */
  public
  boolean is_world_directory( File world_directory )
  {
    for ( String world_file_name : WORLD_FILE_NAMES )
    {
      if ( !new File( world_directory, world_file_name ).exists( ) )
      {
        return false;
      }
    }
    
    File region_directory = new File( world_directory, "region" );
    
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
  
  public
  boolean is_world_region_file( File world_region_file )
  {
    return world_region_file.exists( ) && world_region_file.isFile( ) && world_region_file.getName( ).endsWith(
      REGION_FILE_EXTENSION );
  }
  
  /***
   *
   * @param victim
   * @throws IOException
   * @throws IllegalArgumentException
   */
  public
  void deletes( @NotNull File victim ) throws IllegalArgumentException, IOException
  {
    if ( !victim.exists( ) )
    {
      throw new IllegalArgumentException( "someone already got to `%s`...".formatted( victim ) );
    }
    
    // directories get special treatment. (¬‿¬) (#^‿^#)
    if ( !victim.isDirectory( ) )
    {
      for ( File child_of_victim : Objects.requireNonNull( victim.listFiles( ) ) )
      {
        deletes( child_of_victim );
      }
    }
    
    FileUtils.forceDelete( victim );
  }
  
  /**
   * one does not simply copy a folder programmatically.
   *
   * @param source_path
   * @param target_path
   * @param allow_overwrite set to false if overwrites should raise an IllegalStateException.
   * @throws IOException              the file system fucked up.
   * @throws IllegalArgumentException u fucked up.
   * @throws IllegalStateException    trynna overwrite without consent are you?
   */
  public
  void copies( Path source_path, Path target_path, boolean allow_overwrite )
  throws IOException, IllegalArgumentException, IllegalStateException
  {
    File source = source_path.toFile( );
    File target = target_path.toFile( );
    
    if ( !source.exists( ) )
    {
      throw new IllegalArgumentException( "[files] '%s' is not a valid, existing file path".formatted( source ) );
    }
    
    if ( target.exists( ) )
    {
      if ( !allow_overwrite )
      {
        throw new IllegalStateException( "[files] cannot overwrite '%s' without explicit consent".formatted( target ) );
      }
    }
    
    if ( source.isDirectory( ) )
    {
      if ( !target.mkdir( ) )
      {
        throw new IOException( "[files] unable to create file at path '%s'".formatted( target ) );
      }
      
      for ( String source_child_file_name : Objects.requireNonNull( source.list( ) ) )
      {
        this.copies(
          source_path.resolve( source_child_file_name ),
          target_path.resolve( source_child_file_name ),
          allow_overwrite
        );
      }
    }
    else
    {
      InputStream in = new FileInputStream( source );
      OutputStream out = new FileOutputStream( target );
      byte[] buffer = new byte[ 1024 ];
      int length;
      while ( ( length = in.read( buffer ) ) > 0 )
      {
        out.write( buffer, 0, length );
      }
      in.close( );
      out.close( );
    }
  }
}
