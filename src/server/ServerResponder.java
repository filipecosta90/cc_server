package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ServerResponder implements Runnable {

  DatagramSocket socket = null;
  DatagramPacket packet = null;
  Server localServerPointer;

  public ServerResponder( Server localServer , DatagramSocket socket, DatagramPacket packet) {
    this.localServerPointer = localServer;
    this.socket = socket;
    this.packet = packet;
  }

  public void run() {
    //byte[] data = makeResponse(); // code not shown
    /*DatagramPacket response = new DatagramPacket(data, data.length,
      packet.getAddress(), packet.getPort());
      socket.send(response);*/
    System.out.println("packet received:" + packet.toString());
  }
}
