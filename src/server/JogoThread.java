package server;

/*
* @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
* @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
* @version 0.1
*/

public class JogoThread implements Runnable
{
  private Server server;
  private Jogo jogo;
  private int question=0;
  private boolean is_Certa=false;

  public JogoThread( Server server,Jogo jogo )
  { 
    this.jogo=jogo; 
    this.server=server; 
  }

  @Override
    public synchronized void run() 
    {
      ask( jogo.getId( 1 ), jogo.getId( 2 ) );
    }
      public void ask( short id1,short id2 ){
        server.ask( question++,id1,id2 );
      }

      public synchronized void certa( short id ){
        this.is_Certa=true;
        notifyAll();
      }
    }