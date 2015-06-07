package cliente;

public class ccCliente {

  public static void main( String[] args ) throws Exception 
  {
    ClientResponder connectServer = new ClientResponder ( );
    while ( true ){
      connectServer.navega();
    }
  }
}
