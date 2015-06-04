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

  // Construtores
  public Desafio( String game_name , String alcunhaJogadorCriador , Date dataCriacao , Date dataHoraDesafio )
  {
    this.nomeDesafio=game_name;
    this.criadoPor = alcunhaJogadorCriador;
    this.alcunhasJogadores.add( alcunhaJogadorCriador );
    this.pontuacoesJogadores.put( alcunhaJogadorCriador, 0);
    this.dataCriacao = dataCriacao;
    this.dataHoraInicioDesafio = dataHoraDesafio;
    temMinimoJogadores = false;
  }

  public Desafio ( Desafio makeCopy ){
    this.nomeDesafio= makeCopy.getNomeDesafio();
    this.criadoPor = makeCopy.getCriadoPor();
    this.alcunhasJogadores = makeCopy.getAlcunhasJogadores();
    this.pontuacoesJogadores = makeCopy.getPontuacoesJogadores();
    this.dataCriacao = makeCopy.getDataCriacao();
    this.dataHoraInicioDesafio = makeCopy.getDataHoraInicioDesafio();
    this.temMinimoJogadores = makeCopy.getTemMinimoJogadores();
  }

  // Métodos Get
  public String getCriadoPor() {
    String novoCriadoPor = new String ( this.criadoPor);
    return novoCriadoPor;
  }

  private boolean getTemMinimoJogadores() {
    boolean minimo = this.temMinimoJogadores;
    return minimo;
  }

  public Date getDataCriacao() {
    Date novaDataCriacao = new Date ( this.dataCriacao.getTime() );
    return novaDataCriacao;
  }

  public String getNomeDesafio() {
    String novoNomeDesafio = new String (this.nomeDesafio);
    return novoNomeDesafio;
  }

  public Date getDataHoraInicioDesafio() {
    Date novaDataHoraInicioDesafio = new Date ( this.dataHoraInicioDesafio.getTime() );
    return novaDataHoraInicioDesafio;
  }

  public TreeMap < String, Integer > getPontuacoesJogadores() {
    TreeMap < String, Integer > pontuacoes = new TreeMap < String, Integer > ();
    for ( String alcunha : this.pontuacoesJogadores.keySet() ){
      String copiaAlcunha = new String ( alcunha );
      int copiaPontos = this.pontuacoesJogadores.get(copiaAlcunha);
      pontuacoes.put( copiaAlcunha , copiaPontos );
    }
    return pontuacoes;
  }

  public TreeSet < String > getAlcunhasJogadores() {
    TreeSet < String > alcunhas = new TreeSet < String > ();
    for ( String alcunha : this.alcunhasJogadores ){
      String copiaAlcunha = new String ( alcunha );
      alcunhas.add( copiaAlcunha );
    }
    return alcunhas;
  }

  /* Outros métodos */
  public void adicionaJogador ( String alcunha ){
    this.alcunhasJogadores.add( alcunha );
    this.pontuacoesJogadores.put( alcunha , 0 );
  }

  /* toString & clone */
  @Override
    public String toString()
    {
      return( "Nome desafio: "+this.nomeDesafio );
    }

  @Override
    public Desafio clone(){
      return new Desafio(this);
    }

  @Override 
    public boolean equals ( Object other ){
      boolean resultado = false;
      if (other instanceof Desafio) {
        Desafio that = (Desafio) other;
        resultado = this.getNomeDesafio().equals(that.getNomeDesafio());
      }
      return resultado;
    }
}
