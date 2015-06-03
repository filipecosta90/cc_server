/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

package server;

import java.util.Date;
import java.util.TreeMap;
import java.util.TreeSet;

public class Desafio {
  private String nomeDesafio;
  private String criadoPor;
  TreeSet < String > alcunhasJogadores;
  TreeMap < String, Integer > pontuacoesJogadores;
  private Date dataCriacao;
  private Date dataHoraInicioDesafio;
  private boolean temMinimoJogadores;

  // Cr8tor
  public Desafio( String game_name , String alcunhaJogadorCriador , Date dataCriacao , Date dataHoraDesafio )
  {
    this.nomeDesafio=game_name;
    this.criadoPor = alcunhaJogadorCriador;
    this.alcunhasJogadores.add( alcunhaJogadorCriador );
    this.dataCriacao = dataCriacao;
    this.dataHoraInicioDesafio = dataHoraDesafio;
    temMinimoJogadores = false;
  }

  public void adicionaJogador ( String alcunha ){
    this.alcunhasJogadores.add( alcunha );
  }

  @Override
    public String toString()
    {
      return( "Nome desafio: "+this.nomeDesafio );
    }

  public String getNomeDesafio() {
    return this.nomeDesafio;
  }

  public Date getDataHoraInicioDesafio() {
    return this.dataHoraInicioDesafio;
  }
}
