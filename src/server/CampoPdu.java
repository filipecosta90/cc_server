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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class CampoPdu {

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
    this.tamanhoDadosBytes = intPara2Bytes(tamanhoDadosCopiar);
    this.blocoNumero= numeroBloco;
    this.dadosParcelados = true;
  }

  public boolean campoDadosParciais(){
    if ( this.tipoCampo == ServerCodes.SERVIDOR_CONTINUA ){
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
    dadosCampo = intPara1Byte ( aConverter );
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
    dadosCampo= intPara2Bytes ( aConverter);
    tamanhoTotal+=2;
    tamanhoDados=2;
  }

  public void adicionaPortaAplicacional( int aConverter ) {
    dadosCampo = new byte[2];
    dadosCampo= intPara2Bytes ( aConverter);
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


  public int doisBytesParaInt ( byte[] data){
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);
    // by choosing big endian, high order bytes must be put
    // to the buffer before low order bytes
    byteBuffer.order(ByteOrder.BIG_ENDIAN);
    // since ints are 4 bytes (32 bit), you need to put all 4, so put 0
    // for the high order bytes
    byteBuffer.put((byte)0x00);
    byteBuffer.put((byte)0x00);
    byteBuffer.put((byte)data[1]);
    byteBuffer.put((byte)data[0]);
    byteBuffer.flip();
    int valor = byteBuffer.getInt();
    System.out.println("dois bytes "+ byteBuffer.get(3) + byteBuffer.get(2) + byteBuffer.get(1) + byteBuffer.get(0) +") para int" + valor);

    return valor;
  }

  public int doisBytesParaIntStart ( byte[] data , int start ){
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);
    // by choosing big endian, high order bytes must be put
    // to the buffer before low order bytes
    byteBuffer.order(ByteOrder.BIG_ENDIAN);
    // since ints are 4 bytes (32 bit), you need to put all 4, so put 0
    // for the high order bytes
    byteBuffer.put((byte)0x00);
    byteBuffer.put((byte)0x00);
    byteBuffer.put((byte)data[1+start]);
    byteBuffer.put((byte)data[0+start]);
    byteBuffer.flip();
    int valor = byteBuffer.getInt();
    System.out.println("dois bytes "+ byteBuffer.get(3) + byteBuffer.get(2) + byteBuffer.get(1) + byteBuffer.get(0) +") para int" + valor);

    return valor;
  }

  public int umByteParaInt ( byte[] data ){
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4);
    // by choosing big endian, high order bytes must be put
    // to the buffer before low order bytes
    byteBuffer.order(ByteOrder.BIG_ENDIAN);
    // since ints are 4 bytes (32 bit), you need to put all 4, so put 0
    // for the high order bytes
    byteBuffer.put((byte)0x00);
    byteBuffer.put((byte)0x00);
    byteBuffer.put((byte)0x00);
    byteBuffer.put((byte) data[0]);
    byteBuffer.flip();
    int valor = byteBuffer.getInt();
    System.out.println("dois bytes "+ byteBuffer.get(3) + byteBuffer.get(2) + byteBuffer.get(1) + byteBuffer.get(0) +") para int" + valor);

    return valor;
  }

  public byte[] intPara2Bytes ( int aConverter ){ 
    ByteBuffer bb = ByteBuffer.allocate(4); 
    bb.putInt(aConverter); 
    byte[] arrayR = new byte [2];
    arrayR[0]=bb.get(3);
    arrayR[1]=bb.get(2);
    System.out.println("int ("+ aConverter +") para 2 bytes " + arrayR[1]+arrayR[0]);
    return arrayR;
  }

  public static byte[] intPara1Byte ( int aConverter ){ 
    ByteBuffer bb = ByteBuffer.allocate(4); 
    bb.putInt(aConverter); 
    byte[] arrayR = new byte [1];
    arrayR[0]=bb.get(3);
    System.out.println(arrayR[0]);
    System.out.println("int ("+ aConverter +") para 1 bytes " + arrayR[0]);
    return arrayR;
  }

  public int getCampoInt1Byte (){
    return umByteParaInt(dadosCampo);
  }

  public int getCampoInt2Bytes (){
    return doisBytesParaInt(dadosCampo);
  }

  public void adicionaString ( String aConverter ){
    dadosCampo = aConverter.getBytes(StandardCharsets.UTF_8);
    tamanhoTotal+= dadosCampo.length;
    tamanhoDados = dadosCampo.length;
  }

  public ArrayList <CampoPdu> adicionaFicheiro ( String pathFicheiro ){
    ArrayList <CampoPdu> blocosFicheiroExtra = new ArrayList <CampoPdu> ();
    File file = new File( pathFicheiro );
    FileInputStream fis;
    try {
      fis = new FileInputStream(file);
      byte[] buf = new byte[ServerCodes.TAMANHO_MAX_CAMPO];
      int readNum = 0;
      readNum = fis.read(buf);
      ByteArrayOutputStream bos_esteCampo = new ByteArrayOutputStream();
      bos_esteCampo.write(buf, 0, readNum); 
      dadosCampo = bos_esteCampo.toByteArray();
      tamanhoDados=bos_esteCampo.size();
      tamanhoTotal+=tamanhoDados;
      int numeroBloco = 2;
      for (; readNum != -1 && numeroBloco <= 250; numeroBloco++ ) {
        byte[] bufferNovosCampos = new byte[ServerCodes.TAMANHO_MAX_CAMPO];
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

  public void paraStringDoByteArray ( byte[] bytes, int posArray , int tamanhoCampo ){
    ByteArrayOutputStream outByte = new ByteArrayOutputStream();
    for( int pos=posArray; pos < (tamanhoCampo+posArray); pos++ ){
      outByte.write( bytes[ pos ] );
      System.out.print( bytes[pos] + "("+ pos +")");
    }
    tamanhoTotal += outByte.size();
    dadosCampo = outByte.toByteArray();
    tamanhoDados=outByte.size();
  }

  public byte[] getBytes (){
    byte aux[] = new byte[tamanhoTotal];
    aux[0]=tipoCampo;
    byte[] tamanhoBytes = this.intPara2Bytes(tamanhoDados);
    System.out.println( " tamanhoDados:" + dadosCampo.length + " tamanhoTotal: " +  tamanhoTotal + " comPFinal:" +  aux.length + " tamanhoDados:" + tamanhoDados );
    aux[1]= tamanhoBytes[0];
    aux[2]= tamanhoBytes[1];
    int pos = 0;
    while ( pos < tamanhoDados ){
      aux[pos+3]= dadosCampo[pos];
      pos++;
    }
    return aux;
  }

  private void paraDataDoByteArray(byte[] camposSeguintes, int posCamposSeguintes) {
    dadosCampo = Arrays.copyOfRange(camposSeguintes, posCamposSeguintes, posCamposSeguintes+6);
    tamanhoTotal += 6;
    tamanhoDados= 6;
  }

  private void paraHoraDoByteArray(byte[] camposSeguintes, int posCamposSeguintes) {
    dadosCampo = Arrays.copyOfRange(camposSeguintes, posCamposSeguintes, posCamposSeguintes+6);
    tamanhoTotal += 6;
    tamanhoDados= 6;
  }

  private void umByteAZeroDoByteArray(byte[] camposSeguintes, int posCamposSeguintes) {
    dadosCampo = Arrays.copyOfRange(camposSeguintes, posCamposSeguintes, posCamposSeguintes+1);
    tamanhoTotal += 1;
    tamanhoDados= 1;
  }

  private void paraInteiro1ByteDoByteArray(byte[] camposSeguintes , int posCamposSeguintes) {
    dadosCampo = Arrays.copyOfRange(camposSeguintes, posCamposSeguintes, posCamposSeguintes+1);
    tamanhoTotal += 1;	
    tamanhoDados= 1;
  }

  private void paraJpgDoByteArray(byte[] camposSeguintes , int posCamposSeguintes) {
    // TODO Auto-generated method stub

  }

  private void paraInteiro2BytesDoByteArray(byte[] camposSeguintes , int posCamposSeguintes) {
    dadosCampo = Arrays.copyOfRange(camposSeguintes, posCamposSeguintes, posCamposSeguintes+2);
    tamanhoTotal += 2;
    tamanhoDados= 2;
  }

  private void paraAudioDoByteArray(byte[] camposSeguintes, int posCamposSeguintes) {
    // TODO Auto-generated method stub

  }

  private void paraIPv4DoByteArray(byte[] camposSeguintes , int posCamposSeguintes) {
    dadosCampo = Arrays.copyOfRange(camposSeguintes, posCamposSeguintes, posCamposSeguintes+4);
    tamanhoTotal += 4;
    tamanhoDados= 4;
  }

  private void paraPortaAplicacionalDoByteArray(byte[] camposSeguintes , int posCamposSeguintes) {
    dadosCampo = Arrays.copyOfRange(camposSeguintes, posCamposSeguintes, posCamposSeguintes+2);
    tamanhoTotal += 2;
    tamanhoDados= 2;
  }

  public void parseTamanhoCampo(byte[] b , int posCamposSeguintes) {
    this.tamanhoDados = doisBytesParaIntStart(b , posCamposSeguintes); 
    this.tamanhoDadosBytes[0] = b[posCamposSeguintes];
    this.tamanhoDadosBytes[1] = b[posCamposSeguintes+1];
  }

  public void parseDados(byte[] camposSeguintes, int posCamposSeguintes , int tamanhoCampo ) {	

    switch( tipoCampo ){ 

      /* ************************************************************************ */
      /* Campos dos tipos de pedidos que os clientes podem enviar aos servidores  */
      /* ************************************************************************ */
      case ServerCodes.CLIENTE_NOME :
        {
          paraStringDoByteArray( camposSeguintes , posCamposSeguintes , tamanhoCampo );
          break;
        }
      case ServerCodes.CLIENTE_ALCUNHA :
        {
          paraStringDoByteArray( camposSeguintes , posCamposSeguintes , tamanhoCampo );
          break;
        }
      case ServerCodes.CLIENTE_SEC_INFO :
        { 
          paraStringDoByteArray( camposSeguintes , posCamposSeguintes , tamanhoCampo );
          break; 
        }
      case ServerCodes.CLIENTE_DATA :
        { 
          paraDataDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }
      case ServerCodes.CLIENTE_HORA :
        { 
          paraHoraDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }
      case ServerCodes.CLIENTE_ESCOLHA :
        { 
          paraInteiro1ByteDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }
      case ServerCodes.CLIENTE_NOME_DESAFIO :
        { 
          paraStringDoByteArray( camposSeguintes , posCamposSeguintes , tamanhoCampo );
          break; 
        }
      case ServerCodes.CLIENTE_NUM_QUESTAO :
        { 
          paraInteiro1ByteDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }
      case ServerCodes.CLIENTE_NUM_BLOCO :
        { 
          paraInteiro1ByteDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }

        /* ************************************************************************* */
        /* Campos dos tipos de respostas que os servidores podem enviar aos clientes */
        /* ************************************************************************* */

      case ServerCodes.SERVIDOR_OK :
        { 
          umByteAZeroDoByteArray ( camposSeguintes , posCamposSeguintes );
          break; 
        }
      case ServerCodes.SERVIDOR_ERRO :
        { 
          paraStringDoByteArray( camposSeguintes , posCamposSeguintes , tamanhoCampo );
          break; 
        }
      case ServerCodes.SERVIDOR_CONTINUA :
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
      case ServerCodes.SERVIDOR_TXT_QUESTAO :
        { 
          paraStringDoByteArray( camposSeguintes , posCamposSeguintes , tamanhoCampo );
          break; 
        }
      case ServerCodes.SERVIDOR_NUM_RESPOSTA :
        { 
          paraInteiro1ByteDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }
      case ServerCodes.SERVIDOR_TXT_RESPOSTA :
        { 
          paraStringDoByteArray( camposSeguintes , posCamposSeguintes , tamanhoCampo );
          break; 
        }
      case ServerCodes.SERVIDOR_RESPOSTA_CERTA :
        { 
          paraInteiro1ByteDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }
      case ServerCodes.SERVIDOR_PONTOS :
        {   
          paraInteiro1ByteDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }
      case ServerCodes.SERVIDOR_IMAGEM :
        { 
          paraJpgDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }
        /* 
           case SERVIDOR_NUM_BLOCO :tem o mesmo codigo binario que CLIENTE_NUM_BLOCO
           */
      case ServerCodes.SERVIDOR_AUDIO :
        { 
          paraAudioDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }
      case ServerCodes.SERVIDOR_SCORE :
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
      case ServerCodes.INFO_MUSICA :
        { 
          paraAudioDoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }
        /* 
           case INFO_SCORE : tem o mesmo codigo binario que CLIENTE_SCORE
           */
      case ServerCodes.INFO_IP_SERVIDOR :
        { 
          paraIPv4DoByteArray ( camposSeguintes , posCamposSeguintes);
          break; 
        }
      case ServerCodes.INFO_PORTA_SERVIDOR :
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
