package server;

/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

public class Server implements Runnable {

  private int listeningUDPPort;
  private int listeningTCPPort;

  private ArrayList< Pergunta > perguntas;
  private HashMap< String,Cliente > clientes;
  private HashMap< String , String > activeConnections;
  private HashMap< String , String > clientGameBounds;
  private HashMap< String,Jogo > jogos;
  private Hashtable< String,Jogo > jogados;

  private ServerPduReader pduReader;

  // Cr8tor

  public Server ( int udpPort, int tcpPort ){
    this.listeningUDPPort = udpPort;
    this.listeningTCPPort = tcpPort;
    this.setPduReader(new ServerPduReader ( this ));
  }

  // Account Management
  public boolean registar( String nome ,String alcunha ,byte[] sec_info ,  InetAddress remoteAddress, int remotePort)
  {
    if( this.clientes.containsKey( alcunha )){
      return false;
      //  io.send( builder.REPLY_ERRO( ( short )-1,nick ) ,  );
    }
    else
    {
      Cliente client = new Cliente( nome , alcunha , sec_info );
      clientes.put( alcunha , client );
      return true;
      // io.send( builder.REPLY_ALCUNHA( newID,nick ) );
    }	
  }

  public boolean  isThisSocketBound ( InetAddress remoteAddress, int remotePort ){
    StringBuilder key = new StringBuilder();
    key.append( remoteAddress.toString() );
    key.append( remotePort);
    boolean isBound = false;
    if ( this.activeConnections.containsKey( key ) ){
      isBound = true;
    }
    return isBound;
  }

  public String whoAmI ( InetAddress remoteAddress, int remotePort ){
    StringBuilder key = new StringBuilder();
    key.append( remoteAddress.toString() );
    key.append( remotePort);
    String boundTo = new String();
    if ( this.activeConnections.containsKey( key ) ){
      this.activeConnections.get(key);
    }
    return boundTo;
  }


  public boolean login( String alcunha , byte[] sec_info , InetAddress remoteAddress, int remotePort  )
  {
    if( !this.clientes.containsKey( alcunha ))
    {
      //io.send( builder.REPLY_ERRO( ( short )-2,nick ) );
      return false;
    }
    else
    {
      Cliente cl=this.clientes.get( alcunha );
      if( cl.login( sec_info )){
        StringBuilder key = new StringBuilder();
        key.append( remoteAddress.toString() );
        key.append( remotePort);
        this.activeConnections.put( key.toString() , alcunha);
        return true;
        //io.send( builder.REPLY_NOME( cl.getId() , nick ));
      }
      else{
        return false;
        // io.send( builder.REPLY_ERRO( ( short )-1, nick ) );
      }
    }
  }

  public boolean logout( String alcunha , InetAddress remoteAddress, int remotePort )
  {
    if( this.clientes.containsKey( alcunha ))
    {
      this.clientes.get( alcunha ).logout();
      StringBuilder key = new StringBuilder();
      key.append( remoteAddress.toString() );
      key.append( remotePort);
      this.activeConnections.remove(key.toString());
      //io.send( builder.REPLY_OK( id ));
      return true;
    }
    else {
      return false;
    }
  }

  // Challenges Management
  public void list_Challenges( InetAddress remoteAddress, int remotePort ){ 
    // io.send( builder.REPLY( id,jogos )); 
  }

  public void make_Desafio( String alcunha , String nomeDesafio, byte[] data, byte[] hora , InetAddress remoteAddress, int remotePort ){
    if( this.jogos.containsKey( nomeDesafio )){
      // io.send( builder.REPLY_ERRO( id,"Desafio ja existe." ) );
    }
    else{
      // io.send( builder.REPLY_DESAFIO( id,desafio ));
    }
  }

  public boolean aceitar_Desafio( String alcunha ,String nomeDesafio,  InetAddress remoteAddress, int remotePort )
  {
    if( !this.jogos.containsKey( nomeDesafio )){
      return false;
      //  io.send( builder.REPLY_ERRO( id,"Desafio nao existe." ));
    }
    else{
      Jogo j=this.jogos.get( nomeDesafio );
      j.adicionaJogador ( alcunha );
      return true;
    }
  }

  public void delete_Challange( String alcunha ,String nomeDesafio , InetAddress remoteAddress, int remotePort ){

    if( !this.jogos.containsKey( nomeDesafio )){
      //io.send( builder.REPLY_ERRO( id,"Desafio nao existe." ));
    }
    else{
      this.jogos.remove( nomeDesafio );
      // io.send( builder.REPLY_DESAFIO( id,desafio ));
    }
  }

