/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

package server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;

public class BasePdu {

  // Tipos de pedido servidor
  public static final byte REPLY = 0;
  public static final byte HELLO = 1;
  public static final byte REGISTER = 2;
  public static final byte LOGIN = 3;
  public static final byte LOGOUT = 4;
  public static final byte QUIT = 5;
  public static final byte END = 6;
  public static final byte LIST_CHALLENGES = 7;
  public static final byte MAKE_CHALLENGE = 8;
  public static final byte ACCEPT_CHALLENGE = 9;
  public static final byte DELETE_CHALLENGE = 10;
  public static final byte ANSWER = 11;
  public static final byte RETRANSMIT = 12;
  public static final byte LIST_RANKING = 13;
  public static final byte INFO = 14;
  public static final int TAMANHO_MAX_PDU = 256;

  protected byte versao[];
  protected byte seguranca[];
  protected byte label[];
  protected byte tipo[];
  protected byte numeroCamposSeguintes[];
  protected byte tamanhoBytesCamposSeguintes[];
  private ArrayList<CampoPdu> ArrayListCamposSeguintes;
  protected byte camposSeguintes[];

  protected DatagramPacket pacoteUdp;
  private int  tamanhoPdu;
  private int tamanhoCamposSeguintes;
  private byte[] rawData;
  ByteArrayInputStream inputByteArray;

  public BasePdu ( ) { 
    versao = new byte[1];
    seguranca = new byte[1];
    label = new byte[2];
    tipo = new byte[1];
    numeroCamposSeguintes =  new byte[1];
    tamanhoBytesCamposSeguintes = new byte[2];
    tamanhoCamposSeguintes = 0;
    tamanhoPdu = 8;
  }

  public DatagramPacket getPacote() { 
    return pacoteUdp; 
  }

  public boolean parseCabecalho( DatagramPacket pacote ) {
    rawData = pacote.getData();
    inputByteArray = new ByteArrayInputStream( rawData );
    tamanhoPdu = inputByteArray.available();
    if( tamanhoPdu >= 8 ){
      inputByteArray.read(versao , 0 , 1 );
      inputByteArray.read( seguranca , 0 , 1);
      inputByteArray.read(label, 0, 2);
      inputByteArray.read( tipo , 0 , 1);
      inputByteArray.read( numeroCamposSeguintes , 0 , 1);
      inputByteArray.read( tamanhoBytesCamposSeguintes , 0 , 2);
      Byte Decimal = new Byte (tamanhoBytesCamposSeguintes[0]);
      Byte Unit = new Byte (tamanhoBytesCamposSeguintes[1]);
      int tamanhoCamposSeguintes = Decimal.intValue() * 10 + Unit.intValue();
      if ( tamanhoCamposSeguintes > 0 ) {
        camposSeguintes = new byte[tamanhoCamposSeguintes];
        inputByteArray.read( camposSeguintes , 0 ,  tamanhoCamposSeguintes );
      }
      return true;
    }
    else{
      return false;
    }
  }

  @Override
    public String toString()
    {
      StringBuilder s = new StringBuilder();
      s.append( "********** PDU Base **********");
      s.append( "\nversao: "+ versao[0] );
      s.append( "\tsegurancao: "+ seguranca[0] );
      s.append( "nlabel: "+ label[0]+label[1] );
      s.append( "\ttipo: "+ tipo[0] );
      s.append( "\nnum de campos seguintes: "+ numeroCamposSeguintes[0] );
      s.append( "\ttam de campos seguintes: "+ tamanhoCamposSeguintes );
      s.append( "\ntamanho total: " + tamanhoPdu );
      if ( tamanhoCamposSeguintes > 0){
        s.append( "\t----- INICIO CAMPOS -----");
        for (CampoPdu t : ArrayListCamposSeguintes ) {
          s.append("\t");
          s.append(t.toString());
          s.append("\n");
        }
        s.append( "\t-----  FIM  CAMPOS  -----");
      }
      s.append( "********** FIM PDU Base **********");

      return s.toString();
    }

  /* Métodos auxiliares */
  public void preparaEnvio() throws IOException {
    ByteArrayOutputStream novoOut = new ByteArrayOutputStream();
    for (CampoPdu t : ArrayListCamposSeguintes ) {
      novoOut.write(t.getBytes());
    }
    tamanhoCamposSeguintes = novoOut.size();
    camposSeguintes = novoOut.toByteArray();
  }

  public void parseCampos() throws IOException {
    int posCamposSeguintes = 0;
    int tamanhoCampoLido = 0;
    while ( posCamposSeguintes < tamanhoCamposSeguintes ){
      CampoPdu campo = new CampoPdu ( camposSeguintes[posCamposSeguintes] );
      posCamposSeguintes++;
      campo.parseDados ( camposSeguintes , posCamposSeguintes );
      tamanhoCampoLido = campo.getTamanhoDados();
      posCamposSeguintes += tamanhoCampoLido;
      ArrayListCamposSeguintes.add( campo );
    }
  }
}
