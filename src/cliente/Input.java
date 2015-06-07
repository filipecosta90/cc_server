package cliente;

/**
 * Classe que abstrai a utilização da classe Scanner, escondendo todos os
 * problemas relacionados com excepções, e que oferece métodos simples e
 * robustos para a leitura de valores de tipos simples.
 *
 * -----  Utilização: Exemplos
 *
 * int i = Input.lerInt();
 * String linha = Input.lerString();
 * double raio = Input.lerDouble();
 * ---------------------------------------
 *
 * @author F. Mário Martins
 * @version 1.0 (6/2006)
 */
import static java.lang.System.out;

import java.io.Serializable;
import java.util.Scanner;
import java.util.InputMismatchException;

import server.CampoPdu;

public class Input implements Serializable{

  /**
   * Métodos de Classe
   */

  public static String lerString( Scanner input ) {
    boolean ok = false; 
    String txt = "";
    while(!ok) {
      try {
        txt = input.next();
        ok = true;
      }
      catch(InputMismatchException e) 
      { out.println("Texto Inválido"); 
        out.print("Novo valor: ");
        input.nextLine(); 
      }
    }
    return txt;
  } 


  public static int lerInt( Scanner input ) {
    boolean ok = false; 
    int i = 0; 
    while(!ok) {
      try {
        i = input.nextInt();
        ok = true;
      }
      catch(InputMismatchException e) 
      { out.println("Inteiro Inválido"); 
        out.print("Novo valor: ");
        input.nextLine(); 
      }
    }
    return i;
  } 


  public static byte lerByte( Scanner input ) {
    boolean ok = false; 
    byte [] i = new byte[1] ; 
    int num = 0;
    while(!ok) {
      try {
        num = input.nextInt();
        i = CampoPdu.intPara1Byte(num);
        ok = true;
      }
      catch(InputMismatchException e) 
      { out.println("Inteiro Inválido"); 
        out.print("Novo valor: ");
        input.nextLine(); 
      }
    }
    return i[0];
  } 
}
