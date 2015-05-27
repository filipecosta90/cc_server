package server;

/*
* @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
* @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
* @version 0.1
*/

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class ClientPduBuilder {
	// Cr8tor
	public ClientPduBuilder(){}
	// PDU Building functions
	public byte[] HELLO(){ return new byte[]{ 1 }; }
	public byte[] REGISTER( String nome,String alcunha,String sec_info )
	{
		byte[] idAndCode=idAndCode( ( short )0,2 );
		byte[] pdu=null;
		
		try {
			byte[] name=( nome+'\0' ).getBytes( "UTF-8" );
			byte[] nick=( alcunha+'\0' ).getBytes( "UTF-8" );
			byte[] pass=( sec_info+'\0' ).getBytes( "UTF-8" );
			pdu=appendBytes( idAndCode,appendBytes( name,appendBytes( nick,pass ) ));
		}
		catch( UnsupportedEncodingException e ){}
		
		return pdu;
	}
	public byte[] LOGIN( String alcunha,String sec_info )
	{
		byte[] idAndCode=idAndCode( ( short )-1,3 );
		byte[] pdu=null;
		
		try {
			byte[] nick=( alcunha+'\0' ).getBytes( "UTF-8" );
			byte[] pass=( sec_info+'\0' ).getBytes( "UTF-8" );
			pdu=appendBytes( idAndCode,appendBytes( nick,pass ));
		}
		catch( UnsupportedEncodingException e ){}
		
		return pdu;
	}
	public byte[] LOGOUT( short id ){ return idAndCode( id,4 ); }
	public byte[] QUIT( short id )
	{
		return idAndCode( id,5 );
		
	}
	public byte[] END( short id,String desafio )
	{ 
		byte[] idAndCode=idAndCode( id,6 );
		byte[] pdu=null;
		try
		{
			byte[] game=( desafio+'\0' ).getBytes( "UTF-8" );
			pdu=appendBytes( idAndCode,game );
		}
		catch( UnsupportedEncodingException e ){}
		
		return pdu;
	}
	public byte[] LIST_CHALLENGES( short id ){ return idAndCode( id,7 );}
	public byte[] MAKE_CHALLENGE( short id,String desafio,short[] data,short[] hora )
	{ 
		byte[] idAndCode=idAndCode( id,8 );
		byte[] pdu=null;
		
		try
		{
			byte[] name=( desafio+'\0' ).getBytes( "UTF-8" );
			byte[] date=shortToByte( data );
			byte[] hour=shortToByte( hora );
			pdu=appendBytes( idAndCode,appendBytes( name,appendBytes( date,hour )));
		}
		catch( UnsupportedEncodingException e ){}
		
		return pdu;
	}
	public byte[] ACCEPT_CHALLENGE( short id,String desafio )
	{
		byte[] idAndCode=idAndCode( id,9 );
		byte[] pdu=null;
		
		try
		{
			byte[] name=( desafio+'\0' ).getBytes( "UTF-8" );
			pdu=appendBytes( idAndCode,name ); 
		}
		catch( UnsupportedEncodingException e ){}
		
		return pdu;
	}
	public byte[] DELETE_CHALLENGE( short id,String desafio )
	{
		byte[] idAndCode=idAndCode( id,10 );
		byte[] pdu=null;
		
		try
		{
			byte[] name=( desafio+'\0' ).getBytes( "UTF-8" );
			pdu=appendBytes( idAndCode,name ); 
		}
		catch( UnsupportedEncodingException e ){}
		
		return pdu;
	}
	public byte[] ANSWER( short id,int questao,int resposta,String desafio )
	{
		byte[] idAndCode=idAndCode( id,11,questao,resposta );
		byte[] pdu=null;
		
		try
		{
			byte[] name=( desafio+'\0' ).getBytes( "UTF-8" );
			pdu=appendBytes( idAndCode, name ); 
		}
		catch( UnsupportedEncodingException e ){}
		
		return pdu;
	}
	public byte[] RETRANSMIT( String desafio,int questao,int bloco )
	{
		byte[] code=new byte[]{ 12 };
		byte[] pdu=null;
		
		try
		{
			byte[] name=( desafio+'\0' ).getBytes( "UTF-8" );
			byte[] other=new byte[]{ ( byte )questao,( byte )questao };
			pdu=appendBytes( code,appendBytes( name,other ));
		}
		catch( UnsupportedEncodingException e ){}
		
		return pdu;
	}
	public byte[] LIST_RANKING(){ return new byte[]{ 13 }; }
	/**
	 * Auxiliary functions
	 */
	// Append two byte[] into a new one.
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
	private byte[] idAndCode( short id,int field )
	{
		byte[] bytes=new byte[ 3 ];
		bytes[0]=( byte ) id;
		bytes[1]=( byte )(( id>>8 ) & 0xff);
		bytes[2]=( byte ) field;
		
		return bytes;
	}
	private byte[] idAndCode( short id,int field,int i1, int i2 )
	{
		byte[] bytes=new byte[ 5 ];
		bytes[ 0 ]=( byte ) id;
		bytes[ 1 ]=( byte )(( id>>8 ) & 0xff);
		bytes[ 2 ]=( byte ) field;
		bytes[ 3 ]=( byte ) i1;
		bytes[ 4 ]=( byte ) i2;
		
		return bytes;
	}
	
	// Convert short[] to byte[]
	private byte[] shortToByte( short[] shorts )
	{
		ByteBuffer buffer=ByteBuffer.allocate( shorts.length*2 );
		
		for( int i=0 ; i<shorts.length ; i++ )
			buffer.putShort( shorts[ i ]);
		
		return buffer.array();
	}
}
