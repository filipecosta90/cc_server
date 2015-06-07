package cliente;

import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Scanner;

import server.BasePdu;
import server.CampoPdu;

import java.util.Date;

import server.ServerCodes;

public class ClientResponder implements Serializable{

  private transient DatagramSocket serverSocket;
  private transient DatagramPacket receivedPacket;
  private InetAddress remoteAddress;
 private int remotePort;
  private boolean logginValido;
  private boolean remoteDefinido;
  private int numeroLabel;
  private BasePdu replyPdu;
  private transient Scanner sc;

  ClientResponder ( ){
    remoteDefinido = false;
    logginValido = false;
    numeroLabel=1;
    sc = new Scanner(System.in);
    sc.useDelimiter("\\n");
  }       

  public void navega() throws Exception{
    if ( remoteDefinido == false ){
      System.out.println("Endereço remoto:");
      String endereco = new String ();
      endereco = Input.lerString(sc);
      this.remoteAddress = InetAddress.getByName( endereco.toString() );
      System.out.println("Porta Aplicacional:");
      this.remotePort = Input.lerInt(sc);
      System.out.println("Testando ligação em: " + this.remoteAddress + ":" + this.remotePort );
      preparaPacote ( ServerCodes.HELLO );
      resolvePacote ( ServerCodes.HELLO );
    }
    else {
      byte opcao =(byte) -1;
      while ( logginValido == false ){
        System.out.println("Nao tem uma sessão autenticada! Faça login ou registe-se:" );
        System.out.println("2 - registar " );
        System.out.println("3 - login" );
        System.out.println("opcao:");
        opcao = Input.lerByte(sc);
        if ( opcao == ServerCodes.REGISTER || opcao == ServerCodes.LOGIN ){
          preparaPacote ( opcao );
          resolvePacote ( opcao );
        }
      }
      while ( opcao != ServerCodes.LOGOUT ){
        System.out.println("Escolha uma das seguintes opções:" );
        System.out.println("4 - logout " );
        System.out.println("7 - listar desafios " );
        System.out.println("8 - criar desafio " );
        System.out.println("9 - aceitar desafio " );
        System.out.println("10 - eliminar desafio " );
        System.out.println("13 - listar ranking " );
        System.out.println("opcao:");
        opcao = Input.lerByte(sc);
        preparaPacote ( opcao );
        resolvePacote ( opcao );
      }
    }
  }

  private void enviaPacote( BasePdu pduActual ) throws Exception  {
    pduActual.preparaEnvio();
    System.out.println("Sending pdu:\n"+pduActual.toString());
    DatagramPacket pacoteEnvio = new DatagramPacket ( pduActual.getBytesEnvio() , pduActual.getBytesEnvio().length , this.remoteAddress , this.remotePort );
    serverSocket.send( pacoteEnvio );
  }

