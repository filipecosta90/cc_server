package cliente;

import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

import server.BasePdu;
import server.CampoPdu;
import server.Coneccao;
import server.Desafio;
import server.ServerCodes;


public class DesafioReceiver implements Runnable , Serializable {

  private static final long serialVersionUID = -4941720503862447905L;

  private Desafio desafioAGerir;

  private HashMap < String , Coneccao > mapConeccoes;
  private  Date dataHoraInicio;
  private int portaRemota;
  private InetAddress enderecoRemoto;
  public enum EstadoDesafio { EM_ESPERA , EM_JOGO , TERMINADO , ELIMINADO , PASSOU_PRAZO };
  public EstadoDesafio estado;
  private DatagramSocket serverSocket;
  private ArrayList < BasePdu > stackEspera;
  private int labelNumber;

  DesafioReceiver ( Date Inicio , InetAddress enderecoRemoto ,  int porta ){
    this.dataHoraInicio = Inicio;
    this.portaRemota = porta;
    this.estado = EstadoDesafio.EM_ESPERA;
    this.stackEspera = new ArrayList < BasePdu > ();
    this.labelNumber = 0;
  }

  public void adicionaPacote ( DatagramPacket novoPacote ) throws Exception{
	  System.out.println("Recebeu pacote do servidor!");
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

  private void enviaPacote( BasePdu pduActual ) throws Exception  {
    pduActual.preparaEnvio();
    System.out.println("Sending pdu:\n"+pduActual.toString());
    DatagramPacket pacoteEnvio = new DatagramPacket ( pduActual.getBytesEnvio() , pduActual.getBytesEnvio().length , this.enderecoRemoto , this.portaRemota );
    serverSocket.send( pacoteEnvio );
  }

  public void updateEstadoEsperaInicia(){
    Date agora = new Date();
    if ( agora.after(this.dataHoraInicio) ){
      if ( this.estado == EstadoDesafio.EM_ESPERA ){
        this.estado = EstadoDesafio.EM_JOGO;
      }
    }
  }

  @Override
    public void run() {
	  System.out.println("Iniciado o gestor de perguntas do desafio no cliente:");
      while (this.estado == EstadoDesafio.EM_ESPERA){
        updateEstadoEsperaInicia();
      }
      while( this.estado == EstadoDesafio.EM_JOGO ){
        byte[] udpReceber;
        DatagramSocket udpSocket;
        DatagramPacket udpDataPacket;

        try {
          udpSocket = new DatagramSocket( portaRemota );
          System.out.println( "Iniciado o listner do jogo na porta: " + portaRemota +"\n");
          while ( true ) {
            try {
              udpReceber = new byte[ ServerCodes.TAMANHO_MAX_PDU ];
              udpDataPacket = new DatagramPacket( udpReceber , udpReceber.length );
              udpSocket.receive( udpDataPacket );
              this.adicionaPacote(udpDataPacket);
            } catch ( Exception e ) {
            }
          }
        } catch ( SocketException e ) {
        }
      }
    }

  @SuppressWarnings("deprecation")
    private void resolvePacote(BasePdu pduAResolver) throws Exception {
      BasePdu replyPdu = new BasePdu ( ServerCodes.ANSWER , pduAResolver.getLabel() ); 
      if ( pduAResolver.contemCampo( ServerCodes.SERVIDOR_NOME_DESAFIO ) 
          &&  pduAResolver.contemCampo( ServerCodes.SERVIDOR_NUM_QUESTAO ) 
          && pduAResolver.contemCampo( ServerCodes.SERVIDOR_TXT_QUESTAO )
          && pduAResolver.contemCampo( ServerCodes.SERVIDOR_TXT_QUESTAO )
          && pduAResolver.contemCampo( ServerCodes.SERVIDOR_NUM_RESPOSTA )
          && pduAResolver.contemCampo( ServerCodes.SERVIDOR_TXT_RESPOSTA)
          && pduAResolver.contemCampo( ServerCodes.SERVIDOR_IMAGEM)
          && pduAResolver.contemCampo( ServerCodes.SERVIDOR_AUDIO)){
    	  System.out.println ("Recebeu nova questão do servidor:");
        CampoPdu campoNomeDesafio = pduAResolver.popCampo();
        CampoPdu campoNumeroQuestao = pduAResolver.popCampo();
        CampoPdu campoTextoQuestao = pduAResolver.popCampo();
        CampoPdu campoNumResposta1 = pduAResolver.popCampo();
        CampoPdu campoTextoResposta1 = pduAResolver.popCampo();
        CampoPdu campoNumResposta2 = pduAResolver.popCampo();
        CampoPdu campoTextoResposta2 = pduAResolver.popCampo();
        CampoPdu campoNumResposta3 = pduAResolver.popCampo();
        CampoPdu campoTextoResposta3 = pduAResolver.popCampo();
        CampoPdu campoImagem = pduAResolver.popCampo();
        CampoPdu campoAudio = pduAResolver.popCampo();
        
        String nomeDesafio = campoNomeDesafio.getCampoString();
        int numeroPergunta = campoNumeroQuestao.getCampoInt1Byte();
        String textoQuestao = campoTextoQuestao.getCampoString();
        int numeroResposta1 = campoNumResposta1.getCampoInt1Byte();
        String textoResposta1 = campoTextoResposta1.getCampoString();
        int numeroResposta2 = campoNumResposta2.getCampoInt1Byte();
        String textoResposta2 = campoTextoResposta2.getCampoString();
        int numeroResposta3 = campoNumResposta3.getCampoInt1Byte();
        String textoResposta3 = campoTextoResposta3.getCampoString();
        Date inicioTimer = new Date();
        final long ONE_MINUTE_IN_MILLIS=60000;//millisecs
        Date fimTimer = new Date();
        long t=fimTimer.getTime();
        fimTimer=new Date(t + (1 * ONE_MINUTE_IN_MILLIS));
        Date now = new Date();
        Scanner sc = new Scanner (System.in);
        int opcao = -1;
        new Thread(new JPGHandler ( campoImagem.getBytes() )).start();
        while(( now.after( fimTimer ) )){
        	System.out.println("Desafio: " + nomeDesafio);
            System.out.println("Questao #: " + numeroPergunta);
            System.out.println(textoQuestao);
            System.out.println(numeroResposta1 + " - " + textoResposta1);
            System.out.println(numeroResposta2 + " - " + textoResposta2);
            System.out.println(numeroResposta3 + " - " + textoResposta3);
            System.out.println("4 - passar pergunta");
            opcao = Input.lerInt(sc);
        	}
        if(opcao >0 && opcao != 4){
        	replyPdu.adicionaCampoPdu(campoNomeDesafio);
        	replyPdu.adicionaCampoPdu(campoNumeroQuestao);
        	CampoPdu campoEscolha = new CampoPdu ( ServerCodes.CLIENTE_ESCOLHA);
        	campoEscolha.adicionaInteiro1Byte(opcao);
        	replyPdu.adicionaCampoPdu(campoEscolha);
        }
        if ( opcao == 4){
        	replyPdu.setTipo(ServerCodes.QUIT);
        }
        this.enviaPacote(replyPdu);

        if ( this.stackEspera.contains( pduAResolver) ){
          this.stackEspera.remove( pduAResolver);
        }
        this.labelNumber++;
          }
    }

}
