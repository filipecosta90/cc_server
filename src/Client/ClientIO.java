package Client;
import java.io.IOException;

import BusinessL.ServerIO;

public class ClientIO {
  private ServerIO sio;
  private ClientPduReader reader;

  public ClientIO( Console console )
  {
    this.sio=new ServerIO( this );
    Thread thread=new Thread( sio ); thread.start();
    this.reader=new ClientPduReader( console );
  }
  public void send( byte[] pdu ){ sio.recieve( pdu ); }
  public void receive( byte[] pdu ) throws IOException{ reader.byteRead( pdu ); }
}
