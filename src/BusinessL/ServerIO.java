package BusinessL;

import java.io.IOException;

import Client.ClientIO;

public class ServerIO implements Runnable
{
  private ServerPduReader reader;
  private Server server;
  private ClientIO cio;
  /* Client Recognition through IpAdress */
  // private HashMap< String,String >clientIp;
  // Cr8tor
  public ServerIO( ClientIO cio )
  {
    this.cio = cio;
    this.server=new Server( this );
    this.reader=new ServerPduReader( server );		
    //this.clientIp=new HashMap< String,String >();
  }

  @Override
    public synchronized void run()
    {
      while( true )
        try{ wait(); }
      catch( InterruptedException e ){}
    }

  // IO functions
  public void recieve( byte[] pdu ){ reader.byteRead( pdu ); }
  public void send( byte[] pdu )
  { 
    try 
    {
      cio.receive( pdu );
    } catch( IOException e ) {
      System.out.println( e.toString() );
    }
  }
}
