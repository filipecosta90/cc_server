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
import java.io.Serializable;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Date;

public class BasePdu implements Serializable{

	private static final long serialVersionUID = -1380214578415728232L;

	private byte versao[];
  private byte seguranca[];
  private byte label[];
  private byte tipo[];
  private byte numeroCamposSeguintes[];
  private byte tamanhoBytesCamposSeguintes[];
  private ArrayList<CampoPdu> ArrayListCamposSeguintes;
  private byte camposSeguintes[];

  private transient DatagramPacket pacoteUdp;
  private int  tamanhoPdu;
  private int tamanhoCamposSeguintes;
  private byte[] rawData;
  private boolean esperaDadosNovoPacote;
  private transient ByteArrayInputStream inputByteArray;
  private int numeroCamposSeguintesInt;
  private int posPopCamposSeguintes;

  public BasePdu ( DatagramPacket pacote ) { 
    this.versao = new byte[1];
    this.seguranca = new byte[1];
    this.label = new byte[2];
    this.tipo = new byte[1];
    this.numeroCamposSeguintes =  new byte[1];
    this.tamanhoBytesCamposSeguintes = new byte[2];
    this.tamanhoCamposSeguintes = 0;
    this.tamanhoPdu = pacote.getLength();
    this.rawData = pacote.getData();
    this.esperaDadosNovoPacote = false;
    this.numeroCamposSeguintesInt = 0;
    this.posPopCamposSeguintes = 0;
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

  public BasePdu ( byte tipo , int labelNumber ) { 
    this.versao = new byte[1];
    this.seguranca = new byte[1];
    this.label = new byte[2];
    this.label = CampoPdu.intPara2Bytes( labelNumber);
    this.tipo = new byte[1];
    this.tipo[0] = tipo;
    this.numeroCamposSeguintes =  new byte[1];
    this.tamanhoBytesCamposSeguintes = new byte[2];
    this.tamanhoCamposSeguintes = 0;
    this.tamanhoPdu = 8;
    this.esperaDadosNovoPacote = false;
    this.numeroCamposSeguintesInt = 0;
    this.posPopCamposSeguintes = 0;
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
    this.numeroCamposSeguintes =  new byte[1];
    this.tamanhoBytesCamposSeguintes = new byte[2];
    this.tamanhoCamposSeguintes = 0;
    this.tamanhoPdu = 8;
    this.esperaDadosNovoPacote = false;
    this.numeroCamposSeguintesInt = 0;
    this.posPopCamposSeguintes = 0;
    this.ArrayListCamposSeguintes = new ArrayList <CampoPdu>();
  }

  public byte[] getVersao() {
    return this.versao;
  }

  public byte[] getSeguranca() {
    return this.seguranca;
  }

  public byte[] getLabel() {
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
    inputByteArray = new ByteArrayInputStream( rawData , 0 , this.tamanhoPdu );
    if( tamanhoPdu >= 8 ){
      inputByteArray.read( versao , 0 , 1 );
      inputByteArray.read( seguranca , 0 , 1 );
      inputByteArray.read( label, 0, 2 );
      inputByteArray.read( tipo , 0 , 1 );
      inputByteArray.read( numeroCamposSeguintes , 0 , 1 );
      numeroCamposSeguintesInt = CampoPdu.umByteParaInt( numeroCamposSeguintes );
      inputByteArray.read( tamanhoBytesCamposSeguintes , 0 , 2 );
      tamanhoCamposSeguintes = CampoPdu.doisBytesParaInt( tamanhoBytesCamposSeguintes );
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
    s.append( "************ PDU Base ************");
    s.append( "\nversao: "+ versao[0] );
    s.append( "\tsegurancao: "+ seguranca[0] );
    s.append( "\nlabel: "+ label[1]+label[0] );
    s.append( "\ttipo: "+ tipo[0] );
    s.append( "\nNum Campos: "+ numeroCamposSeguintes[0] + "int("+ this.numeroCamposSeguintesInt +")" );
    s.append( "\tTam: "+ this.tamanhoBytesCamposSeguintes[1]+tamanhoBytesCamposSeguintes[0] + "int("+ tamanhoCamposSeguintes +")" );
    s.append( "\ntamanho total: " + tamanhoPdu );
    if ( tamanhoCamposSeguintes > 0){
      s.append( "\n\t----- INICIO CAMPOS -----");
      for (CampoPdu t : ArrayListCamposSeguintes ) {
        s.append("\n\t");
        s.append(t.toString());
        s.append("\n");
      }
      s.append( "\t-----  FIM  CAMPOS  -----");
    }
    s.append( "\n********** FIM PDU Base **********");
    return s.toString();
  }

  /* Métodos auxiliares */
  public void preparaEnvio() throws IOException {
    ByteArrayOutputStream novoOut = new ByteArrayOutputStream();
    for ( CampoPdu t : ArrayListCamposSeguintes ) {
      novoOut.write(t.getBytes() , 0 , t.getTamanhoTotal());
      novoOut.flush();
    }
    numeroCamposSeguintesInt = ArrayListCamposSeguintes.size();
    this.numeroCamposSeguintes = CampoPdu.intPara1Byte ( numeroCamposSeguintesInt );
    tamanhoCamposSeguintes = novoOut.size();
    this.tamanhoBytesCamposSeguintes = CampoPdu.intPara2Bytes ( tamanhoCamposSeguintes );
    camposSeguintes = novoOut.toByteArray();
    novoOut.flush();
    novoOut.close();
    tamanhoPdu = 8 + tamanhoCamposSeguintes;
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
    outBytes.write( this.versao );
    outBytes.write( this.seguranca );
    outBytes.write( this.label , 0 ,2 );
    outBytes.write( this.tipo );
    outBytes.write( this.numeroCamposSeguintes );
    outBytes.write( this.tamanhoBytesCamposSeguintes , 0 , 2);
    outBytes.write( this.camposSeguintes , 0 , tamanhoCamposSeguintes);
    outBytes.flush();
    rawData = outBytes.toByteArray();
    System.out.println("tamanho bytes gerado: " + rawData.length);
    outBytes.flush();
    outBytes.close();
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
      campo.parseDados ( camposSeguintes , posCamposSeguintes , campo.getTamanhoDados() );
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
    CampoPdu campoOk = new CampoPdu ( ServerCodes.SERVIDOR_OK );
    campoOk.adicionaByteAZero();
    this.adicionaCampoPdu(campoOk);
    this.preparaEnvio();
  }

  public void adicionaCampoPdu(CampoPdu campoInserir) {
    this.tamanhoCamposSeguintes += campoInserir.getTamanhoTotal();
    this.ArrayListCamposSeguintes.add(campoInserir);
  }

  public void replyErro( String descricaoErro ) throws IOException {
    CampoPdu campoErro = new CampoPdu ( ServerCodes.SERVIDOR_ERRO );
    campoErro.adicionaString( descricaoErro );
    this.adicionaCampoPdu(campoErro);
    this.preparaEnvio();
  }

  public void replyAlcunhaScore( String alcunhaCliente , int scoreCliente ) throws IOException {
    CampoPdu campoAlcunha = new CampoPdu ( ServerCodes.SERVIDOR_ALCUNHA );
    campoAlcunha.adicionaString( alcunhaCliente );
    this.adicionaCampoPdu(campoAlcunha);
    CampoPdu campoPontos = new CampoPdu ( ServerCodes.SERVIDOR_PONTOS );
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

  public void replyDesafioDataHora(String nomeDesafio, Date dataHoraInicioDesafio) throws Exception {
    CampoPdu campoNomeDesafio = new CampoPdu ( ServerCodes.SERVIDOR_NOME_DESAFIO );
    campoNomeDesafio.adicionaString( nomeDesafio );
    this.adicionaCampoPdu(campoNomeDesafio);
    CampoPdu campoDataDesafio = new CampoPdu ( ServerCodes.SERVIDOR_DATA_DESAFIO );
    campoDataDesafio.adicionaData( dataHoraInicioDesafio );
    this.adicionaCampoPdu(campoDataDesafio);
    CampoPdu campoHoraDesafio = new CampoPdu ( ServerCodes.SERVIDOR_HORA_DESAFIO );
    campoHoraDesafio.adicionaHora( dataHoraInicioDesafio );
    this.adicionaCampoPdu(campoHoraDesafio);
    this.preparaEnvio();
  }

  public void replyPergunta(String nomeDesafio, int numeroQuestao , Pergunta perguntaEnviar) throws Exception {
    CampoPdu campoNomeDesafio = new CampoPdu ( ServerCodes.SERVIDOR_NOME_DESAFIO );
    campoNomeDesafio.adicionaString( nomeDesafio );
    this.adicionaCampoPdu(campoNomeDesafio);
    CampoPdu campoNumeroQuestao = new CampoPdu ( ServerCodes.SERVIDOR_NUM_QUESTAO );
    campoNumeroQuestao.adicionaInteiro1Byte( numeroQuestao );
    this.adicionaCampoPdu(campoNumeroQuestao);
    CampoPdu campoTextoQuestao = new CampoPdu ( ServerCodes.SERVIDOR_TXT_QUESTAO );
    campoTextoQuestao.adicionaString( perguntaEnviar.get_pergunta() );
    this.adicionaCampoPdu(campoTextoQuestao);
    CampoPdu campoOpcao1 = new CampoPdu ( ServerCodes.SERVIDOR_NUM_RESPOSTA );
    campoOpcao1.adicionaInteiro1Byte(1);
    this.adicionaCampoPdu(campoOpcao1);
    CampoPdu campoTextoOpcao1 = new CampoPdu ( ServerCodes.SERVIDOR_TXT_RESPOSTA );
    campoTextoOpcao1.adicionaString(perguntaEnviar.getTextoOpcao(0));
    this.adicionaCampoPdu(campoTextoOpcao1);
    CampoPdu campoOpcao2 = new CampoPdu ( ServerCodes.SERVIDOR_NUM_RESPOSTA );
    campoOpcao2.adicionaInteiro1Byte(2);
    this.adicionaCampoPdu(campoOpcao2);
    CampoPdu campoTextoOpcao2 = new CampoPdu ( ServerCodes.SERVIDOR_TXT_RESPOSTA );
    campoTextoOpcao2.adicionaString(perguntaEnviar.getTextoOpcao(1));
    this.adicionaCampoPdu(campoTextoOpcao2);
    CampoPdu campoOpcao3 = new CampoPdu ( ServerCodes.SERVIDOR_NUM_RESPOSTA );
    campoOpcao3.adicionaInteiro1Byte(3);
    this.adicionaCampoPdu(campoOpcao3);
    CampoPdu campoTextoOpcao3 = new CampoPdu ( ServerCodes.SERVIDOR_TXT_RESPOSTA );
    campoTextoOpcao3.adicionaString(perguntaEnviar.getTextoOpcao(2));
    this.adicionaCampoPdu(campoTextoOpcao3);
    CampoPdu campoCerta = new CampoPdu ( ServerCodes.SERVIDOR_RESPOSTA_CERTA );
    campoCerta.adicionaInteiro1Byte(perguntaEnviar.getCerta());
    this.adicionaCampoPdu(campoCerta);
    CampoPdu campoImagem = new CampoPdu ( ServerCodes.SERVIDOR_IMAGEM );
    ArrayList <CampoPdu > blocosExtraImagem = new  ArrayList <CampoPdu > ();
    blocosExtraImagem = campoImagem.adicionaFicheiro( perguntaEnviar.get_Imagem() );
    this.adicionaCampoPdu(campoImagem);
    for ( CampoPdu blocoImagem : blocosExtraImagem ){
      CampoPdu campoNumeroBlocoImagemExtra = new CampoPdu ( ServerCodes.SERVIDOR_NUM_BLOCO );
      campoNumeroBlocoImagemExtra.adicionaInteiro1Byte( blocoImagem.getNumeroBloco());
      this.adicionaCampoPdu(campoNumeroBlocoImagemExtra);
      this.adicionaCampoPdu(blocoImagem);
    }
    CampoPdu campoNumeroBlocoMusica = new CampoPdu ( ServerCodes.SERVIDOR_NUM_BLOCO );
    campoNumeroBlocoMusica.adicionaInteiro1Byte(1);
    this.adicionaCampoPdu(campoNumeroBlocoMusica);
    CampoPdu campoBlocoMusica = new CampoPdu ( ServerCodes.SERVIDOR_AUDIO );
    ArrayList <CampoPdu > blocosExtraAudio = new  ArrayList <CampoPdu > ();
    blocosExtraAudio = campoBlocoMusica.adicionaFicheiro( perguntaEnviar.get_Musica());
    this.adicionaCampoPdu(campoBlocoMusica);
    for ( CampoPdu blocoAudio : blocosExtraAudio ){
      CampoPdu campoNumeroBlocoMusicaExtra = new CampoPdu ( ServerCodes.SERVIDOR_NUM_BLOCO );
      campoNumeroBlocoMusicaExtra.adicionaInteiro1Byte( blocoAudio.getNumeroBloco());
      this.adicionaCampoPdu(campoNumeroBlocoMusicaExtra);
      this.adicionaCampoPdu(blocoAudio);
    }
    this.preparaEnvio();
  }

  public void replyRespostaQuestao(String nomeDesafio, int numeroQuestao, int certaErrada , int pontosAmealhados ) throws Exception {
    CampoPdu campoNomeDesafio = new CampoPdu ( ServerCodes.SERVIDOR_NOME_DESAFIO );
    campoNomeDesafio.adicionaString( nomeDesafio );
    this.adicionaCampoPdu(campoNomeDesafio);
    CampoPdu campoNumeroQuestao = new CampoPdu ( ServerCodes.SERVIDOR_NUM_QUESTAO );
    campoNumeroQuestao.adicionaInteiro1Byte( numeroQuestao );
    this.adicionaCampoPdu(campoNumeroQuestao);
    CampoPdu campoCertaErrada = new CampoPdu ( ServerCodes.SERVIDOR_RESPOSTA_CERTA );
    campoCertaErrada.adicionaInteiro1Byte( certaErrada );
    this.adicionaCampoPdu(campoCertaErrada);
    CampoPdu campoPontos = new CampoPdu ( ServerCodes.SERVIDOR_PONTOS );
    campoPontos.adicionaInteiro1Byte( pontosAmealhados );
    this.adicionaCampoPdu(campoPontos);
    this.preparaEnvio();
  }

  public ArrayList<BasePdu> split(int tamanhoMaxPdu) throws Exception {
    ArrayList<BasePdu> dividido = new ArrayList <BasePdu> ();
    int campoNumero = 0;
    CampoPdu continuaNoutroPdu = new CampoPdu ( ServerCodes.SERVIDOR_CONTINUA );
    continuaNoutroPdu.adicionaByteAZero();
    while ( campoNumero < this.ArrayListCamposSeguintes.size() ){
      BasePdu novoPdu = new BasePdu ( this.getVersao() , this.getSeguranca() , this.getLabel() , this.getTipo() ) ;
      while ( ( novoPdu.tamanhoPdu + this.ArrayListCamposSeguintes.get(campoNumero).getTamanhoTotal() ) <= ( tamanhoMaxPdu - continuaNoutroPdu.getTamanhoTotal() )){
        CampoPdu campoActual = this.ArrayListCamposSeguintes.get(campoNumero);
        novoPdu.adicionaCampoPdu(campoActual);
        campoNumero++;
      }
      if ( campoNumero < this.ArrayListCamposSeguintes.size() ){
        novoPdu.adicionaCampoPdu(continuaNoutroPdu);
        novoPdu.esperaDadosNovoPacote = true;
      }
      novoPdu.preparaEnvio();
      dividido.add(novoPdu);
    }
    return dividido;
  }

  @Override 
  public boolean equals ( Object other ){
    boolean resultado = false;
    if (other instanceof BasePdu) {
      BasePdu that = (BasePdu) other;
      resultado = this.label.equals(that.label) && this.tipo.equals(that.tipo) && this.ArrayListCamposSeguintes.equals(that.ArrayListCamposSeguintes) ;
    }
    return resultado;
  }

}
