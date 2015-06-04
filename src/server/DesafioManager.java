package server;

public class DesafioManager implements Runnable {

  private Server localServerPointer;
  private Desafio desafioAGerir;

  DesafioManager ( Server localServer , Desafio desafio ){
    this.localServerPointer = localServer;
    this.desafioAGerir = desafio;
  }

  public String getNomeDesafioGerir() {
		return this.desafioAGerir.getNomeDesafio();
	}
  
  public void run() {
	  System.out.println("Criado um novo gestor para o desafio: " + desafioAGerir.getNomeDesafio() );
  }
  
  @Override 
  public boolean equals ( Object other ){
	  boolean resultado = false;
	    if (other instanceof DesafioManager) {
	        DesafioManager that = (DesafioManager) other;
	        resultado = this.getNomeDesafioGerir().equals(that.getNomeDesafioGerir());
	    }
	    return resultado;
  }


}
