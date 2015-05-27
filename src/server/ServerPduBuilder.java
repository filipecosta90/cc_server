package server;

/*
* @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
* @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
* @version 0.1
*/

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class ServerPduBuilder 
{
  // Cr8tor
  public ServerPduBuilder(){}
  // Calls
  public byte[] REPLY_OK( short id )					{ return REPLY( id,( short )0 );		}
  public byte[] REPLY_ERRO( short id,String text )	{ return REPLY( id,( short )255,text );	}
  public byte[] REPLY_NOME( short id,String name )	{ return REPLY( id,( short )1,name );	}
  public byte[] REPLY_ALCUNHA( short id,String nick )	{ return REPLY( id,( short )2,nick );	}
  public byte[] REPLY_DATA( short id,short[] date )	{ return REPLY( id,( short )4,date );	}
  public byte[] REPLY_HORA( short id,short[] hour )	{ return REPLY( id,( short )5,hour );	}
  public byte[] REPLY_DESAFIO( short id,String text )	{ return REPLY( id,( short )7,text );	}
  public byte[] REPLY_QUESTAO( short id,int numb,String question, String[] ops )
  { 
    return REPLY( id,( short )11,numb,question,ops );
  }
  public byte[] REPLY_CERTA( short id,int certa ) { return REPLY( id,( short )14,certa ); }
  public byte[] REPLY_SCORE( short id,short pontos )
  {
    return REPLY( id,( short )20,pontos );
  }
  /*
     public byte[] REPLY_PONTOS( int i )
     { 
     return new byte[]{ 0,( byte )0,( byte )15,( byte )i }; 
     }
     public byte[] REPLY_BLOCO( int i )
     {
     short campo=17;
     byte pdu[] = new byte[ 4 ];
     pdu[ 0 ]=( byte )0;
     pdu[ 1 ]=( byte )(( campo>>8) & 0xff );
     pdu[ 2 ]=( byte )campo;
     pdu[ 3 ]=( byte )i;
     return pdu;
     }
     public byte[] REPLY_SCORE( short points )
     {
     short campo=20;
     byte pdu[]=new byte[ 5 ];
     pdu[ 0 ]=( byte )0;
     pdu[ 1 ]=( byte )(( campo>>8) & 0xff );
     pdu[ 2 ]=( byte )campo;
     pdu[ 3 ]=( byte )(( points>>8) & 0xff );
     pdu[ 4 ]=( byte )points;
     return pdu;
     }

*/
  /*
   * Auxiliar Generic Reply Functions
   */
  public byte[] REPLY( short s )
  { 
    return new byte[]{ 0,( byte )(( s>>8) & 0xff ),( byte )s }; 
  }

  public byte[] REPLY( short id,short field )
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(baos);
    try
    {
      out.writeShort( id );
      out.writeInt( 0 );
      out.writeShort( field );
    }catch( IOException e ){ System.out.println( e.toString() ); }

    return baos.toByteArray();
  }

  public byte[] REPLY( short id,short field,int Int )
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(baos);
    try
    {
      out.writeShort( id );
      out.writeInt( 0 );
      out.writeShort( field );
      out.writeInt( Int );
    }catch( IOException e ){ System.out.println( e.toString() ); }

    return baos.toByteArray();
  }

  public byte[] REPLY( short id,short field,String text )
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(baos);
    try
    {
      out.writeShort( id );
      out.writeInt( 0 );
      out.writeShort( field );
      out.writeUTF( text );
    }
    catch( IOException e ){ System.out.println( e.toString() ); }

    return baos.toByteArray();
  }

  public byte[] REPLY( short id,short field,short[] shortArray  )
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(baos);
    try
    {
      out.writeShort( id );
      out.writeInt( 0 );
      out.writeShort( field );
      for( short s : shortArray )
        out.writeShort( s );
    }
    catch( IOException e ){ System.out.println( e.toString() ); }

    return baos.toByteArray();
  }

  public byte[] REPLY( short id,short field,int numb,String question, String[] ops )
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(baos);
    try
    {
      out.writeShort( id );
      out.writeInt( 0 );
      out.writeShort( field );
      out.writeInt( numb );
      out.writeUTF( question );
      for( String s : ops )
        out.writeUTF( s );
    }
    catch( IOException e ){ System.out.println( e.toString() ); }

    return baos.toByteArray();
  }

  /**
   * NEW REPLY FUNCTIONS HERE v
   */
  public byte[] REPLY( short id,HashMap< String,Jogo >jogos )
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(baos);
    try 
    {
      out.writeShort( id );
      out.writeInt( 0 );
      out.writeShort( 254 );
      for( Jogo element : jogos.values() )
        out.writeUTF( element.toString() );
    } catch( IOException e ){}

    return baos.toByteArray();
  }

  public byte[] REPLY( String[] jogos )
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(baos);
    short frag=254;
    byte[] cod=new byte[]{ 0,( byte )(( frag>>8) & 0xff ),( byte )frag }; 
    System.out.println( " String " );
    try 
    {
      out.write( cod );
      for( String element : jogos )
        out.writeUTF( element );
    } catch( IOException e ){}

    return baos.toByteArray();
  }

  /**
   * Append two byte arrays into a new one.
   */
  private byte[] appendBytes( byte[] first,byte[] second )
  {
    byte[] twelv = new byte[ first.length+second.length ];

    int i=0;
    for( ; i<first.length ; i++ )
      twelv[ i ]=first[ i ];

    int f=0;
    for( ; f<second.length ; f++ )
      twelv[ i+f ]=second[ f ];

    return twelv;
  }

  /**
   * Convert short[] to byte[]
   */
  private byte[] shortsToByte( short[] shorts )
  {
    ByteBuffer buffer=ByteBuffer.allocate( shorts.length*2 );

    for( int i=0 ; i<shorts.length ; i++ )
      buffer.putShort( shorts[ i ]);

    return buffer.array();
  }
}
