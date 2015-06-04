/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;

public class Coneccao {

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

  /* Campos dos tipos de pedidos que os clientes podem enviar aos servidores */
  public static final byte CLIENTE_NOME = (byte)1;
  public static final byte CLIENTE_ALCUNHA = (byte)2;
  public static final byte CLIENTE_SEC_INFO = (byte)3;
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

  String alcunhaClienteAssociado;
  private ArrayList < BasePdu > stackEspera;
  private ArrayList < BasePdu > historialPdus;
  private Server localServerPointer;
  Date timeStampInicio;
  Date timeStampAlteracao;
  Date timeStampFim;
  private boolean anonima;
  DatagramSocket boundedSocket;
  InetAddress enderecoLigacao;
  int portaRemota;
  BasePdu replyPdu;

  public Coneccao( Server localServer , DatagramSocket inSocket , InetAddress remoteAddress , int remotePort ){
    stackEspera = new ArrayList < BasePdu > ();
    historialPdus = new ArrayList < BasePdu > ();
    this.localServerPointer = localServer;
    timeStampInicio = new Date();
    anonima = true;
    boundedSocket = inSocket;
    enderecoLigacao = remoteAddress;
    portaRemota = remotePort;
  }

  public void adicionaPacote ( DatagramPacket novoPacote ) throws IOException{
    BasePdu novoPdu = new BasePdu ( novoPacote );
    novoPdu.parseCabecalho();
    novoPdu.parseCampos();
    if(novoPdu.dadosParciais()){
      boolean query = parteDePduEmEspera( novoPdu );
      if ( query == true ){
        mergePacotesEspera( novoPdu );
      }
      else{
        this.stackEspera.add( novoPdu );
      }
    }
    else{
      this.resolvePacote( novoPdu );
    }
  }

  public void mergePacotesEspera ( BasePdu pacoteAFundir ) throws IOException{
    for ( BasePdu pduNaStack : this.stackEspera ){
      if ( pduNaStack.MesmaLabel(pacoteAFundir) ){
        pduNaStack.mergePDU(pacoteAFundir);
        if ( pduNaStack.pduCompleto()){
          this.resolvePacote( pduNaStack );
        }
      }
    }
  }

  public boolean parteDePduEmEspera( BasePdu pacoteATestar ){
    boolean resultado = false;
    for ( BasePdu pduNaStack : this.stackEspera ){
      if ( pduNaStack.MesmaLabel(pacoteATestar) ){
        resultado = true;
      }
    }
    return resultado;
  }

  public String getAlcunhaClienteAssociado(){
    return alcunhaClienteAssociado;
  }

  public InetAddress getEnderecoRemoto() {
    return this.enderecoLigacao;
  }

  public int getPortaRemota() {
    return this.portaRemota;
  }

  private void boundAlcunhaCliente(String alcunha) {
    this.alcunhaClienteAssociado = alcunha;	
    this.anonima = false;
  }

  private void enviaPacote( BasePdu replyPdu ) throws IOException {
    DatagramPacket pacoteEnvio = new DatagramPacket ( replyPdu.getBytesEnvio() , replyPdu.getTamanhoTotalPdu () , this.enderecoLigacao , this.portaRemota );
    boundedSocket.send( pacoteEnvio );
  }

