/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

public class Server implements Runnable {

  public enum EstadoServidor { INICIANDO_SERVIDOR , SERVIDOR_ACTIVO , PARANDO_SERVIDOR , SERVIDOR_PARADO , ERRO_SERVIDOR }

  private int listeningUDPPort;
  private int listeningTCPPort;

  private HashMap< String , Cliente > mapClientes;
  private HashMap< String , Coneccao > coneccoesActivas;
  private HashMap< String , Coneccao > historicoConeccoes;
  private HashMap< String , DesafioManager > desafiosCriadosEspera;
  private HashMap< String , DesafioManager > desafiosEmJogo;
  private HashMap< String , DesafioManager > desafiosTerminados;
  private HashMap< String , DesafioManager > desafiosEliminados;
  private String ficheiroMainDesafios;
  private ArrayList < String > nomesFicheirosDesafios;

  public boolean mainServer;

  // Construtores

  public Server ( int udpPort, int tcpPort , String ficheiroMainDesafiosPath ){
    this.listeningUDPPort = udpPort;
    this.listeningTCPPort = tcpPort;
    this.mapClientes = new HashMap< String,Cliente > ();
    this.coneccoesActivas = new HashMap< String , Coneccao > ();
    this.historicoConeccoes = new HashMap< String , Coneccao > ();
    this.desafiosCriadosEspera = new HashMap< String , DesafioManager > ();
    this.desafiosEmJogo = new HashMap< String , DesafioManager > ();
    this.desafiosTerminados = new HashMap< String , DesafioManager > ();
    this.desafiosEliminados = new HashMap< String , DesafioManager > ();
    this.ficheiroMainDesafios= ficheiroMainDesafiosPath;
    this.nomesFicheirosDesafios = new ArrayList < String > ();
    File file = new File(ficheiroMainDesafios);
    Scanner input;
    try {
      input = new Scanner(file);
      while(input.hasNext()) {
        String nomeFicheiroActual = input.next();
        this.nomesFicheirosDesafios.add( nomeFicheiroActual );
      }
      input.close();
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
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
  public Coneccao getConeccaoCliente ( String nomeCliente ){

    Coneccao coneccaoRetornar = null;
    for ( Coneccao coneccaoPointer : this.coneccoesActivas.values() ){
      if ( coneccaoPointer.getAlcunhaClienteAssociado().equals(nomeCliente)){
        coneccaoRetornar = coneccaoPointer;
      }
    }
    return coneccaoRetornar; 
  }

  public void run() {
    byte[] udpReceber;
    DatagramSocket udpSocket;
    DatagramPacket udpDataPacket;

    try {
      udpReceber = new byte[ Coneccao.TAMANHO_MAX_PDU ];
      udpSocket = new DatagramSocket( listeningUDPPort );
      udpDataPacket = new DatagramPacket( udpReceber , udpReceber.length );
      System.out.println( "\t Iniciado o listner UDP na porta: " + listeningUDPPort );

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

  public Coneccao getConeccaoAssociada ( String alcunha ){
    Coneccao coneccaoAssociada = null;
    for ( Coneccao coneccaoActual : this.coneccoesActivas.values() ){
      if ( coneccaoActual.getAlcunhaClienteAssociado().equals(alcunha)){
        coneccaoAssociada = coneccaoActual;
      }
    }
    return coneccaoAssociada;
  }

  public boolean logoutCliente ( String alcunha ) {
    boolean resultado = false;
    if ( this.mapClientes.containsKey( alcunha ) ){
      Cliente clientPointer = mapClientes.get(alcunha);
      if (clientPointer.checkAndSetLoggedOut()){
        Coneccao coneccaoPointer = getConeccaoAssociada ( alcunha);
        coneccaoPointer.terminaConeccao();
        this.coneccoesActivas.remove(coneccaoPointer);
        this.historicoConeccoes.put(coneccaoPointer.getKey() , coneccaoPointer);
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
      Collections.shuffle(nomesFicheirosDesafios);
      String nomeFicheiroPerguntasRandom = nomesFicheirosDesafios.get(0);
      Desafio novoDesafio = new Desafio ( nomeDesafio , alcunhaClienteAssociado , dataCriacao , dataHoraDesafio , nomeFicheiroPerguntasRandom );
      DesafioManager desafioM = new DesafioManager ( this , novoDesafio );
      this.desafiosCriadosEspera.put(nomeDesafio, desafioM);
      new Thread(desafioM).start(); 
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
    Desafio desafioRetornar = null;
    boolean alreadyFound = false;
    for( DesafioManager desafioManagerPointer : this.desafiosTerminados.values() ){
      if ( desafioManagerPointer.getDesafio().clienteParticipa( alcunhaClienteAssociado ) && alreadyFound == false){
        desafioRetornar = desafioManagerPointer.getDesafio();
        alreadyFound = true;
      }
    }
    return desafioRetornar;
  }

  public TreeMap<String, Integer> getPontuacoesDesafioTerminado(String nomeDesafio) {
    TreeMap<String, Integer> scoreBoard = null;
    DesafioManager desafioManagerPointer = this.desafiosTerminados.get(nomeDesafio);
    if( desafioManagerPointer != null ){
      scoreBoard = desafioManagerPointer.getDesafio().getPontuacoesJogadores();
    }
    return scoreBoard;
  }

  public boolean ExisteDesafioEsperaEPodeAceitar(String nomeDesafio , String alcunhaClienteAssociado) {
    boolean resultado = false;
    if ( this.desafiosCriadosEspera.containsKey(nomeDesafio)){
      DesafioManager desafioManagerPointer = this.desafiosCriadosEspera.get(nomeDesafio);
      if ( desafioManagerPointer.getDesafio().podeAdicionarJogador( alcunhaClienteAssociado )){
        resultado = true;
      }
    }
    return resultado;
  }

  public boolean AceitaDesafio(String nomeDesafio, String alcunhaClienteAssociar ) {
    boolean resultado = false;
    DesafioManager desafioManagerPointer = this.desafiosCriadosEspera.get(nomeDesafio);
    if( desafioManagerPointer != null ){
      resultado = desafioManagerPointer.aceitaDesafio( alcunhaClienteAssociar );
    }
    return resultado;
  }

  public boolean DesafioPertenceCliente(String nomeDesafio, String alcunhaClienteAssociado) {
    boolean resultado = false;
    DesafioManager desafioManagerPointer = this.desafiosCriadosEspera.get(nomeDesafio);
    if( desafioManagerPointer != null ){
      if ( desafioManagerPointer.getDesafio().getCriadoPor().equals(alcunhaClienteAssociado)){
        resultado = true;
      }
    }
    return resultado;
  }

  public boolean EliminaDesafio(String nomeDesafio, String alcunhaClienteAssociado) {
    boolean resultado = false;
    DesafioManager desafioManagerPointer = this.desafiosCriadosEspera.get(nomeDesafio);
    if( desafioManagerPointer != null ){
      if ( desafioManagerPointer.getDesafio().getCriadoPor().equals(alcunhaClienteAssociado)){
        desafioManagerPointer.getDesafio().elimina();
        this.desafiosCriadosEspera.remove(nomeDesafio);
        this.desafiosEliminados.put( nomeDesafio , desafioManagerPointer);
        resultado = true;
      }
    }
    return resultado;
  }

  public Desafio getDesafioEliminado(String nomeDesafio) {
    DesafioManager desafioManagerPointer = this.desafiosCriadosEspera.get(nomeDesafio);
    Desafio desafioRetornar = null;
    if( desafioManagerPointer != null ){
      desafioRetornar = desafioManagerPointer.getDesafio();
    }  
    return desafioRetornar;
  }

  public void TerminaDesafio(String nomeDesafio, String criadoPor) {
    // TODO Auto-generated method stub

  }

  public Desafio getDesafioDecorrerAssociadoCliente(String alcunhaClienteAssociado) {
    Desafio desafioPointer = null;
    for ( DesafioManager desafioMPointer : this.desafiosEmJogo.values() ){
      if ( desafioMPointer.getDesafio().clienteParticipa(alcunhaClienteAssociado)){
        desafioPointer = desafioMPointer.getDesafio();
      }
    }
    return desafioPointer;
  }

}
