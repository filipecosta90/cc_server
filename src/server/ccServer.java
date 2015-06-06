/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

package server;

public class ccServer 
{
  public static Server localServer = new Server ( 3030 , 7070 , "data/listaDesafios.txt" );

  public static void main( String[] args ) throws Exception 
  {
    Thread thread = new Thread( localServer );
    thread.start();
  }
}
