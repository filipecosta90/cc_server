package server;

/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

import java.io.ByteArrayInputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class ServerPduReader {
  private Server serverPointer;
  
  // Constructor
  public ServerPduReader( Server server ){ 
    this.serverPointer=server; 
  }

  public void byteRead( byte[] receivedBytes , DatagramSocket receivedSocket , InetAddress remoteAddress , int remotePort ){

    ByteArrayInputStream inputByteArray = new ByteArrayInputStream( receivedBytes );
    int totalLength = inputByteArray.available();
    byte version[] = new byte[1];
    byte security[] = new byte[1];
    byte[] label = new byte[2];
    byte type[] = new byte[1];
    byte[] followingFieldsSize_bytes =  new byte[2];
    byte numberFollowingFields[] = new byte[1];

    inputByteArray.read(version , 0 , 1 );
    inputByteArray.read( security , 0 , 1);
    inputByteArray.read(label, 0, 2);
    inputByteArray.read( type , 0 , 1);
    inputByteArray.read( numberFollowingFields , 0 , 1);
    inputByteArray.read( followingFieldsSize_bytes , 0 , 2);
    Byte Decimal = new Byte (followingFieldsSize_bytes[0]);
    Byte Unit = new Byte (followingFieldsSize_bytes[1]);
    int followingFieldsSize = Decimal.intValue() * 10 + Unit.intValue();
    Byte type_Byte = new Byte ( type[0] );
    int tipo = type_Byte.intValue();
    byte[] fields_bytes = new byte[followingFieldsSize];
    ByteArrayInputStream inputByteFields = new ByteArrayInputStream( fields_bytes );
    if ( followingFieldsSize > 0 ) {
      inputByteArray.read( fields_bytes , 0 ,  followingFieldsSize );
    }

    System.out.print( "\ttotal Length: " + totalLength );
    System.out.print( "\tversion: "+ version[0] );
    System.out.println( "\tsecurity: "+ security[0] );
    System.out.println( "\tlabel: "+ label[0]+label[1] );
    System.out.print( "\ttype: "+ type[0] );
    System.out.println( "\tnumber of following fields: "+ numberFollowingFields[0] );
    System.out.println( "\tfollowing fields size: "+ followingFieldsSize );

    switch( tipo ){
      case 1 :
        {
          System.out.println( "- HELLO -" );
          serverPointer.hello( remoteAddress, remotePort );
          break;
        }
      case 2 :
        {
          System.out.println( "- REGISTER -" );
          ArrayList < String > campos = byteToString( fields_bytes , 2 , 0 );
          String nome = campos.get( 0 );
          String alcunha = campos.get( 1 );
          int tamanhoNomeEAlcunha = nome.length() + alcunha.length() + 2;
          int tamanhoSec = followingFieldsSize - tamanhoNomeEAlcunha;
          byte[] sec_info = new byte[tamanhoSec];
          inputByteFields.read(sec_info, tamanhoNomeEAlcunha, tamanhoSec);
          serverPointer.registar( nome , alcunha , sec_info , remoteAddress, remotePort );
          break;
        }
      case 3 : 
        {
          System.out.println( "- LOGIN -" );
          ArrayList< String > campos = byteToString( fields_bytes , 1 , 0 );
          String alcunha = campos.get ( 0 );
          int tamanhoAlcunha = alcunha.length() + 1;
          int tamanhoSec = followingFieldsSize - tamanhoAlcunha;
          byte[] sec_info = new byte[tamanhoSec];
          inputByteFields.read(sec_info, tamanhoAlcunha, tamanhoSec);
          serverPointer.login( alcunha , sec_info , remoteAddress , remotePort );
          break;
        }
      case 4 : 
        {
          System.out.println( "- LOGOUT -" );
          boolean answer = serverPointer.isThisSocketBound (  remoteAddress , remotePort );
          if ( answer == true ){
            String alcunha = serverPointer.whoAmI ( remoteAddress , remotePort );
            serverPointer.logout ( alcunha , remoteAddress , remotePort  );
          }
          break;
        }
      case 5 :
        {
          System.out.println( "- QUIT -" );
          boolean answer = serverPointer.isThisSocketBound (  remoteAddress , remotePort );
          if ( answer == true ){
            String alcunha = serverPointer.whoAmI ( remoteAddress , remotePort );
            serverPointer.quit ( alcunha , remoteAddress , remotePort  );
          }
          break;
        }
      case 6 :
        {
          System.out.println( "- END -" );
          boolean answer = serverPointer.isThisSocketBound ( remoteAddress, remotePort );
          if ( answer == true ){
            String alcunha = serverPointer.whoAmI ( remoteAddress, remotePort );
            serverPointer.end ( alcunha , remoteAddress, remotePort);
          }
          break;
        }
      case 7 :
        {
          System.out.println( "- LIST_CHALLENGES -" );
          boolean answer = serverPointer.isThisSocketBound (  remoteAddress, remotePort );
          if ( answer == true ){
            serverPointer.list_Challenges ( remoteAddress , remotePort );
          }
          break;
        }
      case 8 :
        {
          System.out.println( "- MAKE_CHALLANGE -" );
          boolean answer = serverPointer.isThisSocketBound (  remoteAddress, remotePort );
          if ( answer == true ){
            String alcunha = serverPointer.whoAmI ( remoteAddress, remotePort );
            ArrayList< String > campos =byteToString( fields_bytes , 1 , 0 );
            String nomeDesafio = campos.get ( 0 );
            int tamanhoNome = nomeDesafio.length() +1 ;
            byte data[] = new byte[6];
            inputByteArray.read(data, 1, tamanhoNome);
            byte hora[] = new byte[6];
            inputByteFields.read(data, 1, tamanhoNome + 6);
            serverPointer.make_Desafio(  alcunha , nomeDesafio, data , hora , remoteAddress, remotePort );
          }
          break;
        }
      case 9 :
        {
          System.out.println( "- ACCEPT_CHALLANGE -" );
          boolean answer = serverPointer.isThisSocketBound (  remoteAddress, remotePort );
          if ( answer == true ){
            String alcunha = serverPointer.whoAmI ( remoteAddress, remotePort );
            ArrayList< String > campos =byteToString( fields_bytes , 1 , 0 );
            String nomeDesafio = campos.get ( 0 );
            serverPointer.aceitar_Desafio( alcunha , nomeDesafio , remoteAddress, remotePort );
          }
          break;
        }
      case 10 :
        {
          System.out.println( "- DELETE_CHALLANGE -" );
          boolean answer = serverPointer.isThisSocketBound (  remoteAddress, remotePort );
          if ( answer == true ){
            String alcunha = serverPointer.whoAmI ( remoteAddress, remotePort );
            ArrayList< String > campos =byteToString( fields_bytes , 1 , 0 );
            String nomeDesafio = campos.get ( 0 );
            serverPointer.delete_Challange( alcunha , nomeDesafio , remoteAddress, remotePort );
          }
          break;
        }
      case 11 :
        {
          System.out.println( "- ANSWER -" );
          boolean answer = serverPointer.isThisSocketBound (  remoteAddress, remotePort );
          if ( answer == true ){
            String alcunha = serverPointer.whoAmI ( remoteAddress, remotePort );
            int escolha = inputByteFields.read();
            int tamanhoNomeDesafio = followingFieldsSize -2 ;
            byte[]  nomeDesafio_bytes = new byte[tamanhoNomeDesafio];
            inputByteFields.read(nomeDesafio_bytes, 1, tamanhoNomeDesafio);
            int questao = inputByteFields.read();
            ArrayList< String >campos=byteToString( nomeDesafio_bytes , 1 , 0 );
            String nomeDesafio = campos.get( 0 );
            serverPointer.answer( alcunha , escolha , nomeDesafio , questao , remoteAddress, remotePort );
          }
          break;
        }
      case 12 :
        {
          System.out.println( "- RETRANSMIT -" );
          boolean answer = serverPointer.isThisSocketBound (  remoteAddress, remotePort );
          if ( answer == true ){
            int tamanhoNomeDesafio = followingFieldsSize -2 ;
            byte[]  nomeDesafio_bytes = new byte[tamanhoNomeDesafio];
            inputByteFields.read(nomeDesafio_bytes, 1, tamanhoNomeDesafio);
            int questao = inputByteFields.read();
            int bloco = inputByteFields.read();
            ArrayList< String >campos=byteToString( nomeDesafio_bytes , 1 , 0 );
            String nomeDesafio = campos.get( 0 );
            serverPointer.retransmit( nomeDesafio , questao , bloco , remoteAddress, remotePort );
          }
          break;
        }
      case 13 :
        {
          System.out.println( "- LIST_RANKING -" );
          boolean answer = serverPointer.isThisSocketBound ( remoteAddress, remotePort );
          if ( answer == true ){
            serverPointer.listRanking (  remoteAddress, remotePort );
          }
          break;
        }
      default : 
        System.out.println( "- CODE_INVALID -" + tipo );
        break;
    }
  }

  public ArrayList< String > byteToString ( byte[] bytes, int strings , int offset ){

    ArrayList< String >message=new ArrayList< String >();
    int pos=offset;
    byte next = bytes[pos];
    for( int numberString = 0 ;  numberString < strings ; numberString++ ){
      StringBuilder reader=new StringBuilder();
      next = bytes[ pos ];
      for( ; next!=0 ; pos++ , next=bytes[ pos ]){
        reader.append(( char )bytes[ pos ]);	
      }
      message.add( reader.toString() );
    }  
    return( message );
  }
}
