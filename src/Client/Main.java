package Client;

public class Main {
  public static void main( String[] args )throws InterruptedException
  {
    Console console=new Console();
    Thread thread=new Thread( console );
    thread.start();
  }
}
