package BusinessL;

public class Cliente 
{
  private short id;
  private String name;
  private String nick;
  private byte[] pass;
  private boolean isLogged;

  // Cr8tor
  public Cliente( String name,short id,String nick,byte[] pass )
  {
    this.name=name;
    this.id=id;
    this.nick=nick;
    this.pass=pass;
    this.isLogged=false;
  }

  // Getter
  public short getId(){ return this.id; }
  public String getName(){ return this.name; }
  public String getNick(){ return this.nick; }
  public boolean isLogged(){ return this.isLogged; }

  // Log
  public boolean login( byte[] pass )
  {
    if( new String( this.pass ).equals( new String( pass )))
      isLogged=true;
    return isLogged;
  }
  public void logout(){ this.isLogged=false; }

  @Override
    public String toString()
    {
      StringBuilder s = new StringBuilder();
      s.append( "Nome: "+name+" , " );
      s.append( "Id: "+id+" , " );
      s.append( "Alcunha: "+nick+" , " );
      return s.toString();
    }
}
