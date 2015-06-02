package server;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;

/*
* @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
* @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
* @version 0.1
*/



public class ServerPduBuilder 
{
	ByteArrayOutputStream outputStream;
protected DatagramPacket outPacket;
	
  public ServerPduBuilder(){
	outputStream = new ByteArrayOutputStream();
  }
  
  // Calls
  
  public void setRemote ( InetAddress remoteIp ){
	  
  }
  
  public byte[] udpReplyOk ( ){
	  byte[] campo = new byte[0];
	  campo[0] = 0;
	  return campo;
  }
  
 
}
