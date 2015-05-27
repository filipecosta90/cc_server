package server;

/*
* @author Filipe Oliveira, Ricardo Agra, Sérgio Caldas
* @author a57816(at)alunos.uminho.pt , a47069(at)alunos.uminho.pt , a57779(at)alunos.uminho.pt
* @version 0.1
*/

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Console implements Runnable
{
	private ClientPduBuilder builder=new ClientPduBuilder();
	private ClientIO io=new ClientIO( this );
	private boolean isLogged=false, inGame=false;
	private String nick="Admin";
	private String desafio;
	private short id;
	private Scanner s;

	@Override
	public void run()
	{
		login_Handler();
	}
	/**
	 * 											Menus
	 */
	// Log-in
	public String login_menu()
	{
		String menu=
		"\n1 : Hello\n"
		+"2 : Registar\n"
		+"3 : Log In\n"
		+"0 : Sair";
		return menu;
	}
	public void login_Handler()
	{
		System.out.println( login_menu() );
		int op=readOp( 3 );

		switch( op )
		{
			case 1 : 
			{
				io.send( builder.HELLO() );
				break;
			}
			case 2 : 
			{
				String name=read( "Nome" );
				this.nick=read( "Alcunha" );
				io.send( builder.REGISTER( name,nick,read( "Password" )));
				break;
			}
			case 3 :
			{
				this.nick=read( "Alcunha" );
				io.send( builder.LOGIN( nick,read( "Password" )));
				break;
			}
			default : break;
		}
	}
	// 											___Games___
	public static String jogos_Menu()
	{
		String menu=
		"\n1 : Listar Desafios\n"
		+ "2 : Criar Desafio\n"
		+ "3 : Aceitar Desafio\n"
		+ "4 : Apagar Desafio\n"
		+ "0 : Logout";
		return menu;
	}
	public void jogos_Handler()
	{
		System.out.println( jogos_Menu() );
		int op = -1;
		op = readOp( 4 );
		
		switch( op )
		{			 
			case 1 : 
			{
				io.send( builder.LIST_CHALLENGES( id ) );
				break;
			}
			case 2 :
			{
				io.send( builder.MAKE_CHALLENGE( id,read( "Nome do desafio" ),getDate(),getHour() ));
				break;
			}			 
			case 3 : 
			{
				this.desafio=read( "Nome do desafio" );
				this.inGame=true;
				io.send( builder.ACCEPT_CHALLENGE( id,desafio ));
			 	break;
			} 				 
			case 4 :
			{
				io.send( builder.DELETE_CHALLENGE( id,read( "Nome do desafio" )));
			 	break;
			}
			case 0 :
			{
				io.send( builder.LOGOUT( id ));
				login_Handler();
				break;
			}
			default : break;
		}
	}
	public static String desafio_Menu()
	{
		String menu=
		"\n1 : Listar Desafios\n"
		+ "2 : Criar Desafio\n"
		+ "3 : Aceitar Desafio\n"
		+ "4 : Apagar Desafio\n"
		+ "0 : Logout";
		return menu;
	}
	/**
	 * 									___Message Handlers___
	 */
	public void handle_OK( int id )
	{
		System.out.println( "[ Server: OK ]" );
		login_Handler();
	}
	public void handle_LogFail( int type,String nick )
	{
		if( type==-1 )
			System.out.println( "[ Server: Alcunha( "+nick+" ) ja existe. ]" );
		else
			System.out.println( "[ Server: Alcunha ou password errada.  ]" );
		login_Handler();
	}
	public void handle_Erro( String message )
	{
		System.out.println( "[ Server: "+message+" ]" );
		if( inGame )
		{
			System.out.println( "Aguardando proxima pergunta..." );
		}
		else
			if( isLogged )
				jogos_Handler();
			else
				login_Handler();
	}
	public void handle_Frag( ArrayList< String >list )
	{
		System.out.println( "[ Server: Desafios ]" );
		int i=1;
		for( String jogo : list )
			System.out.println( i+++": "+jogo );
		jogos_Handler();
	}
	public void handle_Logs()
	{
		System.out.println( "[ Server: Bem Vindo "+this.nick+"! ]" );
		this.isLogged=true;
		jogos_Handler();
	}
	public void handle_Challange( String text )
	{
		System.out.println( "[ Server: informacao sobre "+text+" actualizada com sucesso. ]" );
		jogos_Handler();
	}
	public void handle_Question( int numb,String question,String[] ops )
	{
		System.out.println( "[ Server: Pergunta "+numb+" ]" );
		System.out.println( "- "+question );
		int i=1;
		for( String op : ops )
			System.out.println( i+++": "+op );
		System.out.println( "(0 para terminar)" );
	
		int op=readOp( 3 );
		if( op==0 )
		{
			this.inGame=false;
			io.send( builder.END( id,desafio ));
		}
		else
			io.send( builder.ANSWER( id,numb,op,desafio ));
	}
	/**
	 *  								___Keyboard input functions___
	 */
	private int readOp( int max )
	{
		int op = -1;
		s = new Scanner( System.in );
		while( op<0 )
		{
			try{
				op = s.nextInt();
				if( op>max )
				{
					System.out.println( "Opcao invalida." );
					op = -1;
				}
			} catch( InputMismatchException e )
			{ 
				System.out.println( "Opcao invalida." );
				s.next();
			}
		}
		return op;	
	}
	private String read( String campo )
	{
		s = new Scanner( System.in );
		
		System.out.print( campo+" : " );
		return s.nextLine();
	}
	/**
	 * 								___Auxiliary functions___
	 */
	private short[] getDate()
	{
		short[] data=new short[ 3 ];
		GregorianCalendar greg=new GregorianCalendar();
		
		data[ 0 ]=( short )( greg.get( Calendar.YEAR )%100 );
		data[ 1 ]=( short )greg.get( Calendar.MONTH );
		data[ 2 ]=( short )greg.get( Calendar.DAY_OF_MONTH ); 
		
		return data;
	}
	private short[] getHour()
	{
		short[] hora=new short[ 3 ];
		GregorianCalendar greg=new GregorianCalendar();
		
		hora[ 0 ]=( short )greg.get( Calendar.HOUR );
		hora[ 1 ]=( short )greg.get( Calendar.MINUTE );
		hora[ 2 ]=( short )greg.get( Calendar.SECOND ); 
		
		return hora;
	}
	
	public void setNick( String nick ){ this.nick=nick; }
	public String getNick(){ return this.nick; }
	
	public void setID( short id ){ this.id=id; }
	public int getID(){ return this.id; }
	
	public boolean getLogged(){ return this.isLogged; }
}