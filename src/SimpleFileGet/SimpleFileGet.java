package SimpleFileGet;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class SimpleFileGet implements Runnable {

  public final static int SOCKET_PORT = 13268;      // you may change this
  public final static String SERVER = "127.0.0.1";  // localhost
  public String TARGET = "/Users/ra/Desktop/file.pdf";
  public final static int FILE_SIZE = 6022386;
  
  public SimpleFileGet( String path )
  {
	  this.TARGET = path;
  }

  @Override
  public void run() {
    int bytesRead;
    int current = 0;
    File file = new File( TARGET );
    FileOutputStream fos = null;
    BufferedOutputStream bos = null;
    Socket sock = null;
    try {
      sock = new Socket(SERVER, SOCKET_PORT);
      System.out.println("Connecting...");

      // receive file
      byte [] mybytearray  = new byte [FILE_SIZE];
      InputStream is = sock.getInputStream();
      fos = new FileOutputStream( TARGET );
      bos = new BufferedOutputStream(fos);
      bytesRead = is.read(mybytearray,0,mybytearray.length);
      current = bytesRead;

      do {
         bytesRead =
            is.read(mybytearray, current, (mybytearray.length-current));
         if(bytesRead >= 0) current += bytesRead;
      } while(bytesRead > -1);

      bos.write(mybytearray, 0 , current);
      bos.flush();
      System.out.println("File " + TARGET
          + " downloaded (" + current + " bytes read)");
    } catch( IOException e ){}
    
    finally 
    {
    	try 
    	{
    		if (fos != null) fos.close();
    		if (bos != null) bos.close();
    		if (sock != null) sock.close();
    	} catch( IOException e ){}
    }
  }
}