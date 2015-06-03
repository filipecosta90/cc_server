/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

package server;

public class Pergunta {
  private String musica;
  private String imagem;
  private String pergunta;
  private String[] ops=new String[ 3 ];
  private int certa;

  // Cr8tor
  public Pergunta( String mp3 , String img , String[] pgt, int certa )
  {
    this.musica=mp3;
    this.imagem=img;
    this.pergunta=pgt[ 0 ];
    ops[ 0 ]=pgt[ 1 ];
    ops[ 1 ]=pgt[ 2 ];
    ops[ 2 ]=pgt[ 3 ];
    this.certa=certa;
  }

  public String get_Musica(){ return this.musica; }
  public String get_Imagem(){ return this.imagem; }
  public String get_pergunta(){ return this.pergunta; }
  public String[] get_ops(){ return ops; }
  public boolean is_Certa( int resposta ){ return( resposta==certa ); }
}
