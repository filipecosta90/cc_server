/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;

public class Server implements Runnable, Serializable {

  private static final long serialVersionUID = -1135032786424499068L;

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

  public boolean  isThisSocketBound (  DatagramSocket socketCliente, InetAddress remoteAddress, int remotePort ){
    boolean isBound = false;
    if ( this.coneccoesActivas.containsKey( remoteAddress.toString() ) ){
      isBound = true;
    }
    return isBound;
  }

  public String whoAmI ( DatagramSocket socketCliente,  InetAddress remoteAddress, int remotePort ){
    String boundTo = new String();
    String key = remoteAddress.toString();
    if ( this.coneccoesActivas.containsKey( key ) ){
      boundTo = this.coneccoesActivas.get( key ).getAlcunhaClienteAssociado();
    }
    return boundTo;
  }

  public Coneccao getConeccaoCliente ( String alcunhaCliente ){
    Coneccao coneccaoRetornar = null;
    for ( Coneccao coneccaoPointer : this.coneccoesActivas.values() ){
      if ( coneccaoPointer.getAlcunhaClienteAssociado().equals(alcunhaCliente)){
        coneccaoRetornar = coneccaoPointer;
      }
    }
    return coneccaoRetornar; 
  }

  @Override
    public void run() {
      byte[] udpReceber;
      DatagramSocket udpSocket;
      DatagramPacket udpDataPacket;

      try {
        udpSocket = new DatagramSocket( listeningUDPPort );
        System.out.println( "Iniciado o listner UDP na porta: " + listeningUDPPort +"\n");
        while ( true ) {
          try {
            udpReceber = new byte[ ServerCodes.TAMANHO_MAX_PDU ];
            udpDataPacket = new DatagramPacket( udpReceber , udpReceber.length );
            udpSocket.receive( udpDataPacket );
            new Thread(new ServerResponder( this, udpSocket, udpDataPacket)).start();
          } catch ( Exception e ) {
            e.printStackTrace();
          }
        }
      } catch ( SocketException e ) {
        e.printStackTrace();
      }
    }

  public Coneccao getConeccao(DatagramSocket socketCliente , InetAddress remoteAddress, int remotePort) {
    Coneccao coneccaoRetornar;
    StringBuilder key = new StringBuilder();
    key.append( remoteAddress.toString() );
    coneccaoRetornar = this.coneccoesActivas.get(key.toString());
    return coneccaoRetornar;
  }

  public void adicionaConeccao(Coneccao coneccaoEstabelecida) {
    this.coneccoesActivas.put( coneccaoEstabelecida.getEnderecoRemoto().toString() , coneccaoEstabelecida );
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
  public void guardaServidor() throws Exception{
    // Write to disk with FileOutputStream
    FileOutputStream f_out = new 	FileOutputStream("servidor.data");
    // Write object with ObjectOutputStream
    ObjectOutputStream obj_out = new ObjectOutputStream (f_out);

    // Write object out to disk
    obj_out.writeObject ( this );
    obj_out.close();
  }

  public Server carregaServidor() throws Exception{
    FileInputStream f_in = new FileInputStream("servidor.data");
    // Read object using ObjectInputStream
    ObjectInputStream obj_in = new ObjectInputStream (f_in);
    // Read an object
    Server retornarServidor = (Server) obj_in.readObject();
    obj_in.close();
    return retornarServidor;
  }

  public String getStringUsers() {
    StringBuilder s = new StringBuilder();
    for (Cliente c1 : this.mapClientes.values()){
      s.append(c1.toString());
    }
    return s.toString();
  }

  public void adicionarUser() {
    // TODO Auto-generated method stub

  }

  public void alterarUser() {
    // TODO Auto-generated method stub

  }

  public void eliminarUser() {
    // TODO Auto-generated method stub

  }

}
