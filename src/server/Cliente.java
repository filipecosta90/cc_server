package server;

/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

public class Cliente 
{
  private String nome;
  private String alcunha;
  private byte[] sec_info;
  private boolean isLogged;
  private int score;

  // Constructor
  public Cliente( String nome , String alcunha , byte[] sec_info )
  {
    this.nome = nome;
    this.alcunha = alcunha;
    this.sec_info = sec_info;
    this.isLogged = false;
    this.score = 0;
  }

  // Getter
  public String getNome(){ 
    return this.nome; 
  }

  public String getAlcunha(){ 
    return this.alcunha; 
  }

  public boolean isLogged(){ 
    return this.isLogged; 
  }

  public int getScore() { 
    return this.score; 
  }

  // Log
  public boolean login( byte[] sec_info )
  {
    if( new String( this.sec_info ).equals( new String( sec_info )))
      isLogged=true;
    return isLogged;
  }

  public void logout(){ 
    this.isLogged=false; 
  }

  @Override
    public String toString()
    {
      StringBuilder s = new StringBuilder();
      s.append( "Nome: "+ nome +" , " );
      s.append( "Alcunha: "+ alcunha +" , " );
      s.append( "Score: "+ score +" , " );
      return s.toString();
    }
}
