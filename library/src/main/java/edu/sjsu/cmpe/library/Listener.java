package edu.sjsu.cmpe.library;

import java.net.MalformedURLException; 
import java.net.URL; 
import java.util.StringTokenizer; 

import javax.jms.Connection; 
import javax.jms.Destination; 
import javax.jms.JMSException; 
import javax.jms.Message; 
import javax.jms.MessageConsumer; 
import javax.jms.Session; 
import javax.jms.TextMessage;

import org.fusesource.stomp.jms.StompJmsConnectionFactory; 
import org.fusesource.stomp.jms.StompJmsDestination; 
import org.fusesource.stomp.jms.message.StompJmsMessage;

import edu.sjsu.cmpe.library.config.LibraryServiceConfiguration;
import edu.sjsu.cmpe.library.domain.Book; 
import edu.sjsu.cmpe.library.domain.Book.Status; 
//import edu.sjsu.cmpe.library.repository.BookRepository;
import edu.sjsu.cmpe.library.repository.BookRepositoryInterface;

public class Listener
{
	private String stompTopic;
    private String apolloUser;
    private String apolloPassword;
    private String apolloHost;
    private int apolloPort;
    private final LibraryServiceConfiguration configuration;
    private BookRepositoryInterface bookRepository;
    
    private long isbn[]= new long[3]; 
    private String title[] = new String[3]; 
    private String category[] = new String[3]; 
    private String coverImage[] = new String[3]; 
    private int i;
    private String port;

    public Listener(LibraryServiceConfiguration config, BookRepositoryInterface bookRepository)
    {
            this.configuration = config;
            this.bookRepository = bookRepository;
            apolloUser = configuration.getApolloUser();
            apolloPassword = configuration.getApolloPassword();
            apolloHost = configuration.getApolloHost();
            apolloPort = configuration.getApolloPort();
            stompTopic = configuration.getStompTopicName();
            port = ""+apolloPort;
    }

public void msgReceiver() 
{ 
	while(true)
	{
		try{
			
			System.out.println("******Inside listener*******");
			String user = env("APOLLO_USER", apolloUser); 
			String password = env("APOLLO_PASSWORD", apolloPassword); 
			String host = env("APOLLO_HOST", apolloHost); 
			int apolloport = Integer.parseInt(env("APOLLO_PORT", port)); 
			String destination = stompTopic;
			System.out.println("Destination is :: "+destination);
			StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
			factory.setBrokerURI("tcp://" + host + ":" + apolloport);

			Connection connection = factory.createConnection(user, password);
			connection.start();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Destination dest = new StompJmsDestination(destination);

			MessageConsumer consumer = session.createConsumer(dest);
			System.currentTimeMillis();
			i=0;
			System.out.println("------> Waiting for messages..........");
			while(true) {
			Message msg = consumer.receive();
			if( msg instanceof  TextMessage ) {
				String body = ((TextMessage) msg).getText();
				if( "SHUTDOWN".equals(body) ) {
					break;
				}
				System.out.println(" The Received message is = " + body);
				
		                	StringTokenizer str = new StringTokenizer(body,":,\"");
		                	while(str.hasMoreTokens())
		                	{
		                		isbn[i]= Long.parseLong(str.nextToken());
		                		System.out.println(" Book isbn :: " + isbn[i]);		          
		                		title[i]=str.nextToken();
		                		System.out.println(" Book Title :: "+title[i]);		                		
		                		category[i] = str.nextToken();
		                		System.out.println(" Book Category ::  "+category[i]);		                		
		                		coverImage[i]  = str.nextToken()+":"+str.nextToken();
		                		System.out.println(" Book Cover Image :: " +coverImage[i]);
		                		i++;
		                	}
		                	if(i == 3){break;}		                
				}
			else if (msg instanceof StompJmsMessage)
			{
				StompJmsMessage smsg = ((StompJmsMessage) msg);
				String body = smsg.getFrame().contentAsString();
				if ("SHUTDOWN".equals(body))
				{
					break;
				}
				System.out.println(" The Received message is = " + body);
				//System.out.println(" **Message**");
				}
			else
				{
					System.out.println("Unexpected message type: "+msg.getClass());
				}								   	 		  
		}
		connection.close();
		 }
		 catch(JMSException e)
		 {
			 e.printStackTrace();
		 } 

		System.out.println("**** Waiting for a message****");
		Book book=null;
		   
			int i;
		    long id;
		   for(i=0;i<3;i++)
		   {
		   
		   id= isbn[i];
		   System.out.println(" ----------The ID is-------"+id);
		   if(id!=0)
		   {
			   // get book by isbn and check status 
		   book=bookRepository.getBookByISBN(id);
		   if(book!=null)
		   {
		   	System.out.println(" **BOOK FOUND **");
		   	if(book.getStatus()==Status.lost);
		   	{
		   	System.out.println(" Book ISBN :: " + book.getIsbn());
		   	System.out.println(" Book Title :: "+book.getTitle());
		   	   System.out.println(" Book Status :: "+book.getStatus());
		   	   	
		           bookRepository.updateStatus(id,Status.available);		   	
		   	}		       
		   }
		   else
		   {
		   		Book newbookobj = new Book();
		   		newbookobj.setStatus(Status.available);
		   		newbookobj.setIsbn(isbn[i]);
		   		newbookobj.setTitle(title[i]);
		   		newbookobj.setCategory(category[i]);
		   		try
		   		{
		   			newbookobj.setCoverimage(new URL(coverImage[i]));
		   		}
		   		catch (MalformedURLException e)
		   		{
		   			// 	TODO Auto-generated catch block
		   			e.printStackTrace();
		   		}
		   	
		   		bookRepository.addBooks(id, newbookobj);
		   			   	
		   	}
		  
		}   
		   
  }
		   	
		try
		{
			Thread.sleep(10000);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
} 
																		
private static String env(String key, String defaultValue) 
{ 
	String rc = System.getenv(key); 
	if( rc== null ) 
	{ 
		return defaultValue; 
	} 
	return rc; 
	}

}