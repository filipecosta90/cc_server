package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerResponder implements Runnable {

  DatagramSocket receivedSocket = null;
  DatagramPacket receivedPacket = null;
  InetAddress remoteAddress;
  int remotePort;
  
  Server localServerPointer;

  public ServerResponder( Server localServer , DatagramSocket receivedSocket, DatagramPacket receivedPacket) {
    this.localServerPointer = localServer;
    this.receivedSocket = receivedSocket;
    this.receivedPacket = receivedPacket;
    remoteAddress = receivedPacket.getAddress();
    remotePort = receivedPacket.getPort();
  }

  public void run() {
	  byte[] received = receivedPacket.getData();
	  localServerPointer.getPduReader().byteRead( received , receivedSocket , remoteAddress , remotePort );

      String sentence = new String( received , 0, receivedPacket.getLength() );
    System.out.println("packet received:" + sentence );
  }
}
