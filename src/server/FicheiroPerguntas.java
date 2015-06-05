package server;

import java.util.TreeMap;

public class FicheiroPerguntas {
  TreeMap < Integer , Pergunta > tabelaPerguntas;
  String pathFicheiro;

  public boolean carregaPerguntas (){
    boolean resultado = false;
    return resultado;
  }

  public TreeMap < Integer , Pergunta > getMapPerguntas(){
    return this.tabelaPerguntas;
  }

}
