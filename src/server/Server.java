/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

package server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.TreeMap;

public class Server implements Runnable {

  private int listeningUDPPort;
  private int listeningTCPPort;

  private HashMap< String , Cliente > mapClientes;
  private HashMap< String , Coneccao > coneccoesActivas;
  private HashMap< String , Coneccao > historicoConeccoes;
  private HashMap< String , DesafioManager > desafiosCriadosEspera;
  private HashMap< String , DesafioManager > desafiosEmJogo;
  private HashMap< String , DesafioManager > desafiosTerminados;
  public boolean mainServer;

  // Cr8tor

  public Server ( int udpPort, int tcpPort ){
    this.listeningUDPPort = udpPort;
    this.listeningTCPPort = tcpPort;
    this.mapClientes = new HashMap< String,Cliente > ();
    this.coneccoesActivas = new HashMap< String , Coneccao > ();
    this.historicoConeccoes = new HashMap< String , Coneccao > ();
    this.desafiosCriadosEspera = new HashMap< String , DesafioManager > ();
    this.desafiosEmJogo = new HashMap< String , DesafioManager > ();
    this.desafiosTerminados = new HashMap< String , DesafioManager > ();
    mainServer=true;
  }

  public boolean  isThisSocketBound ( InetAddress remoteAddress, int remotePort ){
    StringBuilder key = new StringBuilder();
    key.append( remoteAddress.toString() );
    key.append( remotePort);
    boolean isBound = false;
    if ( this.coneccoesActivas.containsKey( key ) ){
      isBound = true;
    }
    return isBound;
  }

  public String whoAmI ( InetAddress remoteAddress, int remotePort ){
    StringBuilder key = new StringBuilder();
    key.append( remoteAddress.toString() );
    key.append( remotePort);
    String boundTo = new String();
    if ( this.coneccoesActivas.containsKey( key ) ){
      boundTo = this.coneccoesActivas.get(key).getAlcunhaClienteAssociado();
    }
    return boundTo;
  }

  public void run() {
    byte[] udpReceber;
    DatagramSocket udpSocket;
    DatagramPacket udpDataPacket;

    try {
      udpReceber = new byte[ 256 ];
      udpSocket = new DatagramSocket( listeningUDPPort );
      udpDataPacket = new DatagramPacket( udpReceber , udpReceber.length );
      System.out.println( "\t UDP Listener started at port " + listeningUDPPort );

      while ( true ) {
        try {
          udpSocket.receive( udpDataPacket );
          new Thread(new ServerResponder( this, udpSocket, udpDataPacket)).start();
        } catch ( Exception e ) {
        }
      }
    } catch ( SocketException e ) {
    }
  }

  public Coneccao getConeccao(InetAddress remoteAddress, int remotePort) {
    Coneccao coneccaoRetornar;
    StringBuilder key = new StringBuilder();
    key.append( remoteAddress.toString() );
    key.append( remotePort);
    coneccaoRetornar = this.coneccoesActivas.get(key);
    return coneccaoRetornar;
  }

  public void adicionaConeccao(Coneccao coneccaoEstabelecida) {
    StringBuilder key = new StringBuilder();
    key.append( coneccaoEstabelecida.getEnderecoRemoto().toString() );
    key.append( coneccaoEstabelecida.getPortaRemota());
    this.coneccoesActivas.put( key.toString() , coneccaoEstabelecida );
  }

  public boolean registarCliente(String nome, String alcunha, String sec_info) {
    boolean resultado = false;
    if ( this.mapClientes.containsKey( alcunha ) ){
      resultado = false;
    }
    else {
      Cliente novoCliente = new Cliente ( nome , alcunha , sec_info );
      novoCliente.setLoggedIn();
      this.mapClientes.put( alcunha , novoCliente );
      resultado = true;
    }
    return resultado;
  }

  public boolean loginCliente(String alcunha, String sec_info) {
    boolean resultado = false;
    if ( this.mapClientes.containsKey( alcunha ) ){
      Cliente clientPointer = mapClientes.get(alcunha);
      if (clientPointer.checkAndSetLoggedIn(sec_info)){
        resultado = true;
      }
    }
    return resultado;
  }

  public boolean logoutCliente ( String alcunha ) {
    boolean resultado = false;
    if ( this.mapClientes.containsKey( alcunha ) ){
      Cliente clientPointer = mapClientes.get(alcunha);
      if (clientPointer.checkAndSetLoggedOut()){
        resultado = true;
      }
    }
    return resultado;
  }

  public Cliente getCliente ( String alcunha ){
    Cliente clientPointer = mapClientes.get(alcunha);
    return clientPointer;
  }

  public Desafio getDesafiosCriadosEmEspera(String nomeDesafio) {
    DesafioManager desafioManagerPointer = null;
    if ( this.desafiosCriadosEspera.containsKey( nomeDesafio ) ){
      desafioManagerPointer = desafiosCriadosEspera.get(nomeDesafio);
    }
    return desafioManagerPointer.getDesafio();
  }

  public boolean CriaDesafio(String nomeDesafio, Date dataHoraDesafio, String alcunhaClienteAssociado) {
    boolean resultado = false;
    if ( this.desafiosCriadosEspera.containsKey( nomeDesafio ) || this.desafiosEmJogo.containsKey( nomeDesafio ) || this.desafiosTerminados.containsKey( nomeDesafio ) ){
      resultado = false;
    }
    else {
      Date dataCriacao = new Date();
      Desafio novoDesafio = new Desafio ( nomeDesafio , alcunhaClienteAssociado , dataCriacao , dataHoraDesafio );
      new Thread(new DesafioManager ( this , novoDesafio )).start(); 
      resultado = true;
    }
    return resultado;
  }

  public ArrayList<Desafio> listaDesafiosEspera() {
    ArrayList <Desafio> listaDesafios = new ArrayList<Desafio> ();
    for ( DesafioManager desafioManagerPointer : this.desafiosCriadosEspera.values() ){
      listaDesafios.add( desafioManagerPointer.getDesafio().clone() );
    }
    return listaDesafios;
  }

  public HashMap< String , Cliente > getMapClientes(){
    HashMap< String , Cliente > mapRetornar = new HashMap< String , Cliente > ();
    for ( Cliente clienteRetornar : this.mapClientes.values() ){
      mapRetornar.put( clienteRetornar.getAlcunhaCliente() , clienteRetornar.clone() );
    }
    return mapRetornar;
  }

  public Desafio getDesafioTerminadoAssociadoCliente( String alcunhaClienteAssociado ) {
    // TODO Auto-generated method stub
    return null;
  }

  public TreeMap<String, Integer> getPontuacoesDesafioTerminado(String nomeDesafio) {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean ExisteDesafioEsperaEPodeAceitar(String nomeDesafio,
      String alcunhaClienteAssociado) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean AceitaDesafio(String nomeDesafio, String alcunhaClienteAssociado) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean DesafioPertenceCliente(String nomeDesafio,
      String alcunhaClienteAssociado) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean EliminaDesafio(String nomeDesafio, String alcunhaClienteAssociado) {
    // TODO Auto-generated method stub
    return false;
  }

  public Desafio getDesafioEliminado(String nomeDesafio) {
    // TODO Auto-generated method stub
    return null;
  }

}