  private void resolvePacote(BasePdu pduAResolver) throws IOException {
    BasePdu replyPdu = new BasePdu ( REPLY , pduAResolver.label ); 
    switch( pduAResolver.getTipo() ){
      case HELLO :
        {
          replyPdu.replyOK();
          enviaPacote(replyPdu);
          break;
        }
      case REGISTER :
        {
          boolean ok = false;
          String descricaoErro = new String();
          if ( pduAResolver.contemCampo( CLIENTE_NOME ) &&  pduAResolver.contemCampo( CLIENTE_ALCUNHA ) && pduAResolver.contemCampo( CLIENTE_SEC_INFO )){
            CampoPdu campoNome = pduAResolver.popCampo();
            CampoPdu campoAlcunha = pduAResolver.popCampo();
            CampoPdu campoSecInfo = pduAResolver.popCampo();
            String nome = campoNome.getCampoString();
            String alcunha = campoAlcunha.getCampoString();
            String sec_info = campoSecInfo.getCampoString();
            boolean resultadoRegistar = false;
            resultadoRegistar = this.localServerPointer.registarCliente(nome, alcunha, sec_info);
            if ( resultadoRegistar == true){
              this.boundAlcunhaCliente ( alcunha );
              ok = true;
            }
            else {
              descricaoErro = "Ja existe um cliente com essa alcunha!";
              ok = false;
            }
          }
          else {
            descricaoErro = "O PDU não contém os dados necessários!";
          }
          if ( ok ){
            replyPdu.replyOK();
          }
          else {
            replyPdu.replyErro( descricaoErro );
          }
          enviaPacote(replyPdu);
          break;
        }
      case LOGIN :
        {
          boolean ok = false;
          String descricaoErro = new String();
          if (  pduAResolver.contemCampo( CLIENTE_ALCUNHA ) && pduAResolver.contemCampo( CLIENTE_SEC_INFO )){
            CampoPdu campoAlcunha = pduAResolver.popCampo();
            CampoPdu campoSecInfo = pduAResolver.popCampo();
            String alcunha = campoAlcunha.getCampoString();
            String sec_info = campoSecInfo.getCampoString();
            boolean resultadoLogin = false;
            resultadoLogin = this.localServerPointer.loginCliente( alcunha, sec_info );
            if ( resultadoLogin == true){
              this.boundAlcunhaCliente ( alcunha );
              Cliente clientPointer;
              clientPointer = this.localServerPointer.getCliente( alcunha );
              replyPdu.replyAlcunhaScore( clientPointer.getAlcunhaCliente() , clientPointer.getScoreCliente() );
              ok = true;
            }
            else {
              descricaoErro = "Alcunha ou sec_info inválidos!";
              ok = false;
            }
          }
          else {
            descricaoErro = "O PDU não contém os dados necessários!";
          }
          if ( !ok ) {
            replyPdu.replyErro( descricaoErro );
          }
          enviaPacote(replyPdu);
          break;
        }
      case LOGOUT :
        {
          boolean ok = false;
          String descricaoErro = new String();
          if( this.anonima == false ){
            boolean resultadoLogout = false;
            resultadoLogout = this.localServerPointer.logoutCliente( this.alcunhaClienteAssociado );
            if ( resultadoLogout == true){
              this.UnboundAlcunhaCliente ();
              replyPdu.replyOK();
              ok = true;
            }
            else {
              descricaoErro = "O Cliente Associado a esta ligação não tem sessão activa!";
              ok = false;
            }
          }
          else{
            descricaoErro = "Nao existe uma sessão com login nesta ligação!";
          }
          if ( !ok ) {
            replyPdu.replyErro( descricaoErro );
          }
          enviaPacote(replyPdu);
          break;
        }
      case QUIT :
        {
          break;
        }
      case END :
        {
          boolean ok = false;
          String descricaoErro = new String();
          Desafio desafioPointer = this.localServerPointer.getDesafioTerminadoAssociadoCliente( this.alcunhaClienteAssociado );
          if(desafioPointer != null){
            TreeMap < String, Integer > pontuacoesJogadores;
            pontuacoesJogadores = this.localServerPointer.getPontuacoesDesafioTerminado( desafioPointer.getNomeDesafio() );
            for ( String alcunhaCliente : pontuacoesJogadores.keySet() ){
              replyPdu.replyAlcunhaScore( alcunhaCliente , pontuacoesJogadores.get( alcunhaCliente ) );
            }
            ok = true;
          }
          else{
            descricaoErro = "Nao existe uma sessão com login nesta ligação!";
          }
          if ( !ok ) {
            replyPdu.replyErro( descricaoErro );
          }
          enviaPacote(replyPdu);
          break;
        }
      case LIST_CHALLENGES :
        {
          ArrayList < Desafio > desafiosEmEspera;
          desafiosEmEspera = this.localServerPointer.listaDesafiosEspera();
          if ( desafiosEmEspera != null){
            for ( Desafio desafioPointer : desafiosEmEspera ){
              replyPdu.replyDesafioDataHora( desafioPointer.getNomeDesafio() , desafioPointer.getDataHoraInicioDesafio() );
            }
          }
          enviaPacote(replyPdu);
          break;
        }
      case MAKE_CHALLENGE :
        {
          boolean ok = false;
          String descricaoErro = new String();
          if (  pduAResolver.contemCampo( SERVIDOR_NOME_DESAFIO ) ){
            CampoPdu campoNomeDesafio = pduAResolver.popCampo();
            String nomeDesafio = campoNomeDesafio.getCampoString();
            Date dataHoraDesafio = new Date();
            if ( pduAResolver.contemCampo( SERVIDOR_DATA_DESAFIO ) && pduAResolver.contemCampo( SERVIDOR_HORA_DESAFIO )){
              CampoPdu campoDataDesafio = pduAResolver.popCampo();
              CampoPdu campoHoraDesafio = pduAResolver.popCampo();
              dataHoraDesafio.setYear(campoDataDesafio.getCampoDataAno());
              dataHoraDesafio.setMonth(campoDataDesafio.getCampoDataMes());
              dataHoraDesafio.setDate(campoDataDesafio.getCampoDataDia());
              dataHoraDesafio.setHours( campoHoraDesafio.getCampoHoraHora());
              dataHoraDesafio.setMinutes( campoHoraDesafio.getCampoHoraMinutos());
              dataHoraDesafio.setSeconds( campoHoraDesafio.getCampoHoraSegundos());
            }
            else {
              final long ONE_MINUTE_IN_MILLIS=60000;//millisecs
              long t=dataHoraDesafio.getTime();
              dataHoraDesafio=new Date(t + (5 * ONE_MINUTE_IN_MILLIS));
            }
            boolean resultadoCriacao = false;
            resultadoCriacao = this.localServerPointer.CriaDesafio( nomeDesafio , dataHoraDesafio , this.alcunhaClienteAssociado );
            if ( resultadoCriacao == true){
              Desafio desafioPointer;
              desafioPointer = this.localServerPointer.getDesafiosCriadosEmEspera( nomeDesafio );
              replyPdu.replyDesafioDataHora( desafioPointer.getNomeDesafio() , desafioPointer.getDataHoraInicioDesafio() );
              ok = true;
            }
            else {
              descricaoErro = "Erro na criacao do desafio.";
              ok = false;
            }
          }
          else {
            descricaoErro = "O PDU não contém os dados necessários!";
          }
          if ( !ok ) {
            replyPdu.replyErro( descricaoErro );
          }
          enviaPacote(replyPdu);
          break;
        }
      case ACCEPT_CHALLENGE :
        {
          boolean ok = false;
          String descricaoErro = new String();
          if (  pduAResolver.contemCampo( SERVIDOR_NOME_DESAFIO ) ){
            CampoPdu campoNomeDesafio = pduAResolver.popCampo();
            String nomeDesafio = campoNomeDesafio.getCampoString();
            boolean podeAceitar = false;
            podeAceitar = this.localServerPointer.ExisteDesafioEsperaEPodeAceitar( nomeDesafio , this.alcunhaClienteAssociado );
            if ( podeAceitar ){
              boolean resultadoAceitacao = false;
              resultadoAceitacao = this.localServerPointer.AceitaDesafio( nomeDesafio , this.alcunhaClienteAssociado );
              if ( resultadoAceitacao == true){
                replyPdu.replyOK();
                ok = true;
              }
            }
            else {
              descricaoErro = "O desafio não está em espera ou não pode associar-se a ele.";
              ok = false;
            }
          }
          else {
            descricaoErro = "O PDU não contém os dados necessários!";
          }
          if ( !ok ) {
            replyPdu.replyErro( descricaoErro );
          }
          enviaPacote(replyPdu);
          break;
        }
      case DELETE_CHALLENGE :
        {
          boolean ok = false;
          String descricaoErro = new String();
          if (  pduAResolver.contemCampo( SERVIDOR_NOME_DESAFIO ) ){
            CampoPdu campoNomeDesafio = pduAResolver.popCampo();
            String nomeDesafio = campoNomeDesafio.getCampoString();
            boolean desafioPertence = false;
            desafioPertence = this.localServerPointer.DesafioPertenceCliente( nomeDesafio , this.alcunhaClienteAssociado );
            if ( desafioPertence ){
              boolean resultadoEliminacao = false;
              resultadoEliminacao = this.localServerPointer.EliminaDesafio( nomeDesafio , this.alcunhaClienteAssociado );
              if ( resultadoEliminacao == true){
                Desafio desafioPointer;
                desafioPointer = this.localServerPointer.getDesafioEliminado( nomeDesafio );
                replyPdu.replyDesafioDataHora( desafioPointer.getNomeDesafio() , desafioPointer.getDataHoraInicioDesafio() );
                ok = true;
              }
            }
            else {
              descricaoErro = "O desafio não lhe pertence para o poder eliminar.";
              ok = false;
            }
          }
          else {
            descricaoErro = "O PDU não contém os dados necessários!";
          }
          if ( !ok ) {
            replyPdu.replyErro( descricaoErro );
          }
          enviaPacote(replyPdu);
          break;
        }
      case ANSWER :
        {
          break;
        }
      case RETRANSMIT :
        {
          break;
        }
      case LIST_RANKING :
        {
          HashMap < String , Cliente > mapClientes;
          mapClientes = this.localServerPointer.getMapClientes();
          if ( mapClientes != null){
            for ( Cliente clientPointer : mapClientes.values() ){
              replyPdu.replyAlcunhaScore( clientPointer.getAlcunhaCliente() , clientPointer.getScoreCliente() );
            }
          }
          enviaPacote(replyPdu);
          break;
        }
    }
    if ( this.stackEspera.contains( pduAResolver) ){
      this.stackEspera.remove( pduAResolver);
    }
    this.historialPdus.add( pduAResolver );
  }

  private void UnboundAlcunhaCliente() {
    this.anonima = true;
    this.alcunhaClienteAssociado = new String();
  }

}