  public String listar_clientes( )
  {
    StringBuilder s = new StringBuilder();
    s.append( ": Clientes :\n" );
    for( Cliente cliente : clientes.values() )
      s.append( cliente.toString() );
    return s.toString();
  }

  public void add_Games()
  {
    this.perguntas.add( new Pergunta( 
          "0000001.mp3","000001.jpg",new String[]{"Quem canta esta cansao?","Robert Smith","Bono Vox","Ninguem, e uma musica instrumental"},3 ));
    this.perguntas.add( new Pergunta( 
          "0000002.mp3","000002.jpg",new String[]{"Relacione esta can��o com uma das seguintes palavras:","Microsoft","Adobe","Linux"},2 ));
    this.perguntas.add( new Pergunta( 
          "0000003.mp3","000003.jpg",new String[]{"Qual destas palavras n�o aparece no poema deste fado?","Lamento","Tormento","Momento"},3 ));
    this.perguntas.add( new Pergunta( 
          "0000004.mp3","000004.jpg",new String[]{"Qual destes pa�ses n�o tem rela��o com o cantor desta can��o?","Portugal","Ir�o","Inglaterra"},3 ));
    this.perguntas.add( new Pergunta( 
          "0000005.mp3","000005.jpg",new String[]{"Que outra arte � referida no poema desta can��o?","Pintura","Escultura","Literatura"},1 ));
    this.perguntas.add( new Pergunta(
          "0000006.mp3","000006.jpg",new String[]{"Em que d�cada do s�culo passado foi publicada esta can��o?","D�cada de 60","D�cada de 70","D�cada de 80"},2 ));
    this.perguntas.add( new Pergunta( 
          "0000007.mp3","000007.jpg",new String[]{"Quem escreveu a letra desta can��o?","Carlos Fausto Dias","Guzzetti Bordalo","Correia de Oliveira"},1 ));
    this.perguntas.add( new Pergunta( 
          "0000008.mp3","000008.jpg",new String[]{"De que �pera � esta can��o?","Otelo","La boh�me","The fairy queen"},3 ));
    this.perguntas.add( new Pergunta(
          "0000009.mp3","000009.jpg",new String[]{"Como se chama este trecho musical?","Glass","Metamorphosis","Mad Rush"},3 ));
    this.perguntas.add( new Pergunta( 
          "0000010.mp3","000010.jpg",new String[]{"Este concerto foi gravado em que cidade?","Col�nia","Viena","Paris"},1 ));
  }

  public void ask( int question,short p1,short p2 )
  {
    Pergunta p=perguntas.get( question );
    //  io.send( builder.REPLY_QUESTAO( p1,question,p.get_pergunta(),p.get_ops() ));
    //io.send( builder.REPLY_QUESTAO( p1,question,p.get_pergunta(),p.get_ops() ));
  }

  public void answer( String alcunha , int escolha , String desafio , int questao,  InetAddress remoteAddress, int remotePort )
  {
    if( perguntas.get( questao ).is_Certa( escolha ) ){
      // io.send( builder.REPLY_CERTA( id,1 ));
      // jogos.get( desafio ).right_Answer( id );
    }
    else{ 
      //io.send( builder.REPLY_CERTA( id,0 ));
      //Pergunta p=perguntas.get( questao );
      //io.send( builder.REPLY_QUESTAO( id,questao,p.get_pergunta(),p.get_ops() ));
    }
  }

  public void end( String alcunha ,  InetAddress remoteAddress, int remotePort)
  {
    /*
       Jogo J=jogos.get( desafio );
       jogados.put( desafio,J );
       jogos.remove( desafio );
       short[] scores=J.end( id );

*/
  }

  public int[] rand_perguntas(){ return new int[]{ 1,2,3 }; }

  public Pergunta get_Pergunta( int num ){ return this.perguntas.get( num ); }

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

  public ServerPduReader getPduReader() {
    return pduReader;
  }

  public void setPduReader(ServerPduReader pduReader) {
    this.pduReader = pduReader;
  }

  public void retransmit(String nomeDesafio, int questao, int bloco,
      InetAddress remoteAddress, int remotePort) {
    // TODO Auto-generated method stub
  }

  public void hello(InetAddress remoteAddress, int remotePort) {
    // TODO Auto-generated method stub
  }

  public void quit(String alcunha, InetAddress remoteAddress, int remotePort) {
    // TODO Auto-generated method stub
  }

  public void listRanking(InetAddress remoteAddress, int remotePort) {
    // TODO Auto-generated method stub
  }
}
