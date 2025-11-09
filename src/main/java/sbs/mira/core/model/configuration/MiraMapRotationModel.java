package sbs.mira.core.model.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.core.MiraPulse;
import sbs.mira.core.model.MiraConfigurationModel;

import java.util.List;

public final
class MiraMapRotationModel<Pulse extends MiraPulse<?, ?>>
  extends MiraConfigurationModel<Pulse>
{
  private static final String CONFIG_RESOURCE_FILE_NAME = "rotation.yml";
  private static final String ROTATION_LIST_KEY = "rotation";
  
  @NotNull
  private final List<String> rotation;
  private int rotation_index;
  
  private boolean set_next_map;
  @Nullable
  private String set_next_map_label;
  
  public
  MiraMapRotationModel(
    @NotNull Pulse pulse )
  {
    super( pulse, CONFIG_RESOURCE_FILE_NAME );
    
    this.rotation = this.file_config.getStringList( ROTATION_LIST_KEY );
    this.rotation_index = 0;
    
    this.set_next_map = false;
    this.set_next_map_label = null;
  }
  
  @NotNull
  public
  String[] values( )
  {
    return this.rotation.toArray( new String[]{ } );
  }
  
  public
  int index( )
  {
    return this.rotation_index;
  }
  
  public
  void advance( )
  {
    if ( this.set_next_map )
    {
      this.set_next_map = false;
      this.set_next_map_label = null;
      
      return;
    }
    
    this.rotation_index++;
    
    if ( this.rotation_index >= this.rotation.size( ) )
    {
      this.rotation_index = 0;
    }
  }
  
  public
  boolean set_next_map( )
  {
    return this.set_next_map;
  }
  
  public
  void set_next_map( @Nullable String next_map_label )
  {
    if ( next_map_label == null )
    {
      this.set_next_map = false;
      this.set_next_map_label = null;
      
      return;
    }
    
    this.set_next_map = true;
    this.set_next_map_label = next_map_label;
  }
  
  @NotNull
  public
  String next_map_label( )
  {
    if ( this.set_next_map )
    {
      assert this.set_next_map_label != null;
      
      return this.set_next_map_label;
    }
    
    return this.rotation.get( this.rotation_index++ );
  }
}
