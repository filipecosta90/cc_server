package BusinessL;

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
      ask( jogo.getId( 1 ),jogo.getId( 2 ) );

      public void ask( short id1,short id2 )
      {
        server.ask( question++,id1,id2 );
      }

      public synchronized void certa( short id )
      {
        this.is_Certa=true;
        notifyAll();
      }
    }