  private void preparaPacote( byte tipoPedido) throws Exception {
    BasePdu sendPdu = null; 
    switch( tipoPedido ){
      case ServerCodes.HELLO :
        {
          serverSocket = new DatagramSocket ();
          sendPdu = new BasePdu ( ServerCodes.HELLO , this.numeroLabel );
          enviaPacote(sendPdu);
          break;
        }
      case ServerCodes.REGISTER :
        {
          System.out.println("Nome de utilizador:");
          String nomeUtilizador = Input.lerString(sc);
          System.out.println("Alcunha:");
          String alcunha = Input.lerString(sc);
          System.out.println("Password:");
          String password = Input.lerString(sc);
          CampoPdu campoNome = new CampoPdu ( ServerCodes.CLIENTE_NOME );
          CampoPdu campoAlcunha = new CampoPdu ( ServerCodes.CLIENTE_ALCUNHA );
          CampoPdu campoSecInfo = new CampoPdu ( ServerCodes.CLIENTE_SEC_INFO ); 
          campoNome.adicionaString(nomeUtilizador);
          campoAlcunha.adicionaString(alcunha);
          campoSecInfo.adicionaString(password);
          sendPdu = new BasePdu ( ServerCodes.REGISTER , this.numeroLabel );
          sendPdu.adicionaCampoPdu(campoNome);
          sendPdu.adicionaCampoPdu(campoAlcunha);
          sendPdu.adicionaCampoPdu(campoSecInfo);
          enviaPacote(sendPdu);
          break;
        }
      case ServerCodes.LOGIN :
        {
          System.out.println("Alcunha:");
          String alcunha = Input.lerString(sc);
          System.out.println("Password:");
          String password = Input.lerString(sc);
          CampoPdu campoAlcunha = new CampoPdu ( ServerCodes.CLIENTE_ALCUNHA );
          CampoPdu campoSecInfo = new CampoPdu ( ServerCodes.CLIENTE_SEC_INFO ); 
          campoAlcunha.adicionaString(alcunha);
          campoSecInfo.adicionaString(password);
          sendPdu = new BasePdu ( ServerCodes.LOGIN , this.numeroLabel );
          sendPdu.adicionaCampoPdu(campoAlcunha);
          sendPdu.adicionaCampoPdu(campoSecInfo);
          enviaPacote(sendPdu);
          break;
        }
      case ServerCodes.LOGOUT :
        {
          sendPdu = new BasePdu ( ServerCodes.LOGOUT , this.numeroLabel );
          enviaPacote(sendPdu);
          break;
        }
      case ServerCodes.QUIT :
        {
          sendPdu = new BasePdu ( ServerCodes.QUIT , this.numeroLabel );
          enviaPacote(sendPdu);
          break;
        }
      case ServerCodes.END :
        {
          sendPdu = new BasePdu ( ServerCodes.END , this.numeroLabel );
          enviaPacote(sendPdu);
          break;
        }
      case ServerCodes.LIST_CHALLENGES :
        {
          sendPdu = new BasePdu ( ServerCodes.LIST_CHALLENGES , this.numeroLabel );
          enviaPacote(sendPdu);
          break;
        }
      case ServerCodes.MAKE_CHALLENGE :
        {
          sendPdu = new BasePdu ( ServerCodes.MAKE_CHALLENGE , this.numeroLabel );
          System.out.println("Nome do desafio:");
          String nomeDesafio = Input.lerString(sc);
          CampoPdu campoNomeDesafio = new CampoPdu ( ServerCodes.CLIENTE_NOME_DESAFIO );
          campoNomeDesafio.adicionaString(nomeDesafio);
          sendPdu.adicionaCampoPdu(campoNomeDesafio);
          System.out.println("Pretende indicar data e hora (s/n)");
          String resposta = Input.lerString(sc);
          if ( resposta.equals("s")){
            System.out.println("Ano:");
            int ano = Input.lerInt(sc);
            System.out.println("Mes:");
            int mes = Input.lerInt(sc);
            System.out.println("Dia:");
            int dia = Input.lerInt(sc);
            System.out.println("Hora:");
            int hora = Input.lerInt(sc);
            System.out.println("Minutos:");
            int minutos = Input.lerInt(sc);
            System.out.println("Segundos:");
            int segundos = Input.lerInt(sc);
            CampoPdu campoData = new CampoPdu ( ServerCodes.CLIENTE_DATA );
            CampoPdu campoHora = new CampoPdu ( ServerCodes.CLIENTE_HORA ); 
            Date dataHora = new Date();
            dataHora.setYear(ano);
            dataHora.setMonth(mes);
            dataHora.setDate(dia);
            dataHora.setSeconds(hora);
            dataHora.setSeconds(minutos);
            dataHora.setSeconds(segundos);
            campoData.adicionaData(dataHora);
            campoHora.adicionaHora(dataHora);
            sendPdu.adicionaCampoPdu(campoData);
            sendPdu.adicionaCampoPdu(campoHora);
          }
          enviaPacote(sendPdu);
          break;
        }
      case ServerCodes.ACCEPT_CHALLENGE :
        {
          sendPdu = new BasePdu ( ServerCodes.ACCEPT_CHALLENGE , this.numeroLabel );
          System.out.println("Nome do desafio:");
          String nomeDesafio = sc.nextLine();
          CampoPdu campoNomeDesafio = new CampoPdu ( ServerCodes.CLIENTE_NOME_DESAFIO );
          campoNomeDesafio.adicionaString(nomeDesafio);
          sendPdu.adicionaCampoPdu(campoNomeDesafio);
          enviaPacote(sendPdu);
          break;
        }
      case ServerCodes.DELETE_CHALLENGE :
        {
          sendPdu = new BasePdu ( ServerCodes.DELETE_CHALLENGE , this.numeroLabel );
          System.out.println("Nome do desafio:");
          String nomeDesafio = sc.nextLine();
          CampoPdu campoNomeDesafio = new CampoPdu ( ServerCodes.CLIENTE_NOME_DESAFIO );
          campoNomeDesafio.adicionaString(nomeDesafio);
          sendPdu.adicionaCampoPdu(campoNomeDesafio);
          enviaPacote(sendPdu);
          break;
        }
      case ServerCodes.LIST_RANKING :
        {
          sendPdu = new BasePdu ( ServerCodes.LIST_RANKING , this.numeroLabel );
          enviaPacote(sendPdu);
          break;
        }
    }
  }

