package UDPlistner;
import java.io.IOException;
import java.net.*;

public class UDPlistner implements Runnable 
{
	private String name = "Annon";
	
	// Creators
	public UDPlistner( String name ){ this.name = name; }
	
	//Getter
	public String getName(){ return this.name; }
		
	// Runnable
	public void run()
	{
		byte[] aReceber;
		DatagramSocket s;
		DatagramPacket data;
		
		try
		{
			System.out.println( "Listner started." );
			
			aReceber = new byte[ 1024 ];
			s = new DatagramSocket( 9871 );
			data = new DatagramPacket( aReceber , aReceber.length );
			
			while( true )
			{
				s.receive( data );
				System.out.println( this.name+" : "+data.toString() );
			}
		} catch( IOException io ) {
			System.out.println( this.name+" : " + io.toString() );
		}
	}	
}