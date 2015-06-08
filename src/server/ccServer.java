/*
 * @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
 * @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
 * @version 0.1
 */

package server;


import java.io.Serializable;
import java.util.Scanner;
import cliente.Input;

public class ccServer implements Serializable {

	private static final long serialVersionUID = -774157080972569322L;
	
private transient static Scanner sc;
  public static boolean desligar;

  public static void navegaServidor( Server servidor ) throws Exception {
    StringBuilder s = new StringBuilder();
    s.append("--- Gestao Servidor ---\n\n");
    s.append("\t1 - Gerir users locais ");
    s.append("\t2 - Gerir jogos ");
    s.append("\t3 - Listar ranking local ");
    s.append("\t4 - Gerir Servidores ");
    s.append("\t5 - Desligar ");
    s.append("\t6 - Carregar estado ");
    s.append("\t7 - Guardar estado ");
    s.append("\n-----------------------\n");
    s.append("\nopção:");
    System.out.println(s.toString());
    int opcao = Input.lerInt(sc);
    resolveOpcao( opcao , servidor );
  }

  public static void resolveOpcao( int opcao , Server servidor ) throws Exception{
    switch ( opcao) {
      case 5:
        {
          desligar = true;
          break;
        }
      case 1:
        {
          gereUsers(servidor);
          break;
        }
      case 2:
        {
          gereJogos();
          break;
        }
      case 3: 
        {
          listarRanking();
          break;
        }
      case 4:
        {
          gereServidores();
          break;
        }
      case 6:
        {
          servidor = servidor.carregaServidor();
          break;
        }
      case 7:
        {
          servidor.guardaServidor();
          break;
        }
    }
  }

  private static void gereServidores() {
    // TODO Auto-generated method stub

  }

  private static void listarRanking() {

  }

  private static void gereJogos() {
    // TODO Auto-generated method stub

  }

  private static  void gereUsers( Server server ) {
    StringBuilder s = new StringBuilder();
    s.append("--- Gestao Users ---\n\n");
    s.append("\n\t1 - Listar ");
    s.append("\n\t2 - Eliminar ");
    s.append("\n\t3 - Adicionar ");
    s.append("\n\t4 - Alterar ");
    s.append("\n--------------------\n");
    s.append("\nopção:");
    System.out.println(s.toString());
    int opcao = Input.lerInt(sc);
    switch ( opcao) {
      case 1:
        {
          String resultado = server.getStringUsers();
          System.out.println(resultado);
          break;
        }
      case 2:
        {
          server.eliminarUser();
          break;
        }
      case 3:
        {
          server.adicionarUser();
          break;
        }
      case 4: 
        {
          server.alterarUser();
          break;
        }
    }
  }

  public static void main( String[] args ) throws Exception 
  {
    Server localServer = new Server ( 3030 , 7070 , "data/listaDesafios.txt" );
    sc = new Scanner ( System.in );
    desligar = false;
    Thread thread = new Thread( localServer );
    thread.start();
    while (desligar == false){
      navegaServidor( localServer );
    }
    System.out.println("Desligando Servidor!");
  }
}
