/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;

public class Coneccao {
  String nomeClienteAssociado;
  private ArrayList < BasePdu > stackEspera;
  private ArrayList < BasePdu > historialPdus;
  private Server localServerPointer;
  Date timeStampInicio;
  Date timeStampAlteracao;
  Date timeStampFim;
  private boolean anonima;
  DatagramSocket boundedSocket;
  InetAddress enderecoLigacao;
  int portaRemota;

  public Coneccao( Server localServer , DatagramSocket inSocket , InetAddress remoteAddress , int remotePort ){
    stackEspera = new ArrayList < BasePdu > ();
    historialPdus = new ArrayList < BasePdu > ();
    this.localServerPointer = localServer;
    timeStampInicio = new Date();
    anonima = true;
    boundedSocket = inSocket;
    enderecoLigacao = remoteAddress;
    portaRemota = remotePort;
  }

  public void adicionaPacote ( DatagramPacket novoPacote ){
    BasePdu novoPdu = new BasePdu ( novoPacote );
    novoPdu.parseCabecalho();
    novoPdu.parseCampos();
    if(novoPdu.dadosParciais()){
      boolean query = parteDePduEmEspera( novoPdu );
      if ( query == true ){
        mergePacotesEspera( novoPdu );
      }
      else{
        this.stackEspera.add( novoPdu );
      }
    }
  }

  public void mergePacotesEspera ( BasePdu pacoteAFundir ){
    for ( BasePdu pduNaStack : this.stackEspera ){
      if ( pduNaStack.MesmaLabel(pacoteAFundir) ){
        pduNaStack.mergePDU(pacoteAFundir);
        if ( pduNaStack.pduCompleto()){
          this.resolvePacote( pduNaStack );
          this.respondeRemoto( pduNaStack );
        }
      }
    }
  }

  private void respondeRemoto(BasePdu pduNaStack) {
    // TODO Auto-generated method stub

  }

  private void resolvePacote(BasePdu pduNaStack) {
    // TODO Auto-generated method stub

  }

  public boolean parteDePduEmEspera( BasePdu pacoteATestar ){
    boolean resultado = false;
    for ( BasePdu pduNaStack : this.stackEspera ){
      if ( pduNaStack.MesmaLabel(pacoteATestar) ){
        resultado = true;
      }
    }
    return resultado;
  }

  public String getNomeClienteAssociado(){
    return nomeClienteAssociado;
  }

  public InetAddress getEnderecoRemoto() {
    return this.enderecoLigacao;
  }

  public int getPortaRemota() {
    return this.portaRemota;
  }
}
