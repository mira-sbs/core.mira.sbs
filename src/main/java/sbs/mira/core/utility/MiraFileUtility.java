package sbs.mira.core.utility;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Predicate;

public final
class MiraFileUtility
{
  /***
   * forcefully and recursively deletes a path from the file system.
   *
   * @param victim the path (represented as a `File`) to be deleted.
   * @throws FileNotFoundException the path does not exist.
   * @throws IOException an i/o operation failed - check message.
   */
  public static
  void deletes( @NotNull File victim )
    throws FileNotFoundException, IOException
  {
    if ( !victim.exists( ) )
    {
      throw new FileNotFoundException( "[files] someone already got to '%s'...".formatted( victim ) );
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
   * one does not simply copy a folder recursively **and** programmatically.
   *
   * @param source_path               the file or directory path to be copied recursively - including children.
   * @param target_path               the destination path of the copied file or directory - name can be different.
   * @param allow_overwrite           set to false if overwrites should raise an IllegalStateException.
   * @param valid_file_predicate      only these files should be copied.
   * @param valid_directory_predicate only these directories should be copied.
   * @throws FileNotFoundException    bro where did u put the file?
   * @throws IOException              the file system fucked up.
   * @throws IllegalArgumentException u fucked up.
   * @throws IllegalStateException    trynna overwrite without consent are you?
   */
  public static
  void copies(
    Path source_path,
    Path target_path,
    boolean allow_overwrite,
    @Nullable Predicate<File> valid_file_predicate,
    @Nullable Predicate<File> valid_directory_predicate
  )
    throws IOException, IllegalArgumentException, IllegalStateException
  {
    File source = source_path.toFile( );
    File target = target_path.toFile( );
    
    if ( !source.exists( ) )
    {
      throw new FileNotFoundException( "[files] failed to copy '%s' as it does not exist".formatted(
        source ) );
    }
    
    if ( target.exists( ) )
    {
      if ( !allow_overwrite )
      {
        throw new IllegalArgumentException(
          "[files] unintentional overwrite at '%s' when disallowed.".formatted( target ) );
      }
    }
    
    if ( source.isDirectory( ) )
    {
      if ( valid_directory_predicate != null && valid_directory_predicate.test( source ) )
      {
        return;
      }
      
      if ( !target.mkdir( ) )
      {
        throw new IOException( "[files] unable to create file at path '%s'".formatted( target ) );
      }
      
      for ( String source_child_file_name : Objects.requireNonNull( source.list( ) ) )
      {
        copies(
          source_path.resolve( source_child_file_name ),
          target_path.resolve( source_child_file_name ),
          allow_overwrite,
          valid_file_predicate,
          valid_directory_predicate
        );
      }
    }
    else
    {
      if ( valid_file_predicate != null && valid_file_predicate.test( source ) )
      {
        return;
      }
      
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
  
  public static
  void copies( Path source_path, Path target_path, boolean allow_overwrite )
    throws IOException, IllegalArgumentException, IllegalStateException
  {
    MiraFileUtility.copies( source_path, target_path, allow_overwrite, null, null );
  }
}