  private void resolvePacote(byte tipoPedido) throws Exception {
    numeroLabel++;
    byte[] udpReceber;
    DatagramPacket udpDataPacket;
    System.out.println( "Aguardando resposta de servidor na porta: " + remotePort);
    udpReceber = new byte[ ServerCodes.TAMANHO_MAX_PDU ];
    udpDataPacket = new DatagramPacket( udpReceber , udpReceber.length );
    serverSocket.receive( udpDataPacket );
    BasePdu novoPdu = new BasePdu ( udpDataPacket );
    novoPdu.parseCabecalho();
    novoPdu.parseCampos();
    System.out.println(novoPdu.toString());

    switch( tipoPedido ){
      case ServerCodes.HELLO :
        {
          if ( novoPdu.contemCampo( ServerCodes.SERVIDOR_OK ) ){
            System.out.println( "Conecção estabelecida UDP -> UDP");
            remoteDefinido = true;
          }
          else{
            System.out.println("falha na comunicação ponto a ponto!");
          }
          break;
        }
      case ServerCodes.REGISTER :
        {
          if ( novoPdu.contemCampo( ServerCodes.SERVIDOR_OK ) ){
            System.out.println( "Utilizador registado com sucesso:");
            this.logginValido = true;
          }
          else{
            if ( novoPdu.contemCampo( ServerCodes.SERVIDOR_ERRO ) ){
              CampoPdu campoErro = novoPdu.popCampo();
              String descricaoErro = campoErro.getCampoString();
              System.out.println("Erro: " + descricaoErro);
            }
          }
          break;
        }
      case ServerCodes.LOGIN :
        {
          if ( novoPdu.contemCampo( ServerCodes.SERVIDOR_NOME ) && novoPdu.contemCampo( ServerCodes.SERVIDOR_SCORE)){
            CampoPdu campoNome = novoPdu.popCampo();
            CampoPdu campoScore = novoPdu.popCampo();
            String nomeCliente = campoNome.getCampoString();
            int score = campoScore.getCampoInt2Bytes();
            System.out.println( "Login efectuado com sucesso:\n\tNome: " + nomeCliente +"\n\tPontos: "+ score);
            this.logginValido = true;
          }
          else{
            if ( novoPdu.contemCampo( ServerCodes.SERVIDOR_ERRO ) ){
              CampoPdu campoErro = novoPdu.popCampo();
              String descricaoErro = campoErro.getCampoString();
              System.out.println("Erro: " + descricaoErro);
            }
          }
          break;
        }
      case ServerCodes.LOGOUT :
        {
          if ( novoPdu.contemCampo( ServerCodes.SERVIDOR_OK ) ){
            System.out.println( "Logout realizado com sucesso:");
            this.logginValido = false;
          }
          else{
            if ( novoPdu.contemCampo( ServerCodes.SERVIDOR_ERRO ) ){
              CampoPdu campoErro = novoPdu.popCampo();
              String descricaoErro = campoErro.getCampoString();
              System.out.println("Erro: " + descricaoErro);
            }
          }
          break;
        }
      case ServerCodes.QUIT :
        {
          if ( novoPdu.contemCampo( ServerCodes.SERVIDOR_OK ) ){
            System.out.println( "Passou a pergunta.");
          }
          else{
            if ( novoPdu.contemCampo( ServerCodes.SERVIDOR_ERRO ) ){
              CampoPdu campoErro = novoPdu.popCampo();
              String descricaoErro = campoErro.getCampoString();
              System.out.println("Erro: " + descricaoErro);
            }
          }
          break;
        }
      case ServerCodes.END :
        {
          if ( novoPdu.contemCampo( ServerCodes.SERVIDOR_ALCUNHA ) && novoPdu.contemCampo( ServerCodes.SERVIDOR_SCORE )  ){
            ArrayList <CampoPdu> alcunhasScores= novoPdu.getArrayListCamposSeguintes();
            int numeroCampos = alcunhasScores.size();
            int campoNumero = 1;
            System.out.println( "Pontuações do Jogo");
            while ( campoNumero < numeroCampos ){
              CampoPdu alcunhaJogador = novoPdu.popCampo();
              CampoPdu scoreJogador = novoPdu.popCampo();
              String nome = alcunhaJogador.getCampoString();
              int score = scoreJogador.getCampoInt2Bytes();
              System.out.println( "Alcunha: " + nome + "\tScore: "+ score);
              campoNumero+=2;
            }
          }
          else{
            if ( novoPdu.contemCampo( ServerCodes.SERVIDOR_ERRO ) ){
              CampoPdu campoErro = novoPdu.popCampo();
              String descricaoErro = campoErro.getCampoString();
              System.out.println("Erro: " + descricaoErro);
            }
          }
          break;
        }
      case ServerCodes.LIST_CHALLENGES :
        {
          if ( novoPdu.contemCampo( ServerCodes.SERVIDOR_NOME_DESAFIO ) && novoPdu.contemCampo( ServerCodes.SERVIDOR_DATA_DESAFIO ) && novoPdu.contemCampo( ServerCodes.SERVIDOR_HORA_DESAFIO )  ){
            ArrayList <CampoPdu> desafiosDataHora= novoPdu.getArrayListCamposSeguintes();
            int numeroCampos = desafiosDataHora.size();
            int campoNumero = 1;
            while ( campoNumero < numeroCampos ){
              CampoPdu nomeDesafio = novoPdu.popCampo();
              CampoPdu dataDesafio = novoPdu.popCampo();
              CampoPdu horaDesafio = novoPdu.popCampo();
              String nome = nomeDesafio.getCampoString();
              int dataAno = dataDesafio.getCampoDataAno();
              int dataMes = dataDesafio.getCampoDataMes();
              int dataDia = dataDesafio.getCampoDataDia();
              int horaHora = horaDesafio.getCampoHoraHora();
              int horaMinutos = horaDesafio.getCampoHoraMinutos();
              int horaSegundos = horaDesafio.getCampoHoraSegundos();
              System.out.println( "Desafio: " + nome + "Data(AA/MM/DD): "+ dataAno + "/"+ dataMes + "/"+ dataDia + "/" + "Hora(HH:MM:SS): "+ horaHora + ":"+ horaMinutos + ":"+ horaSegundos);
              campoNumero+=3;
            }
          }
          else{
            if ( novoPdu.contemCampo( ServerCodes.SERVIDOR_ERRO ) ){
              CampoPdu campoErro = novoPdu.popCampo();
              String descricaoErro = campoErro.getCampoString();
              System.out.println("Erro: " + descricaoErro);
            }
          }
          break;
        }
      case ServerCodes.MAKE_CHALLENGE :
        {
          if ( novoPdu.contemCampo( ServerCodes.SERVIDOR_NOME_DESAFIO ) && novoPdu.contemCampo( ServerCodes.SERVIDOR_DATA_DESAFIO ) && novoPdu.contemCampo( ServerCodes.SERVIDOR_HORA_DESAFIO )  ){
            CampoPdu nomeDesafio = novoPdu.popCampo();
            CampoPdu dataDesafio = novoPdu.popCampo();
            CampoPdu horaDesafio = novoPdu.popCampo();
            String nome = nomeDesafio.getCampoString();
            int dataAno = dataDesafio.getCampoDataAno();
            int dataMes = dataDesafio.getCampoDataMes();
            int dataDia = dataDesafio.getCampoDataDia();
            int horaHora = horaDesafio.getCampoHoraHora();
            int horaMinutos = horaDesafio.getCampoHoraMinutos();
            int horaSegundos = horaDesafio.getCampoHoraSegundos();
            System.out.println( "Desafio Criado: " + nome + "Data(AA/MM/DD): "+ dataAno + "/"+ dataMes + "/"+ dataDia + "/" + "Hora(HH:MM:SS): "+ horaHora + ":"+ horaMinutos + ":"+ horaSegundos);
          }
          else{
            if ( novoPdu.contemCampo( ServerCodes.SERVIDOR_ERRO ) ){
              CampoPdu campoErro = novoPdu.popCampo();
              String descricaoErro = campoErro.getCampoString();
              System.out.println("Erro: " + descricaoErro);
            }
          }
          break;
        }
      case ServerCodes.ACCEPT_CHALLENGE :
        {
          if ( novoPdu.contemCampo( ServerCodes.SERVIDOR_OK ) ){
            System.out.println( "Logout realizado com sucesso:");
            this.logginValido = false;
          }
          else{
            if ( novoPdu.contemCampo( ServerCodes.SERVIDOR_ERRO ) ){
              CampoPdu campoErro = novoPdu.popCampo();
              String descricaoErro = campoErro.getCampoString();
              System.out.println("Erro: " + descricaoErro);
            }
          }
          break;
        }
      case ServerCodes.DELETE_CHALLENGE :
        {
          if ( novoPdu.contemCampo( ServerCodes.SERVIDOR_NOME_DESAFIO ) && novoPdu.contemCampo( ServerCodes.SERVIDOR_DATA_DESAFIO ) && novoPdu.contemCampo( ServerCodes.SERVIDOR_HORA_DESAFIO )  ){
            CampoPdu nomeDesafio = novoPdu.popCampo();
            CampoPdu dataDesafio = novoPdu.popCampo();
            CampoPdu horaDesafio = novoPdu.popCampo();
            String nome = nomeDesafio.getCampoString();
            int dataAno = dataDesafio.getCampoDataAno();
            int dataMes = dataDesafio.getCampoDataMes();
            int dataDia = dataDesafio.getCampoDataDia();
            int horaHora = horaDesafio.getCampoHoraHora();
            int horaMinutos = horaDesafio.getCampoHoraMinutos();
            int horaSegundos = horaDesafio.getCampoHoraSegundos();
            System.out.println( "Desafio Eliminado: " + nome + "Data(AA/MM/DD): "+ dataAno + "/"+ dataMes + "/"+ dataDia + "/" + "Hora(HH:MM:SS): "+ horaHora + ":"+ horaMinutos + ":"+ horaSegundos);
          }
          else{
            if ( novoPdu.contemCampo( ServerCodes.SERVIDOR_ERRO ) ){
              CampoPdu campoErro = novoPdu.popCampo();
              String descricaoErro = campoErro.getCampoString();
              System.out.println("Erro: " + descricaoErro);
            }
          }
          break;
        }
      case ServerCodes.LIST_RANKING :
        {
          if ( novoPdu.contemCampo( ServerCodes.SERVIDOR_ALCUNHA ) && novoPdu.contemCampo( ServerCodes.SERVIDOR_SCORE )  ){
            ArrayList <CampoPdu> alcunhasScores= novoPdu.getArrayListCamposSeguintes();
            int numeroCampos = alcunhasScores.size();
            int campoNumero = 1;
            while ( campoNumero < numeroCampos ){
              CampoPdu alcunhaJogador = novoPdu.popCampo();
              CampoPdu scoreJogador = novoPdu.popCampo();
              String nome = alcunhaJogador.getCampoString();
              int score = scoreJogador.getCampoInt2Bytes();
              System.out.println( "Alcunha: " + nome + "\tScore: "+ score);
              campoNumero+=2;
            }
          }
          else{
            if ( novoPdu.contemCampo( ServerCodes.SERVIDOR_ERRO ) ){
              CampoPdu campoErro = novoPdu.popCampo();
              String descricaoErro = campoErro.getCampoString();
              System.out.println("Erro: " + descricaoErro);
            }
          }
          break;
        }
    }
  }
}
