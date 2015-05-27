package server;

/*
* @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
* @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
* @version 0.1
*/

import java.io.*;
import java.net.*;

public class UDPSender implements Runnable {
	int remotePort;
	InetAddress remoteAddress ;
	
	public UDPSender ( InetAddress remoteAddress , int remotePort ){ 
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
	}

  public void run() {    
    try {

      System.out.printf("Sending to remote host:%s:%d%n",
          remoteAddress , remotePort);     
      
      DatagramSocket serverSocket = new DatagramSocket();
      byte[] sendData = null;
      
      while(true)
      {

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,
            remoteAddress, remotePort);
        
        serverSocket.send(sendPacket);
      }
    } catch (IOException e) {
      System.out.println(e);
    }
  }  
}
