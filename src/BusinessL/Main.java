package BusinessL;
import UDPlistner.UDPlistner;

public class Main 
{
  public static UDPlistner udpl = new UDPlistner( "Server" );

  public static void main( String[] args ) throws Exception 
  {
    Thread thread = new Thread( udpl );
    thread.start();
  }
}
