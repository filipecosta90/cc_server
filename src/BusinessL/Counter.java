package BusinessL;

public class Counter {
  private short c;

  public Counter(){ c=1; }

  public synchronized short genID() 
  { 
    if( c>( 32767 ))
      c = 0;
    c++;
    return c;
  }
}
