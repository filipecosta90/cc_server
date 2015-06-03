package server;

public class DesafioManager implements Runnable {

  private Server localServerPointer;
  private Desafio desafioAGerir;

  DesafioManager ( Server localServer , Desafio desafio ){
    this.localServerPointer = localServer;
    this.desafioAGerir = desafio;
  }

  public void run() {

  }
}
