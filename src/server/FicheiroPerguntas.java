/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

package server;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;


/* xml */
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class FicheiroPerguntas implements Serializable {

  private static final long serialVersionUID = 5365520557990456604L;

  private ArrayList <Pergunta> tabelaPerguntas;
  private String pathFicheiro;
  private String musicDir;
  private String imagesDir;
  private int numeroQuestoes;

  public FicheiroPerguntas ( String path ){
    this.pathFicheiro = path;
    this.tabelaPerguntas = new ArrayList < Pergunta >();
  }

  public boolean carregaPerguntas (){
    boolean resultado = false;
    try {	
      File ficheiroAbrir = new File ( this.pathFicheiro );
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse( ficheiroAbrir );
      doc.getDocumentElement().normalize();
      musicDir = doc.getDocumentElement().getAttribute("musicDir");
      imagesDir = doc.getDocumentElement().getAttribute("imagesDir");
      NodeList nList = doc.getElementsByTagName("pergunta");
      numeroQuestoes = nList.getLength();
      for (int posXML = 0; posXML < (nList.getLength()); posXML++) {
        Node nNode = nList.item(posXML);
        if (nNode.getNodeType() == Node.ELEMENT_NODE) {
          Element eElement = (Element) nNode;
          String ficheiroAudio = new String ( musicDir );
          ficheiroAudio+="/";
          ficheiroAudio+= eElement.getAttribute("audio");
          String ficheiroImagem = new String ( imagesDir );
          ficheiroImagem+="/";
          ficheiroImagem+= eElement.getAttribute("imagem");
          String questao = eElement.getAttribute("questao");
          String opcoes[] = new String [3];
          opcoes[0] = eElement.getAttribute("opcao1");
          opcoes[1] = eElement.getAttribute("opcao2");
          opcoes[2] = eElement.getAttribute("opcao3");
          int opcaoCorrecta = Integer.parseInt (eElement.getAttribute("correcta"));
          Pergunta novaPergunta = new Pergunta ( ficheiroAudio , ficheiroImagem , questao , opcoes , opcaoCorrecta );
          this.tabelaPerguntas.add(  novaPergunta );
        }
      }
      resultado = true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return resultado;
  }

  public ArrayList < Pergunta > getArrayListPerguntas(){
    return this.tabelaPerguntas;
  }

  public Pergunta getPergunta(int numeroQuestao) {
    int posicaoArray = numeroQuestao -1;
    return this.tabelaPerguntas.get(posicaoArray);
  }

}
