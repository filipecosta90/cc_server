package server;

/*
* @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
* @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
* @version 0.1
*/

public class Counter {
  private short c;

  public Counter(){ c=1; }

  public synchronized short genID() 
  { 
    if( c>( 32767 ))
      c = 0;
    c++;
    return c;
  }
}
