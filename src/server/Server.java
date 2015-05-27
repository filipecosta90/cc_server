package server;

/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

public class Server implements Runnable {

  private int listeningUDPPort;
  private int listeningTCPPort;

  private ArrayList< Pergunta >perguntas;
  private HashMap< String,Cliente >clientes;
  private String[] nicks=new String[ 1000 ];
  private HashMap< String,Jogo >jogos;
  private Hashtable< String,Jogo >jogados=new Hashtable< String,Jogo >();
  private ServerPduBuilder builder;
  private ServerIO io;
  private Counter counter=new Counter();

  // Cr8tor

  public Server ( int udpPort, int tcpPort ){
    this.listeningUDPPort = udpPort;
    this.listeningTCPPort = tcpPort;
  }

  public Server( ServerIO io )
  {
    this.builder=new ServerPduBuilder();
    this.jogos=new HashMap< String,Jogo >();
    this.clientes=new HashMap< String,Cliente >();
    this.perguntas=new ArrayList< Pergunta >();
    add_Games();
    this.io = io;
    // Admin account
    Cliente admin=new Cliente( "admin",( short )0,"admin",( ""+'\0' ).getBytes() );
    nicks[ 0 ]="admin";
    this.clientes.put( ( "admin" ),admin );
  }
  // Account Management
  //public void helLo(){ io.send( builder.REPLY_OK( -1 )); }
  public void registar( String name,String nick,byte[] pass )
  {
    if( this.clientes.containsKey( nick ))
      io.send( builder.REPLY_ERRO( ( short )-1,nick ) );
    else
    {
      short newID=counter.genID();
      nicks[ newID ]=nick;
      Cliente client = new Cliente( name,newID,nick,pass );
      clientes.put( nick,client );
      io.send( builder.REPLY_ALCUNHA( newID,nick ) );
    }	
  }

  public void login( String nick,byte[] pass )
  {
    if( !this.clientes.containsKey( nick ))
    {
      io.send( builder.REPLY_ERRO( ( short )-2,nick ) );
    }
    else
    {
      Cliente cl=this.clientes.get( nick );
      if( cl.login( pass ))
        io.send( builder.REPLY_NOME( cl.getId(),nick ));
      else
        io.send( builder.REPLY_ERRO( ( short )-1,nick ) );
    }
  }

  public void logout( short id )
  {
    if( this.clientes.containsKey( nicks[ id ] ))
    {
      this.clientes.get( nicks[ id ] ).logout();
      io.send( builder.REPLY_OK( id ));
    }
  }

  // Challenges Management
  public void list_Challenges( short id ){ io.send( builder.REPLY( id,jogos )); }

  public void make_Desafio( short id,String desafio,short[] time )
  {
    if( this.jogos.containsKey( desafio ))
      io.send( builder.REPLY_ERRO( id,"Desafio ja existe." ) );
    else
    {

      Jogo jogo=new Jogo( desafio,id,time );
      this.jogos.put( desafio,jogo );
      System.out.println( jogos.get( desafio ).toString() );
      io.send( builder.REPLY_DESAFIO( id,desafio ));
    }
  }
  public void aceitar_Desafio( short id,String desafio )
  {
    if( !this.jogos.containsKey( desafio ))
      io.send( builder.REPLY_ERRO( id,"Desafio nao existe." ));
    else
    {
      Jogo j=this.jogos.get( desafio );
      j.add_player2( id );
      JogoThread jThread=new JogoThread( this,j );
      j.setThread( jThread );
      Thread thread=new Thread( jThread );
      thread.start();
    }
  }
  public void delete_Challange( short id,String desafio )
  {
    if( !this.jogos.containsKey( desafio ))
      io.send( builder.REPLY_ERRO( id,"Desafio nao existe." ));
    else
    {
      this.jogos.remove( desafio );
      io.send( builder.REPLY_DESAFIO( id,desafio ));
    }
  }

  public String listar_clientes()
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
    io.send( builder.REPLY_QUESTAO( p1,question,p.get_pergunta(),p.get_ops() ));
    //io.send( builder.REPLY_QUESTAO( p1,question,p.get_pergunta(),p.get_ops() ));
  }

  public void answer( short id,int questao,int resposta,String desafio )
  {
    if( perguntas.get( questao ).is_Certa( resposta ) )
    {
      io.send( builder.REPLY_CERTA( id,1 ));
      jogos.get( desafio ).right_Answer( id );
    }
    else
    { 
      io.send( builder.REPLY_CERTA( id,0 ));
      Pergunta p=perguntas.get( questao );
      io.send( builder.REPLY_QUESTAO( id,questao,p.get_pergunta(),p.get_ops() ));
    }
  }

  public void end( short id,String desafio )
  {
    Jogo J=jogos.get( desafio );
    jogados.put( desafio,J );
    jogos.remove( desafio );
    short[] scores=J.end( id );

    io.send( builder.REPLY_SCORE( id,scores[ 1 ] ));
    //io.send( builder.REPLY_SCORE( scores[ 0 ],scores[ 2 ] ));
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
}
