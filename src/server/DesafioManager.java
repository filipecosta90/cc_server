/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

package server;

import java.io.Serializable;
import java.util.HashMap;

import server.Desafio.EstadoDesafio;

public class DesafioManager implements Runnable , Serializable { 

	private static final long serialVersionUID = -4861608614717777756L;
	
private Server localServerPointer;
  private Desafio desafioAGerir;
  private int perguntaEnviar;
  private HashMap < String , Coneccao > mapConeccoes;

  DesafioManager ( Server localServer , Desafio desafio ){
    this.localServerPointer = localServer;
    this.desafioAGerir = desafio;
    mapConeccoes = new HashMap < String , Coneccao >();
    perguntaEnviar = 1;
  }

  public String getNomeDesafioGerir() {
    return this.desafioAGerir.getNomeDesafio();
  }

  public boolean aceitaDesafio(String alcunhaClienteAssociar) {
    boolean resultado = false;
    resultado = this.desafioAGerir.adicionaJogador( alcunhaClienteAssociar );
    return resultado;
  }

  public void updateConeccoes(){
    for ( String nomeJogador : desafioAGerir.getAlcunhasJogadoresActivos() ){
      Coneccao coneccaoRetornada = this.localServerPointer.getConeccaoCliente(nomeJogador);
      if ( coneccaoRetornada == null ){
        this.desafioAGerir.rageQuit( nomeJogador );
        this.mapConeccoes.remove( nomeJogador );
      }
      Coneccao coneccaoNoDesafio = mapConeccoes.get( nomeJogador );
      if (  coneccaoRetornada.mesmoEnderecoPortaSocket ( coneccaoNoDesafio ) == false ){
        coneccaoNoDesafio.copiaEnderecoPortaSocket ( coneccaoRetornada );
        mapConeccoes.remove( nomeJogador);
        mapConeccoes.put( nomeJogador , coneccaoNoDesafio );
      }
    }
  }

  @Override
    public void run() {
      System.out.println("Criado um novo gestor para o desafio: " + desafioAGerir.getNomeDesafio() );
      System.out.println("Date e hora da Criação: " + desafioAGerir.getDataCriacao() );
      System.out.println("Date e hora do desafio: " + desafioAGerir.getDataHoraInicioDesafio() );
      System.out.println("Estado actual " + desafioAGerir.getEstado() );
      while ( desafioAGerir.estado == EstadoDesafio.EM_ESPERA ){
        desafioAGerir.updateEstadoEsperaIniciaCancela();
      }
      while( desafioAGerir.estado == EstadoDesafio.EM_JOGO ){
          System.out.println("Desafio " + desafioAGerir.getNomeDesafio() + " iniciado: "+ desafioAGerir.getEstado() );
        for ( Pergunta perguntaActual : desafioAGerir.getPerguntas() ){
          this.updateConeccoes();
          if ( perguntaEnviar <= 10 ){
            try {
              this.enviaPergunta( this.getNomeDesafioGerir(), perguntaEnviar,  perguntaActual);
              perguntaEnviar++;   
              Thread.sleep( 60000 );
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }

      }
      if (desafioAGerir.estado == EstadoDesafio.ELIMINADO ){
        this.localServerPointer.EliminaDesafio( desafioAGerir.getNomeDesafio() , desafioAGerir.getCriadoPor());
      }
      else if ( desafioAGerir.estado == EstadoDesafio.PASSOU_PRAZO ){
        this.localServerPointer.EliminaDesafio( desafioAGerir.getNomeDesafio() , desafioAGerir.getCriadoPor());
      }
      else if ( desafioAGerir.estado == EstadoDesafio.TERMINADO ){
        this.localServerPointer.TerminaDesafio( desafioAGerir.getNomeDesafio() , desafioAGerir.getCriadoPor());
      }
    }

  private void enviaPergunta(String nomeDesafio , int numeroPergunta , Pergunta perguntaActual) throws Exception {
    for ( Coneccao coneccaoActual : mapConeccoes.values() ){
      coneccaoActual.enviaPergunta( nomeDesafio , numeroPergunta , perguntaActual);
    }
  }

  public Desafio getDesafio(){
    return this.desafioAGerir;
  }

  @Override 
    public boolean equals ( Object other ){
      boolean resultado = false;
      if (other instanceof DesafioManager) {
        DesafioManager that = (DesafioManager) other;
        resultado = this.getNomeDesafioGerir().equals(that.getNomeDesafioGerir());
      }
      return resultado;
    }
}
