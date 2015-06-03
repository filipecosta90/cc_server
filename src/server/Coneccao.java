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

  // Tipos de pedido servidor
  public static final byte REPLY = 0;
  public static final byte HELLO = 1;
  public static final byte REGISTER = 2;
  public static final byte LOGIN = 3;
  public static final byte LOGOUT = 4;
  public static final byte QUIT = 5;
  public static final byte END = 6;
  public static final byte LIST_CHALLENGES = 7;
  public static final byte MAKE_CHALLENGE = 8;
  public static final byte ACCEPT_CHALLENGE = 9;
  public static final byte DELETE_CHALLENGE = 10;
  public static final byte ANSWER = 11;
  public static final byte RETRANSMIT = 12;
  public static final byte LIST_RANKING = 13;
  public static final byte INFO = 14;
  public static final int TAMANHO_MAX_PDU = 256;

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
  BasePdu replyPdu;

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
    else{
      this.resolvePacote( novoPdu );
    }
  }

  public void mergePacotesEspera ( BasePdu pacoteAFundir ){
    for ( BasePdu pduNaStack : this.stackEspera ){
      if ( pduNaStack.MesmaLabel(pacoteAFundir) ){
        pduNaStack.mergePDU(pacoteAFundir);
        if ( pduNaStack.pduCompleto()){
          this.resolvePacote( pduNaStack );
        }
      }
    }
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

  private void resolvePacote(BasePdu pduAResolver) {
    replyPdu = new BasePdu ( REPLY , pduAResolver.label ); 
    switch( pduAResolver.getTipo() ){
      case HELLO :
        {
          break;
        }
      case REGISTER :
        {
          break;
        }
      case LOGIN :
        {
          break;
        }
      case LOGOUT :
        {
          break;
        }
      case QUIT :
        {
          break;
        }
      case END :
        {
          break;
        }
      case LIST_CHALLENGES :
        {
          break;
        }
      case MAKE_CHALLENGE :
        {
          break;
        }
      case ACCEPT_CHALLENGE :
        {
          break;
        }
      case DELETE_CHALLENGE :
        {
          break;
        }
      case ANSWER :
        {
          break;
        }
      case RETRANSMIT :
        {
          break;
        }
      case LIST_RANKING :
        {
          break;
        }
    }
  }
}
