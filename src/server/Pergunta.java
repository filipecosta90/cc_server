/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

package server;

import java.io.Serializable;

public class Pergunta implements Serializable {

  private static final long serialVersionUID = -6467491037165992618L;

  private String musica;
  private String imagem;
  private String pergunta;
  private String[] ops=new String[ 3 ];
  private int certa;

  // Construtores
  public Pergunta( String mp3 , String img , String pergunta , String[] opcoes , int certa )
  {
    this.musica=mp3;
    this.imagem=img;
    this.pergunta=pergunta;
    ops[ 0 ]=opcoes[ 0 ];
    ops[ 1 ]=opcoes[ 1 ];
    ops[ 2 ]=opcoes[ 2 ];
    this.certa=certa;
  }

  public String get_Musica(){ 
    return this.musica; 
  }

  public String get_Imagem(){ 
    return this.imagem; 
  }

  public String get_pergunta(){ 
    return this.pergunta; 
  }

  public String[] get_ops(){ 
    return ops; 
  }

  public int getCerta(){ 
    return this.certa;
  }

  public String getTextoOpcao( int numeroOpcao ){
    String opcaoRetornar = new String( ops[numeroOpcao]);
    return opcaoRetornar; 
  }

  public boolean acertou( int resposta ){ 
    return( resposta==certa ); 
  }

  @Override
    public String toString()
    {
      StringBuilder s = new StringBuilder();
      s.append(this.pergunta);
      s.append("\n opcao1: "+ this.ops[0]);
      s.append("\t opcao2: "+ this.ops[1]);
      s.append("\t opcao3: "+ this.ops[2]);
      s.append("\n Opcao Correcta: "+ this.certa);
      return s.toString();
    }




}
