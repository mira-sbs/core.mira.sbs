package sbs.mira.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

public
class MiraPluginTests
{
  
  private ServerMock server;
  private MiraMockPlugin plugin;
  
  @BeforeEach
  void set_up( )
  {
    MiraMockPulse pulse = new MiraMockPulse();
    
    this.server = MockBukkit.mock( );
    this.plugin = MockBukkit.load( MiraMockPlugin.class, pulse );
  }
  
  @AfterEach
  public
  void tear_down( )
  {
    MockBukkit.unmock( );
  }
  
  @Test
  public
  void test_1( )
  {
    assert true;
  }
}
