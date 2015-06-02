package server;

/*
* @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
* @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
* @version 0.1
*/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class ServerIO implements Runnable
{
  private ServerPduReader reader;
  private Server server;
  
  
  @Override
    public synchronized void run()
    {
      while( true )
        try{ wait(); }
      catch( InterruptedException e ){}
    }

  // IO functions
  
  public void send( byte[] pdu ,  DatagramSocket outSocket , InetAddress remoteAddress, int remotePort )
  { 
    try 
    {
    	  DatagramPacket sendPacket = new DatagramPacket( pdu , pdu.length, remoteAddress, remotePort);
    	  outSocket.send(sendPacket);
    } catch( IOException e ) {
      System.out.println( e.toString() );
    }
  }
}
