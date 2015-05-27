package server;

/*
* @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
* @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
* @version 0.1
*/

import java.util.ArrayList;

public class Jogo {
  private String name;
  private short[] player_ids=new short[ 3 ];
  private short[] scores={ 0,0,0 };
  private short[] time;
  private JogoThread JT;
  
  // Cr8tor
  public Jogo( String game_name,short player1,short[] time )
  {
    this.name=game_name;
    this.player_ids[ 1 ]=player1;
    this.player_ids[ 2 ]=-1;
    this.time=time;
  }
  
  //--------------------------------------------------------------------------
  // Gets & Sets
  public void setThread( JogoThread jt ){ this.JT=jt; }
  
  public int get_player( short player )
  {  
    if( player==player_ids[ 1 ] ){ return 1; }
    if( player==player_ids[ 1 ] ){ return 2; }
    return -1;
  }

  public short getId( int player ){ return player_ids[ player ]; }
  
  public JogoThread getJT(){ return this.JT; }
  
  //--------------------------------------------------------------------------
  // Add 2nd player
  public void add_player2( short p2 ){ this.player_ids[ 2 ]=p2; }
  
  //--------------------------------------------------------------------------
  // Add score
  public void right_Answer( short id ){
    scores[ get_player( id ) ]++;
    JT.ask( player_ids[ 1 ],player_ids[ 1 ] );
  }

  public short[] end( short id )
  {
    short[] end=new short[ 3 ];
    if( this.player_ids[ 1 ]==id )
    {
      end[ 0 ]=player_ids[ 2 ]; end[ 1 ]=scores[ 1 ]; end[ 2 ]=scores[ 2 ];
    }
    else
      scores[ 0 ]=player_ids[ 1 ]; end[ 1 ]=scores[ 2 ]; end[ 2 ]=scores[ 1 ];
    return end;
  }

  @Override
    public String toString()
    {
      return( "Nome: "+this.name+", Por: "+this.player_ids[ 1 ]
          +" ,Data: "+time[ 2 ]+"-"+time[ 1 ]+"-"+time[ 0 ]
          +" ,Hora: "+time[ 3 ]+":"+time[ 4 ]+":"+time[ 5 ] 
          );
    }
}
