package server;

/*
* @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
* @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
* @version 0.1
*/

import java.io.IOException;

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
