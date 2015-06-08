/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

package server;

import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerResponder implements Runnable , Serializable {

	private static final long serialVersionUID = -5039522701708707380L;
	
private transient DatagramSocket receivedSocket = null;
  private transient DatagramPacket receivedPacket = null;
  private InetAddress remoteAddress;
  private int remotePort;
  private Server localServerPointer;

  public ServerResponder( Server localServer , DatagramSocket receivedSocket, DatagramPacket receivedPacket) {
    this.localServerPointer = localServer;
    this.receivedSocket = receivedSocket;
    this.receivedPacket = receivedPacket;
    remoteAddress = receivedPacket.getAddress();
    remotePort = receivedPacket.getPort();
  }

  @Override
    public void run() {
      System.out.println("packet received from " + remoteAddress + ":"+remotePort );
      Coneccao coneccaoEstabelecida;
      if( this.localServerPointer.isThisSocketBound( receivedSocket , remoteAddress, remotePort)){
        System.out.println(">>>>>>coneccaoExistente: " + remoteAddress.toString());
        coneccaoEstabelecida = this.localServerPointer.getConeccao( receivedSocket , remoteAddress , remotePort );
        System.out.println("alcunha associado: "+ coneccaoEstabelecida.getAlcunhaClienteAssociado());
        try {
          coneccaoEstabelecida.verificaAlteraSeNecessarioSocket(this.receivedSocket , this.remotePort);
          coneccaoEstabelecida.adicionaPacote(this.receivedPacket);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      /* Nova coneccao */
      else {
        System.out.println("nova coneccao de: " + remoteAddress.toString());
        coneccaoEstabelecida = new Coneccao ( this.localServerPointer , this.receivedSocket , this.remoteAddress, this.remotePort );
        try {
          coneccaoEstabelecida.adicionaPacote(this.receivedPacket);
        } catch (Exception e) {
          e.printStackTrace();
        }
        this.localServerPointer.adicionaConeccao( coneccaoEstabelecida );
      }
    }
}
