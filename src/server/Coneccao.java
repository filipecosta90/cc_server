/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

package server;

import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;

public class Coneccao implements Serializable {

	private static final long serialVersionUID = 774628494621909111L;

	private String alcunhaClienteAssociado;
  private ArrayList < BasePdu > stackEspera;
  private ArrayList < BasePdu > historialPdus;
  private Server localServerPointer;
  private Date timeStampInicio;
  private Date timeStampAlteracao;
  private Date timeStampFim;
  private boolean anonima;
  private transient DatagramSocket boundedSocket;
  private InetAddress enderecoLigacao;
  private int portaRemota;
  private BasePdu replyPdu;
  private int numeroPdu;

  public Coneccao( Server localServer , DatagramSocket inSocket , InetAddress remoteAddress , int remotePort ){
    alcunhaClienteAssociado= new String();
    stackEspera = new ArrayList < BasePdu > ();
    historialPdus = new ArrayList < BasePdu > ();
    this.localServerPointer = localServer;
    timeStampInicio = new Date();
    anonima = true;
    boundedSocket = inSocket;
    enderecoLigacao = remoteAddress;
    portaRemota = remotePort;
  }

  public void adicionaPacote ( DatagramPacket novoPacote ) throws Exception{
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

  public void mergePacotesEspera ( BasePdu pacoteAFundir ) throws Exception{
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

  private void enviaPacote( BasePdu replyPdu ) throws Exception {
    ArrayList < BasePdu > pdusEnviar = new ArrayList < BasePdu >();
    if ( replyPdu.getTamanhoTotalPdu() > ServerCodes.TAMANHO_MAX_PDU ){
      pdusEnviar = replyPdu.split( ServerCodes.TAMANHO_MAX_PDU );
    }
    else {
      pdusEnviar.add(replyPdu);
    }
    for ( BasePdu pduActual : pdusEnviar ){
      DatagramPacket pacoteEnvio = new DatagramPacket ( pduActual.getBytesEnvio() , pduActual.getBytesEnvio().length , this.enderecoLigacao , this.portaRemota );
      try{
        boundedSocket.send( pacoteEnvio );
      }
      catch ( SocketException e){
        boundedSocket.connect(this.enderecoLigacao, this.portaRemota);
        boundedSocket.send( pacoteEnvio );

      }
    }
  }

  @SuppressWarnings("deprecation")
private void resolvePacote(BasePdu pduAResolver) throws Exception {
    BasePdu replyPdu = new BasePdu ( ServerCodes.REPLY , pduAResolver.getLabel() ); 
    switch( (pduAResolver.getTipo())[0] ){
      case ServerCodes.HELLO :
        {
          replyPdu.replyOK();
          enviaPacote(replyPdu);
          break;
        }
      case ServerCodes.REGISTER :
        {
          boolean ok = false;
          String descricaoErro = new String();
          if ( pduAResolver.contemCampo( ServerCodes.CLIENTE_NOME ) &&  pduAResolver.contemCampo( ServerCodes.CLIENTE_ALCUNHA ) && pduAResolver.contemCampo( ServerCodes.CLIENTE_SEC_INFO )){
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
      case ServerCodes.LOGIN :
        {
          boolean ok = false;
          String descricaoErro = new String();
          if (  pduAResolver.contemCampo( ServerCodes.CLIENTE_ALCUNHA ) && pduAResolver.contemCampo( ServerCodes.CLIENTE_SEC_INFO )){
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
              replyPdu.replyNomeScore( clientPointer.getNomeCliente() , clientPointer.getScoreCliente() );
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
      case ServerCodes.LOGOUT :
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
      case ServerCodes.QUIT :
        {
          boolean ok = false;
          String descricaoErro = new String();
          Desafio desafioPointer = this.localServerPointer.getDesafioDecorrerAssociadoCliente( this.alcunhaClienteAssociado );
          if(desafioPointer != null){
            desafioPointer.quitPergunta(this.alcunhaClienteAssociado);
            replyPdu.replyOK();
            ok = true;
          }
          else{
            descricaoErro = "Nao existe um desafio activo para este cliente!";
          }
          if ( !ok ) {
            replyPdu.replyErro( descricaoErro );
          }
          enviaPacote(replyPdu);
          break;
        }
      case ServerCodes.END :
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
            descricaoErro = "Nao existe nenhum desafio terminado jogado por si!";
          }
          if ( !ok ) {
            replyPdu.replyErro( descricaoErro );
          }
          enviaPacote(replyPdu);
          break;
        }
      case ServerCodes.LIST_CHALLENGES :
        {
          ArrayList < Desafio > desafiosEmEspera;
          desafiosEmEspera = this.localServerPointer.listaDesafiosEspera();
          if ( desafiosEmEspera != null){
            for ( Desafio desafioPointer : desafiosEmEspera ){
              replyPdu.replyDesafioDataHora( desafioPointer.getNomeDesafio() , desafioPointer.getDataHoraInicioDesafio() );
            }
          }
          else{
              replyPdu.replyErro( "Não Existem desafios em espera!" );
          }
          enviaPacote(replyPdu);
          break;
        }
      case ServerCodes.MAKE_CHALLENGE :
        {
          boolean ok = false;
          String descricaoErro = new String();
          if (  pduAResolver.contemCampo( ServerCodes.SERVIDOR_NOME_DESAFIO ) ){
            CampoPdu campoNomeDesafio = pduAResolver.popCampo();
            String nomeDesafio = campoNomeDesafio.getCampoString();
            Date dataHoraDesafio = null;
            if ( pduAResolver.contemCampo( ServerCodes.SERVIDOR_DATA_DESAFIO ) && pduAResolver.contemCampo( ServerCodes.SERVIDOR_HORA_DESAFIO )){
              CampoPdu campoDataDesafio = pduAResolver.popCampo();
              CampoPdu campoHoraDesafio = pduAResolver.popCampo();
              int ano = campoDataDesafio.getCampoDataAno();
              int mes = campoDataDesafio.getCampoDataMes();
              int dia = campoDataDesafio.getCampoDataDia();
              int hora =  campoHoraDesafio.getCampoHoraHora();
            int minutos =  campoHoraDesafio.getCampoHoraMinutos();
             int segundos =   campoHoraDesafio.getCampoHoraSegundos();
               dataHoraDesafio = new Date ( ano , mes, dia , hora , minutos , segundos );
            }
            else {
              final long ONE_MINUTE_IN_MILLIS=60000;//millisecs
              dataHoraDesafio = new Date();
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
      case ServerCodes.ACCEPT_CHALLENGE :
        {
          boolean ok = false;
          String descricaoErro = new String();
          if (  pduAResolver.contemCampo( ServerCodes.SERVIDOR_NOME_DESAFIO ) ){
            CampoPdu campoNomeDesafio = pduAResolver.popCampo();
            String nomeDesafio = campoNomeDesafio.getCampoString();
            boolean podeAceitar = false;
            podeAceitar = this.localServerPointer.ExisteDesafioEsperaEPodeAceitar( nomeDesafio , this.alcunhaClienteAssociado );
            if ( podeAceitar ){
              boolean resultadoAceitacao = false;
              resultadoAceitacao = this.localServerPointer.AceitaDesafio( nomeDesafio , this.alcunhaClienteAssociado );
              if ( resultadoAceitacao == true){
            	  Desafio desafioPointer;
                  desafioPointer = this.localServerPointer.getDesafiosCriadosEmEspera( nomeDesafio );
                  replyPdu.replyDesafioDataHora( desafioPointer.getNomeDesafio() , desafioPointer.getDataHoraInicioDesafio() );
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
      case ServerCodes.DELETE_CHALLENGE :
        {
          boolean ok = false;
          String descricaoErro = new String();
          if (  pduAResolver.contemCampo( ServerCodes.SERVIDOR_NOME_DESAFIO ) ){
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
      case ServerCodes.ANSWER :
        {
          boolean ok = false;
          String descricaoErro = new String();
          if (  pduAResolver.contemCampo( ServerCodes.CLIENTE_NOME_DESAFIO ) && pduAResolver.contemCampo( ServerCodes.CLIENTE_NUM_QUESTAO )  && pduAResolver.contemCampo( ServerCodes.CLIENTE_ESCOLHA )){
            CampoPdu campoEscolha = pduAResolver.popCampo();
            CampoPdu campoNomeDesafio = pduAResolver.popCampo();
            CampoPdu campoQuestao = pduAResolver.popCampo();
            int escolha = campoEscolha.getCampoInt1Byte();
            String nomeDesafio = campoNomeDesafio.getCampoString();
            int numeroQuestao = campoQuestao.getCampoInt1Byte();
            Desafio desafioPointer = this.localServerPointer.getDesafioDecorrerAssociadoCliente( this.alcunhaClienteAssociado );
            if(desafioPointer != null){
              boolean resultadoResposta = false;
              resultadoResposta = desafioPointer.respondePergunta( this.alcunhaClienteAssociado , numeroQuestao , escolha );
              if ( resultadoResposta == true ){
                replyPdu.replyRespostaQuestao ( nomeDesafio , numeroQuestao , 1 , 2 );
              }
              else {
                replyPdu.replyRespostaQuestao ( nomeDesafio , numeroQuestao , 0 , -1 );
              }
              ok = true;
            }
            else{
              descricaoErro = "Nao existe um desafio activo para este cliente!";
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
      case ServerCodes.RETRANSMIT :
        {
          break;
        }
      case ServerCodes.LIST_RANKING :
        {
          HashMap < String , Cliente > mapClientes;
          mapClientes = this.localServerPointer.getMapClientes();
          if ( mapClientes != null){
            for ( Cliente clientPointer : mapClientes.values() ){
              replyPdu.replyNomeAlcunhaScore( clientPointer.getNomeCliente()  , clientPointer.getAlcunhaCliente() , clientPointer.getScoreCliente() );
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
    this.numeroPdu++;
  }

  private void UnboundAlcunhaCliente() {
    this.anonima = true;
    this.alcunhaClienteAssociado="";
  }

  public boolean mesmoEnderecoPortaSocket(Coneccao coneccaoNoDesafio) {
    boolean resultado = false;
    if ( this.enderecoLigacao.equals( coneccaoNoDesafio.getEnderecoRemoto()) && this.enderecoLigacao.equals( coneccaoNoDesafio.getPortaRemota() ) && this.boundedSocket.equals( coneccaoNoDesafio.getBoundedSocket())){
      resultado = true;
    }
    return resultado;
  }

  private DatagramSocket getBoundedSocket() {
    return this.boundedSocket;
  }

  public void copiaEnderecoPortaSocket(Coneccao coneccaoRetornada) {
    this.setEnderecoLigacao ( coneccaoRetornada.getEnderecoRemoto());
    this.setPortaRemota ( coneccaoRetornada.getPortaRemota());
    this.setSocket ( coneccaoRetornada.getBoundedSocket());

  }

  private void setSocket(DatagramSocket remoteSocket) {
    this.boundedSocket = remoteSocket;
  }

  private void setPortaRemota(int copiaPortaRemota ) {
    this.portaRemota = copiaPortaRemota;
  }

  private void setEnderecoLigacao(InetAddress enderecoRemoto) {
    this.enderecoLigacao = enderecoRemoto;
  }

  public void enviaPergunta( String nomeDesafio , int numeroQuestao , Pergunta perguntaActual) throws Exception {

    byte[] labelNumero = new byte[2];
    labelNumero[0] = (byte) (this.numeroPdu & 0xFF);
    labelNumero[1] = (byte) ((this.numeroPdu >> 8) & 0xFF);
    BasePdu replyPdu = new BasePdu ( ServerCodes.REPLY , labelNumero  ); 
    replyPdu.replyPergunta( nomeDesafio, numeroQuestao , perguntaActual );
    enviaPacote(replyPdu);
    System.out.println("Enviando Pdu: " + replyPdu.toString());
    this.numeroPdu++;
  }

  public void terminaConeccao() {
    this.timeStampFim = new Date();
    this.boundedSocket.close();
  }

  public String getKey() {
    String chave = new String ();
    chave += this.getEnderecoRemoto();
    chave += ":";
    chave += this.getPortaRemota();
    return chave;
  }

  public void verificaAlteraSeNecessarioSocket(DatagramSocket receivedSocket,
      int remotePort) {
    if (this.boundedSocket != receivedSocket){
      this.boundedSocket = receivedSocket;
    }
    if (this.portaRemota != remotePort ){
      this.portaRemota = remotePort;
    }			
  }

}
