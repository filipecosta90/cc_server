/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

package server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;
import java.util.TreeSet;

public class Desafio implements Serializable {

	private static final long serialVersionUID = -460290336218378857L;

public enum EstadoDesafio { EM_ESPERA , EM_JOGO , TERMINADO , ELIMINADO , PASSOU_PRAZO }
  private String nomeDesafio;
  private String criadoPor;
  private TreeSet < String > alcunhasJogadores;
  private TreeSet < String > alcunhasJogadoresActivos;
  private TreeMap < String, Integer > pontuacoesJogadores;
  private Date dataCriacao;
  private Date dataHoraInicioDesafio;
  private boolean temMinimoJogadores;
  private FicheiroPerguntas perguntasDesafio;
  public EstadoDesafio estado;

  // Construtores
  public Desafio( String game_name , String alcunhaJogadorCriador , Date dataCriacao , Date dataHoraDesafio , String nomeFicheiroPerguntas )
  {
    this.nomeDesafio=game_name;
    this.criadoPor = alcunhaJogadorCriador;
    this.alcunhasJogadores = new TreeSet < String > ();
    this.alcunhasJogadoresActivos = new TreeSet <String>();
    this.pontuacoesJogadores = new TreeMap < String, Integer > ();
    this.alcunhasJogadores.add( alcunhaJogadorCriador );
    this.pontuacoesJogadores.put( alcunhaJogadorCriador, 0);
    this.dataCriacao = dataCriacao;
    this.dataHoraInicioDesafio = dataHoraDesafio;
    temMinimoJogadores = false;
    this.estado = EstadoDesafio.EM_ESPERA;
    this.perguntasDesafio = new FicheiroPerguntas ( nomeFicheiroPerguntas );
    this.perguntasDesafio.carregaPerguntas();
  }

  public Desafio ( Desafio makeCopy ){
    this.nomeDesafio= makeCopy.getNomeDesafio();
    this.criadoPor = makeCopy.getCriadoPor();
    this.alcunhasJogadores = makeCopy.getAlcunhasJogadores();
    this.pontuacoesJogadores = makeCopy.getPontuacoesJogadores();
    this.dataCriacao = makeCopy.getDataCriacao();
    this.dataHoraInicioDesafio = makeCopy.getDataHoraInicioDesafio();
    this.temMinimoJogadores = makeCopy.getTemMinimoJogadores();
    this.estado = makeCopy.getEstado();
    this.perguntasDesafio = makeCopy.getPerguntasDesafio();
  }

  // Métodos Get
  public String getCriadoPor() {
    String novoCriadoPor = new String ( this.criadoPor);
    return novoCriadoPor;
  }

  public EstadoDesafio getEstado(){
    EstadoDesafio estadoRetornar = this.estado;
    return estadoRetornar;
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

  public TreeSet < String > getAlcunhasJogadoresActivos() {
    TreeSet < String > alcunhasActivas = new TreeSet < String > ();
    for ( String alcunha : this.alcunhasJogadoresActivos ){
      String copiaAlcunha = new String ( alcunha );
      alcunhasActivas.add( copiaAlcunha );
    }
    return alcunhasActivas;
  }

  /* Outros métodos */
  public boolean adicionaJogador ( String alcunha ){
    boolean resultado = false;
    if ( this.alcunhasJogadores.contains( alcunha )){
      resultado = false;
    }
    else{
      this.alcunhasJogadores.add( alcunha );
      this.pontuacoesJogadores.put( alcunha , 0 );
      this.temMinimoJogadores = true;
      resultado = true;
    }
    return resultado;
  }

  public boolean podeAdicionarJogador ( String alcunhaJogador ){
    boolean resultado = false;
    if ( this.alcunhasJogadores.contains( alcunhaJogador )){
      resultado = false;
    }
    else {
      resultado = true;
    }
    return resultado;
  }

  public boolean clienteParticipa(String alcunhaJogador ) {
    boolean resultado = false;
    if ( this.alcunhasJogadores.contains( alcunhaJogador )){
      resultado = true;
    }
    return resultado;
  }

  public void elimina(){
    this.estado = EstadoDesafio.ELIMINADO;
  }

  public void iniciaJogo(){
    this.estado = EstadoDesafio.EM_JOGO;
  }
  public void updateEstadoEsperaIniciaCancela(){
    Date agora = new Date();
    if ( agora.after(this.dataHoraInicioDesafio) ){
      if ( this.estado == EstadoDesafio.EM_ESPERA && this.temMinimoJogadores == false ){
        this.estado = EstadoDesafio.PASSOU_PRAZO;
      }
      else if ( this.estado == EstadoDesafio.EM_ESPERA && this.temMinimoJogadores == true ){
        this.estado = EstadoDesafio.EM_JOGO;

      }
    }
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

  public void rageQuit(String nomeJogador) {
    this.alcunhasJogadoresActivos.remove(nomeJogador);
  }

  public FicheiroPerguntas getPerguntasDesafio (){
    return this.perguntasDesafio;
  }

  public ArrayList < Pergunta > getPerguntas( ) {
    return this.perguntasDesafio.getArrayListPerguntas();
  }

  public boolean respondePergunta(String alcunhaClienteAssociado , int numeroQuestao, int escolha) {
    boolean resultado = false;
    int pontuacaoAnterior = this.pontuacoesJogadores.get(alcunhaClienteAssociado);
    if ( this.getPerguntasDesafio().getPergunta(numeroQuestao).acertou(escolha) ){
      pontuacaoAnterior += 2;
      resultado = true;
    }
    else {
      pontuacaoAnterior -= 1;
    }
    this.pontuacoesJogadores.put( alcunhaClienteAssociado, pontuacaoAnterior );
    return resultado;
  }

  public void quitPergunta(String alcunhaClienteAssociado) {
    // TODO Auto-generated method stub

  }
}
