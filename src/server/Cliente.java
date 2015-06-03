/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

package server;

public class Cliente 
{
  private String nome;
  private String alcunha;
  private String sec_info;
  private boolean isLogged;
  private int score;

  // Constructor
  public Cliente( String nome , String alcunha , String sec_info )
  {
    this.nome = nome;
    this.alcunha = alcunha;
    this.sec_info = sec_info;
    this.isLogged = false;
    this.score = 0;
  }

  // Getter
  public String getNomeCliente(){ 
    return this.nome; 
  }

  public String getAlcunha(){ 
    return this.alcunha; 
  }

  public boolean isLogged(){ 
    return this.isLogged; 
  }

  public int getScoreCliente() { 
    return this.score; 
  }

  // Log
  public boolean checkAndSetLoggedIn( String sec_info )
  {
    boolean result = false;
    if( this.sec_info.equals(  sec_info ) && isLogged == false){
      isLogged=true;
      result = true;
    }
    return result;
  }

  public boolean checkAndSetLoggedOut( )
  {
    boolean result = false;
    if( isLogged == true){
      isLogged=false;
      result = true;
    }
    return result;
  }

  public void setLoggedIn(){
    this.isLogged = true;
  }

  public void setLoggedOut(){ 
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
