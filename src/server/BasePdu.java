/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

package server;

import server.CampoPdu;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Date;

public class BasePdu {

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
  public static final byte SERVIDOR_DATA_DESAFIO = (byte)4;
  public static final byte SERVIDOR_HORA_DESAFIO = (byte)5;
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
  private boolean esperaDadosNovoPacote;
  ByteArrayInputStream inputByteArray;
  private int numeroCamposSeguintesInt;
  private int posPopCamposSeguintes;

  public BasePdu ( DatagramPacket pacote ) { 
    versao = new byte[1];
    seguranca = new byte[1];
    label = new byte[2];
    tipo = new byte[1];
    numeroCamposSeguintes =  new byte[1];
    tamanhoBytesCamposSeguintes = new byte[2];
    tamanhoCamposSeguintes = 0;
    tamanhoPdu = 8;
    rawData = pacote.getData();
    esperaDadosNovoPacote = false;
    numeroCamposSeguintesInt = 0;
    posPopCamposSeguintes = 0;
    this.ArrayListCamposSeguintes = new ArrayList <CampoPdu>();
  }

  public BasePdu ( byte tipo , byte[] label ) { 
    versao = new byte[1];
    seguranca = new byte[1];
    this.label = new byte[2];
    this.label[0] = label[0];
    this.label[1] = label[1];
    this.tipo = new byte[1];
    this.tipo[0] = tipo;
    numeroCamposSeguintes =  new byte[1];
    tamanhoBytesCamposSeguintes = new byte[2];
    tamanhoCamposSeguintes = 0;
    tamanhoPdu = 8;
    esperaDadosNovoPacote = false;
    numeroCamposSeguintesInt = 0;
    posPopCamposSeguintes = 0;
    this.ArrayListCamposSeguintes = new ArrayList <CampoPdu>();
  }

  public BasePdu ( byte[] versaoC, byte[] segurancaC , byte[] labelC , byte tipoC[] ) { 
    this.versao = new byte[1];
    this.versao = versaoC;
    this.seguranca = new byte[1];
    this.seguranca = segurancaC;
    this.label = new byte[2];
    this.label[0] = labelC[0];
    this.label[1] = labelC[1];
    this.tipo = new byte[1];
    this.tipo = tipoC;
    numeroCamposSeguintes =  new byte[1];
    tamanhoBytesCamposSeguintes = new byte[2];
    tamanhoCamposSeguintes = 0;
    tamanhoPdu = 8;
    esperaDadosNovoPacote = false;
    numeroCamposSeguintesInt = 0;
    posPopCamposSeguintes = 0;
    this.ArrayListCamposSeguintes = new ArrayList <CampoPdu>();
  }

  private byte[] getVersao() {
    return this.versao;
  }

  private byte[] getSeguranca() {
    return this.seguranca;
  }

  private byte[] getLabel() {
    return this.label;
  }

  public byte[] getTipo() {
    return this.tipo;
  }

  public synchronized void mergePDU ( BasePdu toMerge ){
    this.tamanhoPdu += toMerge.getTamanhoCamposSeguintesPdu();
    for ( CampoPdu toMergeCampo : toMerge.getArrayListCamposSeguintes() ){
      this.ArrayListCamposSeguintes.add( toMergeCampo);
    }
    this.numeroCamposSeguintesInt += toMerge.getNumeroCamposSeguintesInt();
    if( toMerge.dadosParciais() ){
      this.esperaDadosNovoPacote = true;
    }
    else {
      this.esperaDadosNovoPacote = false;
    }
  }

  public int getNumeroCamposSeguintesInt() {
    return this.numeroCamposSeguintesInt;
  }

  public int getTamanhoCamposSeguintesPdu(){
    return this.tamanhoCamposSeguintes;
  }

  public ArrayList < CampoPdu > getArrayListCamposSeguintes(){
    ArrayList < CampoPdu > novoArray = new ArrayList <CampoPdu>();
    for ( CampoPdu copyCampo : ArrayListCamposSeguintes ){
      novoArray.add( copyCampo );
    }
    return novoArray;
  }

  public boolean dadosParciais (){
    return esperaDadosNovoPacote;
  }

