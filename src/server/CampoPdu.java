/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

package server;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class CampoPdu {

  /* tamanho máximo do campo = 49152 - 256 */
  public static final int  TAMANHO_MAX_CAMPO = 48896;

  /* Campos dos tipos de pedidos que os clientes podem enviar aos servidores */
  public static final byte CLIENTE_NOME = (byte)1;
  public static final byte CLIENTE_ALCUNHA = (byte)2;
  public static final byte CLIENT_SEC_INFO = (byte)3;
  public static final byte CLIENTE_DATA = (byte)4;
  public static final byte CLIENTE_HORA = (byte)5;
  public static final byte CLIENTE_ESCOLHA = (byte)6;
  public static final byte CLIENTE_NOME_DESAFIO = (byte)7;
  public static final byte CLIENTE_NUM_QUESTAO = (byte)10;
  public static final byte CLIENTE_NUM_BLOCO = (byte)17;

  /* Campos dos tipos de respostas que os servidores podem enviar aos clientes */
  public static final byte SERVIDOR_OK = (byte)0;
  public static final byte SERVIDOR_ERRO = (byte) 255;
  public static final byte SERVIDOR_CONTINUA = (byte) 254;
  public static final byte SERVIDOR_NOME = (byte)1;
  public static final byte SERVIDOR_ALCUNHA = (byte)2;
  public static final byte SERVIDOR_DATA = (byte)4;
  public static final byte SERVIDOR_HORA = (byte)5;
  public static final byte SERVIDOR_NOME_DESAFIO = (byte)7;
  public static final byte SERVIDOR_NUM_QUESTAO = (byte)10;	  
  public static final byte SERVIDOR_TXT_QUESTAO = (byte)11;
  public static final byte SERVIDOR_NUM_RESPOSTA = (byte)12;
  public static final byte SERVIDOR_TXT_RESPOSTA = (byte)13;
  public static final byte SERVIDOR_RESPOSTA_CERTA = (byte)14;
  public static final byte SERVIDOR_PONTOS = (byte)15;
  public static final byte SERVIDOR_IMAGEM = (byte)16;
  public static final byte SERVIDOR_NUM_BLOCO = (byte)17;
  public static final byte SERVIDOR_AUDIO = (byte)18;
  public static final byte SERVIDOR_SCORE = (byte)20;

  /* Campos dos tipos de informação que os servidores podem difundir entre si */
  public static final byte INFO_NOME = (byte)1;
  public static final byte INFO_ALCUNHA = (byte)2;
  public static final byte INFO_NOME_DESAFIO = (byte)7;
  public static final byte INFO_DATA = (byte)4;
  public static final byte INFO_HORA = (byte)5;
  public static final byte INFO_NUM_QUESTAO = (byte)10;	  
  public static final byte INFO_TXT_QUESTAO = (byte)11;
  public static final byte INFO_NUM_RESPOSTA = (byte)12;
  public static final byte INFO_TXT_RESPOSTA = (byte)13;
  public static final byte INFO_RESPOSTA_CERTA = (byte)14;
  public static final byte INFO_IMAGEM = (byte)16;
  public static final byte INFO_MUSICA = (byte)19;
  public static final byte INFO_SCORE = (byte)20;
  public static final byte INFO_IP_SERVIDOR = (byte)30;
  public static final byte INFO_PORTA_SERVIDOR =(byte) 31;

  protected byte tipoCampo;
  protected byte[] dadosCampo;
  protected int tamanhoTotal;
  protected int tamanhoDados;
  protected byte[] tamanhoDadosBytes;
  protected int blocoNumero;
  protected boolean dadosParcelados;


  public CampoPdu ( byte tipo ){
    tipoCampo = tipo;
    tamanhoTotal = 3;
    tamanhoDados=0;
    blocoNumero=1;
    dadosParcelados = false;
    tamanhoDadosBytes = new byte [2];
  }

  public CampoPdu ( byte tipo , int numeroBloco , byte[] dados , int tamanhoDadosCopiar ){
    this.tipoCampo = tipo;
    this.tamanhoTotal = 3+tamanhoDadosCopiar;
    this.tamanhoDados = tamanhoDadosCopiar;
    tamanhoDadosBytes = new byte [2];
    this.tamanhoDadosBytes = this.intPara2Bytes(tamanhoDadosCopiar);
    this.blocoNumero= numeroBloco;
    this.dadosParcelados = true;
  }

  public boolean campoDadosParciais(){
    if ( this.tipoCampo == SERVIDOR_CONTINUA ){
      return true;
    }
    else {
      return false;
    }
  }

  public int getNumeroBloco(){
    return this.blocoNumero;
  }

  public void adicionaData( Date data ) {
    int ano =  data.getYear();
    int mes = data.getMonth();
    int dia = data.getDay();
    dadosCampo = new byte[6];
    byte[] buffer = new byte[2];
    buffer = intPara2Bytes( ano );
    dadosCampo[0] = buffer[0];
    dadosCampo[1] = buffer[1];
    buffer = intPara2Bytes( mes );
    dadosCampo[2] = buffer[0];
    dadosCampo[3] = buffer[1];
    buffer = intPara2Bytes( dia );
    dadosCampo[4] = buffer[0];
    dadosCampo[5] = buffer[1];
    tamanhoTotal+=6;
    tamanhoDados=6;
  }

  public int getCampoDataAno() {
    int temp0 = dadosCampo[0] & 0xFF;
    int temp1 = dadosCampo[1] & 0xFF;
    return ((temp0 << 8) + temp1);
  }

  public int getCampoDataMes() {
    int temp0 = dadosCampo[2] & 0xFF;
    int temp1 = dadosCampo[3] & 0xFF;
    return ((temp0 << 8) + temp1);
  }

  public int getCampoDataDia() {
    int temp0 = dadosCampo[4] & 0xFF;
    int temp1 = dadosCampo[5] & 0xFF;
    return ((temp0 << 8) + temp1);
  }

  public void adicionaHora( Date data ) {
    int horas =  data.getHours();
    int minutos = data.getMinutes();
    int segundos = data.getSeconds();
    dadosCampo = new byte[6];
    byte[] buffer = new byte[2];
    buffer = intPara2Bytes( horas );
    dadosCampo[0] = buffer[0];
    dadosCampo[1] = buffer[1];
    buffer = intPara2Bytes( minutos );
    dadosCampo[2] = buffer[0];
    dadosCampo[3] = buffer[1];
    buffer = intPara2Bytes( segundos );
    dadosCampo[4] = buffer[0];
    dadosCampo[5] = buffer[1];
    tamanhoTotal+=6;
    tamanhoDados=6;
  }

  public int getCampoHoraHora() {
    int temp0 = dadosCampo[0] & 0xFF;
    int temp1 = dadosCampo[1] & 0xFF;
    return ((temp0 << 8) + temp1);
  }

  public int getCampoHoraMinutos() {
    int temp0 = dadosCampo[2] & 0xFF;
    int temp1 = dadosCampo[3] & 0xFF;
    return ((temp0 << 8) + temp1);
  }

  public int getCampoHoraSegundos() {
    int temp0 = dadosCampo[4] & 0xFF;
    int temp1 = dadosCampo[5] & 0xFF;
    return ((temp0 << 8) + temp1);
  }

  public void adicionaInteiro1Byte( int aConverter ) {
    dadosCampo = new byte[1];
    dadosCampo[0] = (byte) (aConverter & 0xFF);
    tamanhoTotal+=1;
    tamanhoDados=1;
  }

  public void adicionaByteAZero( ) {
    dadosCampo = new byte[1];
    dadosCampo[0] = (byte) (0);
    tamanhoTotal+=1;
    tamanhoDados=1;
  }

  public void adicionaInteiro2Bytes( int aConverter ) {
    dadosCampo = new byte[2];
    dadosCampo[0] = (byte) (aConverter & 0xFF);
    dadosCampo[1] = (byte) ((aConverter >> 8) & 0xFF);
    tamanhoTotal+=2;
    tamanhoDados=2;
  }

  public void adicionaPortaAplicacional( int aConverter ) {
    dadosCampo = new byte[2];
    dadosCampo[0] = (byte) (aConverter & 0xFF);
    dadosCampo[1] = (byte) ((aConverter >> 8) & 0xFF);
    tamanhoTotal+=2;
    tamanhoDados=2;
  }

  public void adicionaEnderecoIPv4( InetAddress ip ) {
    dadosCampo = new byte[4];
    dadosCampo = ip.getAddress();
    tamanhoTotal+=4;
    tamanhoDados=4;
  }

  public void adicionaBytes ( byte[] bytesAInserir ) {
    int tamanhoAuxiliar = bytesAInserir.length;
    dadosCampo = Arrays.copyOf(bytesAInserir, tamanhoAuxiliar);
    tamanhoTotal+=tamanhoAuxiliar;
    tamanhoDados=tamanhoAuxiliar;
  }

  public static byte[] intPara2Bytes ( int aConverter ){ 
    byte[] data = new byte[2];
    data[0] = (byte) (aConverter & 0xFF);
    data[1] = (byte) ((aConverter >> 8) & 0xFF);
    return data;
  }

  public int getCampoInt1Byte (){
    return (int)dadosCampo[0] & 0xFF;
  }

  public int getCampoInt2Bytes (){
    int temp0 = dadosCampo[0] & 0xFF;
    int temp1 = dadosCampo[1] & 0xFF;
    return ((temp0 << 8) + temp1);
  }

  public void adicionaString ( String aConverter ){
    dadosCampo = aConverter.getBytes(StandardCharsets.UTF_8);
    int  tamanhoString = aConverter.length();
    tamanhoTotal+=tamanhoString;
    tamanhoDados=tamanhoString;
  }

  public ArrayList <CampoPdu> adicionaFicheiro ( String pathFicheiro ){
    ArrayList <CampoPdu> blocosFicheiroExtra = new ArrayList <CampoPdu> ();
    File file = new File( pathFicheiro );
    FileInputStream fis;
    try {
      fis = new FileInputStream(file);
      byte[] buf = new byte[TAMANHO_MAX_CAMPO];
      int readNum = 0;
      readNum = fis.read(buf);
      ByteArrayOutputStream bos_esteCampo = new ByteArrayOutputStream();
      bos_esteCampo.write(buf, 0, readNum); 
      dadosCampo = bos_esteCampo.toByteArray();
      tamanhoDados=bos_esteCampo.size();
      tamanhoTotal+=tamanhoDados;
      int numeroBloco = 2;
      for (; readNum != -1 && numeroBloco <= 250; numeroBloco++ ) {
        byte[] bufferNovosCampos = new byte[TAMANHO_MAX_CAMPO];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        readNum = fis.read(bufferNovosCampos);
        if( readNum > 0 ){
          bos.write(bufferNovosCampos, 0, readNum); 
          byte[] dadosCampoNovo = bos.toByteArray();
          CampoPdu novoCampoBloco = new CampoPdu ( this.tipoCampo , numeroBloco , dadosCampoNovo , bos.size() ); 
          blocosFicheiroExtra.add( novoCampoBloco);
        }
      }
      fis.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    if ( blocosFicheiroExtra.size() > 0 ){
      this.dadosParcelados = true;
    }
    return blocosFicheiroExtra;
  }

  public String getCampoString() throws UnsupportedEncodingException {
    String campoConvertido = new String( dadosCampo, StandardCharsets.UTF_8);  // example for one encoding type
    return campoConvertido;
  }

  public void paraStringDoByteArray ( byte[] bytes, int posArray ){
    ByteArrayOutputStream outByte = new ByteArrayOutputStream();
    for( int pos=posArray; bytes[ pos ]!=0 ; pos++ ){
      outByte.write( bytes[ pos ] );
    }
    tamanhoTotal += outByte.size();
    dadosCampo = outByte.toByteArray();
    tamanhoDados=outByte.size();
  }

  public byte[] getBytes (){
    byte aux[] = new byte[tamanhoTotal];
    aux[0]=tipoCampo;
    aux[1]= (byte) (tamanhoDados & 0xFF);
    aux[2]= (byte) ((tamanhoDados >> 8) & 0xFF);
    int pos = 3;
    for ( byte b : dadosCampo ){
      aux[pos]= b;
      pos++;
    }
    return aux;
  }

  private void paraDataDoByteArray(byte[] camposSeguintes, int posCamposSeguintes) {
    dadosCampo = Arrays.copyOfRange(camposSeguintes, posCamposSeguintes, 6);
    tamanhoTotal += 6;
    tamanhoDados= 6;
  }

  private void paraHoraDoByteArray(byte[] camposSeguintes, int posCamposSeguintes) {
    dadosCampo = Arrays.copyOfRange(camposSeguintes, posCamposSeguintes, 6);
    tamanhoTotal += 6;
    tamanhoDados= 6;
  }

  private void umByteAZeroDoByteArray(byte[] camposSeguintes, int posCamposSeguintes) {
    dadosCampo = Arrays.copyOfRange(camposSeguintes, posCamposSeguintes, 1);
    tamanhoTotal += 1;
    tamanhoDados= 1;
  }

  private void paraInteiro1ByteDoByteArray(byte[] camposSeguintes , int posCamposSeguintes) {
    dadosCampo = Arrays.copyOfRange(camposSeguintes, posCamposSeguintes, 1);
    tamanhoTotal += 1;	
    tamanhoDados= 1;
  }

  private void paraJpgDoByteArray(byte[] camposSeguintes , int posCamposSeguintes) {
    // TODO Auto-generated method stub

  }

  private void paraInteiro2BytesDoByteArray(byte[] camposSeguintes , int posCamposSeguintes) {
    dadosCampo = Arrays.copyOfRange(camposSeguintes, posCamposSeguintes, 2);
    tamanhoTotal += 2;
    tamanhoDados= 2;
  }

  private void paraAudioDoByteArray(byte[] camposSeguintes, int posCamposSeguintes) {
    // TODO Auto-generated method stub

  }

  private void paraIPv4DoByteArray(byte[] camposSeguintes , int posCamposSeguintes) {
    dadosCampo = Arrays.copyOfRange(camposSeguintes, posCamposSeguintes, 4);
    tamanhoTotal += 4;
    tamanhoDados= 4;
  }

  private void paraPortaAplicacionalDoByteArray(byte[] camposSeguintes , int posCamposSeguintes) {
    dadosCampo = Arrays.copyOfRange(camposSeguintes, posCamposSeguintes, 2);
    tamanhoTotal += 2;
    tamanhoDados= 2;
  }

  public void parseTamanhoCampo(byte[] b , int posCamposSeguintes) {
    int temp0 = b[posCamposSeguintes] & 0xFF;
    int temp1 = dadosCampo[posCamposSeguintes+1] & 0xFF;
    this.tamanhoDados =  ((temp0 << 8) + temp1);
    this.tamanhoDadosBytes[0] = b[posCamposSeguintes];
    this.tamanhoDadosBytes[1] = b[posCamposSeguintes+1];
  }

  public void parseDados(byte[] camposSeguintes, int posCamposSeguintes) {	
    this.parseTamanhoCampo( camposSeguintes, posCamposSeguintes);
    posCamposSeguintes+=2;

    switch( tipoCampo ){ 

      /* ************************************************************************ */
      /* Campos dos tipos de pedidos que os clientes podem enviar aos servidores  */
      /* ************************************************************************ */
      case CLIENTE_NOME :
        {
          paraStringDoByteArray( camposSeguintes , posCamposSeguintes );
          break;
        }
      case CLIENTE_ALCUNHA :
        {
          paraStringDoByteArray( camposSeguintes , posCamposSeguintes );
          break;
        }
      case CLIENT_SEC_INFO :
        { 
          paraStringDoByteArray( camposSeguintes , posCamposSeguintes );
          break; 
        }
      case CLIENTE_DATA :
        { 
          paraDataDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }
      case CLIENTE_HORA :
        { 
          paraHoraDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }
      case CLIENTE_ESCOLHA :
        { 
          paraInteiro1ByteDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }
      case CLIENTE_NOME_DESAFIO :
        { 
          paraStringDoByteArray( camposSeguintes , posCamposSeguintes );
          break; 
        }
      case CLIENTE_NUM_QUESTAO :
        { 
          paraInteiro1ByteDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }
      case CLIENTE_NUM_BLOCO :
        { 
          paraInteiro1ByteDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }

        /* ************************************************************************* */
        /* Campos dos tipos de respostas que os servidores podem enviar aos clientes */
        /* ************************************************************************* */

      case SERVIDOR_OK :
        { 
          umByteAZeroDoByteArray ( camposSeguintes , posCamposSeguintes );
          break; 
        }
      case SERVIDOR_ERRO :
        { 
          paraStringDoByteArray( camposSeguintes , posCamposSeguintes );
          break; 
        }
      case SERVIDOR_CONTINUA :
        { 
          umByteAZeroDoByteArray ( camposSeguintes , posCamposSeguintes );
          break; 
        }
        /* 
           case SERVIDOR_NOME : tem o mesmo codigo binario que CLIENTE_NOME
           case SERVIDOR_ALCUNHA : tem o mesmo codigo binario que CLIENTE_ALCUNHA
           case SERVIDOR_DATA : tem o mesmo codigo binario que CLIENTE_DATA
           case SERVIDOR_HORA : tem o mesmo codigo binario que CLIENTE_HORA
           case SERVIDOR_NOME_DESAFIO : tem o mesmo codigo binario que CLIENTE_NOME_DESAFIO
           case SERVIDOR_NUM_QUESTAO : tem o mesmo codigo binario que CLIENTE_NUM_QUESTAO
           */
      case SERVIDOR_TXT_QUESTAO :
        { 
          paraStringDoByteArray( camposSeguintes , posCamposSeguintes );
          break; 
        }
      case SERVIDOR_NUM_RESPOSTA :
        { 
          paraInteiro1ByteDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }
      case SERVIDOR_TXT_RESPOSTA :
        { 
          paraStringDoByteArray( camposSeguintes , posCamposSeguintes );
          break; 
        }
      case SERVIDOR_RESPOSTA_CERTA :
        { 
          paraInteiro1ByteDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }
      case SERVIDOR_PONTOS :
        {   
          paraInteiro1ByteDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }
      case SERVIDOR_IMAGEM :
        { 
          paraJpgDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }
        /* 
           case SERVIDOR_NUM_BLOCO :tem o mesmo codigo binario que CLIENTE_NUM_BLOCO
           */
      case SERVIDOR_AUDIO :
        { 
          paraAudioDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }
      case SERVIDOR_SCORE :
        { 
          paraInteiro2BytesDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }

        /* ************************************************************************ */
        /* Campos dos tipos de informação que os servidores podem difundir entre si */
        /* ************************************************************************ */

        /*
           case INFO_NOME : tem o mesmo codigo binario que CLIENTE_NOME
           case INFO_ALCUNHA : tem o mesmo codigo binario que CLIENTE_ALCUNHA
           case INFO_NOME_DESAFIO : tem o mesmo codigo binario que CLIENTE_NOME_DESAFIO
           case INFO_DATA : tem o mesmo codigo binario que CLIENTE_DATA
           case INFO_HORA : tem o mesmo codigo binario que CLIENTE_HORA
           case INFO_NUM_QUESTAO : tem o mesmo codigo binario que CLIENTE_NUM_QUESTAO
           case INFO_TXT_QUESTAO : tem o mesmo codigo binario que SERVIDOR_TXT_QUESTAO
           case INFO_NUM_RESPOSTA : tem o mesmo codigo binario que SERVIDOR_NUM_RESPOSTA
           case INFO_TXT_RESPOSTA : tem o mesmo codigo binario que SERVIDOR_TXT_RESPOSTA
           case INFO_RESPOSTA_CERTA : tem o mesmo codigo binario que SERVIDOR_RESPOSTA_CERTA
           case INFO_IMAGEM : tem o mesmo codigo binario que SERVIDOR_IMAGEM
           */
      case INFO_MUSICA :
        { 
          paraAudioDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }
        /* 
           case INFO_SCORE : tem o mesmo codigo binario que CLIENTE_SCORE
           */
      case INFO_IP_SERVIDOR :
        { 
          paraIPv4DoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }
      case INFO_PORTA_SERVIDOR :
        {   
          paraPortaAplicacionalDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }
    }
  }



  public int getTamanhoDados() {
    return tamanhoDados;
  }

  @Override
    public String toString()
    {
      StringBuilder s = new StringBuilder();
      s.append( "Campo: ");
      s.append( tipoCampo );
      s.append( " Tamanho Dados: ");
      s.append( this.getTamanhoDados() );
      return s.toString();
    }

  public boolean mesmoTipo(byte tipoCampoTeste ) {
    boolean resultado = false; 
    if (this.tipoCampo == tipoCampoTeste ){
      resultado = true;
    }
    return resultado;
  }
}
