package server;

/*
* @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
* @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
* @version 0.1
*/

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class ServerPduReader {
  private Server server;
  // Cr8tor
  public ServerPduReader( Server server ){ this.server=server; }

  public void byteRead( byte[] bytes )
  {			
    short id=( short )(( bytes[ 1 ] << 8 )+( bytes[ 0 ]&0xFF ));
    int code=( int )bytes[ 2 ];
    
    ///*      DEBUGGER

    System.out.print( "( ID: "+id );
    System.out.print( " , Code: "+code );
    System.out.println( " , Size: "+bytes.length+" )" );
  
    switch( code )
    {
      case 1 :
        {
          System.out.println( "- HELLO -" );
          //server.helLo();
          break;
        }
      case 2 :
        {
          System.out.println( "- REGISTER -" );
          ArrayList< String >arrl=byteToString( bytes,2 );
          server.registar( arrl.get( 0 ),arrl.get( 1 ),
              subByteArray( bytes,Integer.parseInt( arrl.get( 2 )),bytes.length-1 ));
          break;
        }
      case 3 : 
        {
          System.out.println( "- LOGIN -" );
          ArrayList< String >arrl=byteToString( bytes,1 );

          server.login( arrl.get( 0 ),
              subByteArray( bytes,Integer.parseInt( arrl.get( 1 )),bytes.length-1 ));
          break;
        }
      case 4 : 
        {
          System.out.println( "- LOGOUT -" );
          server.logout( id );
          break;
        }
      case 5 :
        {
          System.out.println( "- QUIT -" );
          break;
        }
      case 6 :
        {
          System.out.println( "- END -" );
          ArrayList< String >arrl=byteToString( bytes,1 );
          server.end( id,arrl.get( 0 ));
          break;
        }
      case 7 :
        {
          System.out.println( "- LIST_CHANLLENGES -" );
          server.list_Challenges( id );
          break;
        }
      case 8 :
        {
          System.out.println( "- MAKE_CHALLANGE -" );
          ArrayList< String >arrl=byteToString( bytes,1 );
          server.make_Desafio( id,arrl.get( 0 ),
              byteToShort( bytes,Integer.parseInt( arrl.get( 1 ))+1,6 ));
          break;
        }
      case 9 :
        {
          System.out.println( "- ACCEPT_CHALLANGE -" );
          server.aceitar_Desafio( id,byteToString( bytes,1 ).get( 0 ) );
          break;
        }
      case 10 :
        {
          System.out.println( "- DELETE_CHALLANGE -" );
          server.delete_Challange( id,byteToString( bytes,1 ).get( 0 ));
          break;
        }
      case 11 :
        {
          System.out.println( "- ANSWER -" );
          int questao=( int )bytes[ 3 ];
          int resposta=( int )bytes[ 4 ];
          ArrayList< String >arrl=byteToString( bytes,1 );
          server.answer( id,questao,resposta,singleString( bytes,5 ) );

          //System.out.println( "Answer: "+( int )bytes[ index ]);
          //System.out.println( "Question: "+( int )bytes[ index+1 ]);
          break;
        }
      case 12 :
        {
          System.out.println( "- RETRANSMIT -" );
          //int index=byteToString( bytes,1 );
          //System.out.println( "Question: "+( int )bytes[ index ]);
          //System.out.println( "Block: "+( int )bytes[ index+1 ]);
          break;
        }
      case 13 :
        {
          System.out.println( "- LIST_RANKING -" );
          // QUIT HANDLER
          break;
        }
      default : System.out.println( "- CODE_INVALID -" );
                break;
    }
  }
  public String singleString( byte[] bytes,int start )
  {
    StringBuilder reader=new StringBuilder();
    byte next=bytes[ start ];
    for( int i=start; next!=0 ; i++,next=bytes[ i ])
    {
      //System.out.println( "At index: "+i+" ,Read: "+( char )bytes[ i ]);
      reader.append(( char )bytes[ i ]);	
    }
    return reader.toString();
  }
  public ArrayList< String >byteToString( byte[] bytes,int strings )
  {
    ArrayList< String >message=new ArrayList< String >();
    StringBuilder reader=new StringBuilder();
    int i=2;
    byte next;

    for( int s=0 ; s<strings ; s++ )
    {
      i++; next=bytes[ i ];
      //System.out.println( "Iteracao s: "+s+" index at: "+i+" ,next char: "+( char )next );
      for( ; next!=0 ; i++,next=bytes[ i ])
      {
        //System.out.println( "At index: "+i+" ,Read: "+( char )bytes[ i ]);
        reader.append(( char )bytes[ i ]);	
      }
      message.add( reader.toString() );
      reader=new StringBuilder();
    }
    message.add( ""+i );
    /** 
     * System.out.println( "End of s, index at: "+i );
     * System.out.println( "ArrayList: "+message.toString() );
     */
    return( message );
  }
  public short[] byteToShort( byte[] bytes,int start,int shorts )
  {
    ByteBuffer bb=ByteBuffer.wrap( bytes,start,2*shorts );
    short[] time=new short[ shorts ];
    int i=0;

    while( bb.hasRemaining() )
    {
      time[ i++ ]=bb.getShort();
    }

    for( short s : time )
      System.out.print( s+" " );

    return time;
  }
  public byte[] subByteArray( byte[] bytes,int start,int end )
  {
    byte[] subBytes=new byte[ end-start ];

    for( int i=start ; i<( end )&&( i<bytes.length ) ; i++ )		
      subBytes[ i-start ]=bytes[ i ];

    return subBytes;
  }
}
