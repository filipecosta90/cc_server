package Client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import BusinessL.Server;

public class ClientPduReader 
{
  private Console console;
  // Cr8tor
  public ClientPduReader( Console console ){ this.console=console; }

  // Byte reading functions
  public void byteRead( byte[] bytes )
  {	
    ByteArrayInputStream bais=new ByteArrayInputStream( bytes );
    DataInputStream in=new DataInputStream( bais );
    short id=0;
    int code=-1;
    short field=-1;

    try 
    {
      id=in.readShort();
      /*
         if( id>0 && id!=console.getID() )
         return;
         */

      if( id==-1 ){ System.out.println( "ID :: Anon" ); }

      code=in.readInt();
      if( code!=0 ){ System.out.println( "Bad message code." ); }
      field=in.readShort();


      ///*		DEBUGGER 
      System.out.print( "[ ID: "+id+" , " );
      System.out.print( "Code: "+code+" , "  );
      System.out.print( "Field : "+field+" , " );
      System.out.println( "Size: "+bytes.length+" ]"  );	
      //*/

      switch( field )
      {
        case 0 : 
          {
            System.out.println( "- OK -" );
            console.handle_OK( id );
            break;
          }
        case 255 :
          {
            System.out.println( "- ERRO -" );
            String text=in.readUTF();
            if( id==console.getID() )
              console.handle_Erro( text );
            else
            {
              if( !console.getLogged()&&( text.equals( console.getNick() )))
                console.handle_LogFail( id,text );
            }
            console.handle_Erro( byteToString( bytes,1,2 ).get( 0 ) );
            break;
          }
        case 254 : 
          {
            System.out.println( "- FRAGMENT -" );
            console.handle_Frag( readSet( bytes ));
            break;
          }
        case 1 : 
          {
            System.out.println( "- NOME -" );
            String nick=in.readUTF();
            if( console.getNick().equals( nick ) )
            {
              console.setID( id );
              console.handle_Logs();
            }
            break;
          }
        case 2 :
          {
            System.out.println( "- ALCUNHA -" );
            console.setNick( in.readUTF() );
            console.setID( id );
            console.handle_Logs();
            break;
          }
        case 4 :
          {
            System.out.println( "- DATA -" );
            byteToShort( bytes,2,3 );
            break;
          }
        case 5 :
          {
            System.out.println( "- HORA -" );
            byteToShort( bytes,2,3 );
            break;
          }
        case 7 :
          {
            System.out.println( "- DESAFIO -" );
            console.handle_Challange( in.readUTF() );
            break;
          }
        case 10 :
          {
            System.out.println( "- N_QUESTAO -" );
            //System.out.println( "Questao nº "+( int )bytes[ 3 ]);
            break;
          }
        case 11 :
          {
            System.out.println( "- QUESTAO -" );
            int numb=in.readInt();
            String question=in.readUTF();
            String[] op=new String[ 3 ];
            op[ 0 ]=in.readUTF(); op[ 1 ]=in.readUTF(); op[ 2 ]=in.readUTF();
            console.handle_Question( numb,question,op );
            break;
          }
        case 12 :
          {
            //System.out.println( "- N_ANSWER -" );
            //System.out.println( "Opcao nº "+( int )bytes[ 3 ]);
            break;
          }
        case 13 :
          {
            //System.out.println( "- ANSWER -" );
            break;
          }
        case 14 :
          {
            System.out.println( "- CERTA -" );
            int certa=in.readInt();
            if( certa==0 )
              System.out.println( "A Resposta esta errada." );
            else
              System.out.println( "Parabéns! Acertou!" );
            break;
          }
        default : System.out.println( "- INVALID_CODE -" );
                  break;
      }

    }
    catch (IOException e) { System.out.println( e ); }
  }
  public ArrayList< String >byteToString( byte[] bytes,int strings,int start )
  {
    ArrayList< String >message=new ArrayList< String >();
    StringBuilder reader=new StringBuilder();
    int i=start;
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
     * System.out.println( "End of s, index at: "+( i ));
     * System.out.println( "ArrayList: "+message.toString() );
     */
    return( message );
  }
  public void byteToShort( byte[] bytes,int start,int shorts )
  {
    ArrayList< String >message=new ArrayList< String >();
    short s;

    for( int i=start ; i<( start+( 2*shorts )) ; i+=2  )
    {			
      //System.out.println( "Index i: "+i+" ,Short: "+( short )( bytes[ i ]+bytes[ i+1 ]));
      s=( short )( bytes[ i ]+bytes[ i+1 ]);
      message.add( String.valueOf( s ));
    }

    System.out.println( "ArrayList: "+message.toString() );
  }
  public ArrayList< String >readSet( byte[] bytes )
  {
    ByteArrayInputStream bais=new ByteArrayInputStream( bytes );
    DataInputStream in=new DataInputStream( bais);
    ArrayList< String >list=new ArrayList< String >();

    try {
      short id=in.readShort();
      int code=in.readInt();
      short field=in.readShort();

      while( in.available()>0 ) 
      {
        String element = in.readUTF();
        list.add( element );
      }
    } catch (IOException e) {}

    return list;
  }
}
