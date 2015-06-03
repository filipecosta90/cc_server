/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

package server;

import java.io.IOException;
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
    System.out.println("packet received from: " + remoteAddress );
    Coneccao coneccaoEstabelecida;
    if( this.localServerPointer.isThisSocketBound(remoteAddress, remotePort)){
      coneccaoEstabelecida = this.localServerPointer.getConeccao( remoteAddress , remotePort );
      try {
        coneccaoEstabelecida.adicionaPacote(this.receivedPacket);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    /* Nova coneccao */
    else {
      coneccaoEstabelecida = new Coneccao ( this.localServerPointer , this.receivedSocket , this.remoteAddress, this.remotePort );
      try {
        coneccaoEstabelecida.adicionaPacote(this.receivedPacket);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      this.localServerPointer.adicionaConeccao( coneccaoEstabelecida );
    }
  }
}