  public boolean parseCabecalho( ) {
    inputByteArray = new ByteArrayInputStream( rawData );
    tamanhoPdu = inputByteArray.available();
    if( tamanhoPdu >= 8 ){
      inputByteArray.read(versao , 0 , 1 );
      inputByteArray.read( seguranca , 0 , 1);
      inputByteArray.read(label, 0, 2);
      inputByteArray.read( tipo , 0 , 1);
      inputByteArray.read( numeroCamposSeguintes , 0 , 1);
      Byte UnitNumeroSeguintes = new Byte (numeroCamposSeguintes[0]);
      numeroCamposSeguintesInt = UnitNumeroSeguintes.intValue();
      inputByteArray.read( tamanhoBytesCamposSeguintes , 0 , 2);
      Byte Decimal = new Byte (tamanhoBytesCamposSeguintes[0]);
      Byte Unit = new Byte (tamanhoBytesCamposSeguintes[1]);
      int tamanhoCamposSeguintes = Decimal.intValue() * 10 + Unit.intValue();
      this.tamanhoPdu += tamanhoCamposSeguintes;
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
    numeroCamposSeguintesInt = ArrayListCamposSeguintes.size();
    this.numeroCamposSeguintes = CampoPdu.intPara2Bytes ( numeroCamposSeguintesInt );
    tamanhoCamposSeguintes = novoOut.size();
    this.tamanhoBytesCamposSeguintes = CampoPdu.intPara2Bytes ( tamanhoCamposSeguintes );
    camposSeguintes = novoOut.toByteArray();
    tamanhoPdu = 8 + tamanhoCamposSeguintes;
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    outBytes.write( this.versao );
    outBytes.write( this.seguranca );
    outBytes.write( this.label );
    outBytes.write( this.tipo );
    outBytes.write( this.tamanhoBytesCamposSeguintes );
    outBytes.write( this.camposSeguintes );
    rawData = outBytes.toByteArray();
  }

  /* Métodos auxiliares */
  public byte[] getBytesEnvio()  {
    return rawData;
  }

  public void parseCampos()  {
    int posCamposSeguintes = 0;
    int tamanhoCampoLido = 0;
    while ( posCamposSeguintes < tamanhoCamposSeguintes ){
      CampoPdu campo = new CampoPdu ( camposSeguintes[posCamposSeguintes] );
      if(campo.campoDadosParciais()){
        this.esperaDadosNovoPacote = true;
      }
      posCamposSeguintes++;
      campo.parseTamanhoCampo( camposSeguintes , posCamposSeguintes) ;
      posCamposSeguintes+=2;
      campo.parseDados ( camposSeguintes , posCamposSeguintes );
      tamanhoCampoLido = campo.getTamanhoDados();
      posCamposSeguintes += tamanhoCampoLido;
      this.ArrayListCamposSeguintes.add( campo );
    }
    numeroCamposSeguintesInt = ArrayListCamposSeguintes.size();
  }

  public boolean MesmaLabel(BasePdu pacoteATestar) {
    byte[] testLabel = pacoteATestar.getLabel();
    if ( this.label[0] ==  testLabel[0] && this.label[1] ==  testLabel[1] ){
      return true;
    }
    else {
      return false;
    }
  }

  public boolean pduCompleto() {
    return esperaDadosNovoPacote;
  }

  public int getTamanhoTotalPdu() {
    return this.tamanhoPdu;
  }

  public void replyOK() throws IOException {
    CampoPdu campoOk = new CampoPdu ( SERVIDOR_OK );
    campoOk.adicionaByteAZero();
    this.adicionaCampoPdu(campoOk);
    this.preparaEnvio();
  }

  private void adicionaCampoPdu(CampoPdu campoInserir) {
    this.tamanhoCamposSeguintes += campoInserir.tamanhoTotal;
    this.ArrayListCamposSeguintes.add(campoInserir);
  }

  public void replyErro( String descricaoErro ) throws IOException {
    CampoPdu campoErro = new CampoPdu ( SERVIDOR_ERRO );
    campoErro.adicionaString( descricaoErro );
    this.adicionaCampoPdu(campoErro);
    this.preparaEnvio();
  }

  public void replyAlcunhaScore( String alcunhaCliente , int scoreCliente ) throws IOException {
    CampoPdu campoAlcunha = new CampoPdu ( SERVIDOR_ALCUNHA );
    campoAlcunha.adicionaString( alcunhaCliente );
    this.adicionaCampoPdu(campoAlcunha);
    CampoPdu campoPontos = new CampoPdu ( SERVIDOR_PONTOS );
    campoPontos.adicionaInteiro1Byte ( scoreCliente );
    this.adicionaCampoPdu(campoPontos);
    this.preparaEnvio();
  }


  public boolean contemCampo(byte tipoCampo ) {
    boolean resultado = false;
    for (CampoPdu t : ArrayListCamposSeguintes ) {
      if ( t.mesmoTipo( tipoCampo )){
        resultado = true;
      }
    }
    return resultado;
  }

  public CampoPdu popCampo() {
    CampoPdu popCampo = this.ArrayListCamposSeguintes.get(posPopCamposSeguintes);
    posPopCamposSeguintes++;
    return popCampo;
  }

  public void replyDesafioDataHora(String nomeDesafio, Date dataHoraInicioDesafio) {
    CampoPdu campoNomeDesafio = new CampoPdu ( SERVIDOR_NOME_DESAFIO );
    campoNomeDesafio.adicionaString( nomeDesafio );
    this.adicionaCampoPdu(campoNomeDesafio);
    CampoPdu campoDataDesafio = new CampoPdu ( SERVIDOR_DATA_DESAFIO );
    campoDataDesafio.adicionaData( dataHoraInicioDesafio );
    this.adicionaCampoPdu(campoDataDesafio);
    CampoPdu campoHoraDesafio = new CampoPdu ( SERVIDOR_HORA_DESAFIO );
    campoHoraDesafio.adicionaHora( dataHoraInicioDesafio );
    this.adicionaCampoPdu(campoHoraDesafio);
  }

  public void replyPergunta(String nomeDesafio, int numeroQuestao , Pergunta perguntaEnviar) {
    CampoPdu campoNomeDesafio = new CampoPdu ( SERVIDOR_NOME_DESAFIO );
    campoNomeDesafio.adicionaString( nomeDesafio );
    this.adicionaCampoPdu(campoNomeDesafio);
    CampoPdu campoNumeroQuestao = new CampoPdu ( SERVIDOR_NUM_QUESTAO );
    campoNumeroQuestao.adicionaInteiro1Byte( numeroQuestao );
    this.adicionaCampoPdu(campoNumeroQuestao);
    CampoPdu campoTextoQuestao = new CampoPdu ( SERVIDOR_TXT_QUESTAO );
    campoTextoQuestao.adicionaString( perguntaEnviar.get_pergunta() );
    this.adicionaCampoPdu(campoTextoQuestao);
    CampoPdu campoOpcao1 = new CampoPdu ( SERVIDOR_NUM_RESPOSTA );
    campoOpcao1.adicionaInteiro1Byte(1);
    this.adicionaCampoPdu(campoOpcao1);
    CampoPdu campoTextoOpcao1 = new CampoPdu ( SERVIDOR_TXT_RESPOSTA );
    campoTextoOpcao1.adicionaString(perguntaEnviar.getTextoOpcao(0));
    this.adicionaCampoPdu(campoTextoOpcao1);
    CampoPdu campoOpcao2 = new CampoPdu ( SERVIDOR_NUM_RESPOSTA );
    campoOpcao2.adicionaInteiro1Byte(2);
    this.adicionaCampoPdu(campoOpcao2);
    CampoPdu campoTextoOpcao2 = new CampoPdu ( SERVIDOR_TXT_RESPOSTA );
    campoTextoOpcao2.adicionaString(perguntaEnviar.getTextoOpcao(1));
    this.adicionaCampoPdu(campoTextoOpcao2);
    CampoPdu campoOpcao3 = new CampoPdu ( SERVIDOR_NUM_RESPOSTA );
    campoOpcao3.adicionaInteiro1Byte(3);
    this.adicionaCampoPdu(campoOpcao3);
    CampoPdu campoTextoOpcao3 = new CampoPdu ( SERVIDOR_TXT_RESPOSTA );
    campoTextoOpcao3.adicionaString(perguntaEnviar.getTextoOpcao(2));
    this.adicionaCampoPdu(campoTextoOpcao3);
    CampoPdu campoCerta = new CampoPdu ( SERVIDOR_RESPOSTA_CERTA );
    campoCerta.adicionaInteiro1Byte(perguntaEnviar.getCerta());
    this.adicionaCampoPdu(campoCerta);
    CampoPdu campoImagem = new CampoPdu ( SERVIDOR_IMAGEM );
    ArrayList <CampoPdu > blocosExtraImagem = new  ArrayList <CampoPdu > ();
    blocosExtraImagem = campoImagem.adicionaFicheiro( perguntaEnviar.get_Imagem() );
    this.adicionaCampoPdu(campoImagem);
    for ( CampoPdu blocoImagem : blocosExtraImagem ){
      CampoPdu campoNumeroBlocoImagemExtra = new CampoPdu ( SERVIDOR_NUM_BLOCO );
      campoNumeroBlocoImagemExtra.adicionaInteiro1Byte( blocoImagem.getNumeroBloco());
      this.adicionaCampoPdu(campoNumeroBlocoImagemExtra);
      this.adicionaCampoPdu(blocoImagem);
    }
    CampoPdu campoNumeroBlocoMusica = new CampoPdu ( SERVIDOR_NUM_BLOCO );
    campoNumeroBlocoMusica.adicionaInteiro1Byte(1);
    this.adicionaCampoPdu(campoNumeroBlocoMusica);
    CampoPdu campoBlocoMusica = new CampoPdu ( SERVIDOR_AUDIO );
    ArrayList <CampoPdu > blocosExtraAudio = new  ArrayList <CampoPdu > ();
    blocosExtraAudio = campoBlocoMusica.adicionaFicheiro( perguntaEnviar.get_Musica());
    this.adicionaCampoPdu(campoBlocoMusica);
    for ( CampoPdu blocoAudio : blocosExtraAudio ){
      CampoPdu campoNumeroBlocoMusicaExtra = new CampoPdu ( SERVIDOR_NUM_BLOCO );
      campoNumeroBlocoMusicaExtra.adicionaInteiro1Byte( blocoAudio.getNumeroBloco());
      this.adicionaCampoPdu(campoNumeroBlocoMusicaExtra);
      this.adicionaCampoPdu(blocoAudio);
    }
  }

  public void replyRespostaQuestao(String nomeDesafio, int numeroQuestao, int certaErrada , int pontosAmealhados ) {
    CampoPdu campoNomeDesafio = new CampoPdu ( SERVIDOR_NOME_DESAFIO );
    campoNomeDesafio.adicionaString( nomeDesafio );
    this.adicionaCampoPdu(campoNomeDesafio);
    CampoPdu campoNumeroQuestao = new CampoPdu ( SERVIDOR_NUM_QUESTAO );
    campoNumeroQuestao.adicionaInteiro1Byte( numeroQuestao );
    this.adicionaCampoPdu(campoNumeroQuestao);
    CampoPdu campoCertaErrada = new CampoPdu ( SERVIDOR_RESPOSTA_CERTA );
    campoCertaErrada.adicionaInteiro1Byte( certaErrada );
    this.adicionaCampoPdu(campoCertaErrada);
    CampoPdu campoPontos = new CampoPdu ( SERVIDOR_PONTOS );
    campoPontos.adicionaInteiro1Byte( pontosAmealhados );
    this.adicionaCampoPdu(campoPontos);
  }

  public ArrayList<BasePdu> split(int tamanhoMaxPdu) {
    ArrayList<BasePdu> dividido = new ArrayList <BasePdu> ();
    int campoNumero = 0;
    CampoPdu continuaNoutroPdu = new CampoPdu ( SERVIDOR_CONTINUA );
    continuaNoutroPdu.adicionaByteAZero();
    while ( campoNumero < this.ArrayListCamposSeguintes.size() ){
      BasePdu novoPdu = new BasePdu ( this.getVersao() , this.getSeguranca() , this.getLabel() , this.getTipo() ) ;
      while ( ( novoPdu.tamanhoPdu + this.ArrayListCamposSeguintes.get(campoNumero).tamanhoTotal ) <= ( tamanhoMaxPdu - continuaNoutroPdu.tamanhoTotal )){
        CampoPdu campoActual = this.ArrayListCamposSeguintes.get(campoNumero);
        novoPdu.adicionaCampoPdu(campoActual);
        campoNumero++;
      }
      if ( campoNumero < this.ArrayListCamposSeguintes.size() ){
        novoPdu.adicionaCampoPdu(continuaNoutroPdu);
        novoPdu.esperaDadosNovoPacote = true;
      }
      dividido.add(novoPdu);
    }
    return dividido;
  }

}
