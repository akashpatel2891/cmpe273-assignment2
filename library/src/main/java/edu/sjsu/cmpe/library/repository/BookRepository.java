package edu.sjsu.cmpe.library.repository;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.fusesource.stomp.jms.StompJmsConnectionFactory;
import org.fusesource.stomp.jms.StompJmsDestination;


import edu.sjsu.cmpe.library.config.LibraryServiceConfiguration;
//import edu.sjsu.cmpe.library.config.LibraryServiceConfiguration;
import edu.sjsu.cmpe.library.domain.Book;
import edu.sjsu.cmpe.library.domain.Book.Status;

public class BookRepository implements BookRepositoryInterface
{
    /** In-memory map to store books. (Key, Value) -> (ISBN, Book) */
    private final ConcurrentHashMap<Long, Book> bookInMemoryMap;

    public static BookRepository bookRepository=null;
    /** Never access this key directly; instead use generateISBNKey() */
    private long isbnKey;
    LibraryServiceConfiguration config;
    public BookRepository(LibraryServiceConfiguration config)
    {
    	bookInMemoryMap = seedData();
    	this.config=config;
    	isbnKey = 0;
    }

    private ConcurrentHashMap<Long, Book> seedData()
    {
    	ConcurrentHashMap<Long, Book> bookMap = new ConcurrentHashMap<Long, Book>();
    	Book book = new Book();
    	book.setIsbn(1);
    	book.setCategory(" Computer ");
    	book.setTitle(" Java Concurrency in Practice ");
	try 
	{
	    book.setCoverimage(new URL("http://goo.gl/N96GJN"));
	}
	catch (MalformedURLException e)
	{
	    // eat the exception
	}
	bookMap.put(book.getIsbn(), book);

		book = new Book();
		book.setIsbn(2);
		book.setCategory(" computer ");
		book.setTitle(" Restful Web Services ");
	try
	{
	    book.setCoverimage(new URL("http://goo.gl/ZGmzoJ"));
	}
	catch (MalformedURLException e)
	{
	    // eat the exception
	}
		bookMap.put(book.getIsbn(), book);

		return bookMap;
    }

    /**
     * This should be called if and only if you are adding new books to the
     * repository.
     * 
     * @return a new incremental ISBN number
     */
    private final Long generateISBNKey()
    {
	// increment existing isbnKey and return the new value
    	return Long.valueOf(++isbnKey);
    }

    /**
     * This will auto-generate unique ISBN for new books.
     */
    @Override
    public Book saveBook(Book newBook)
    {
    	checkNotNull(newBook, "newBook instance must not be null");
    	// Generate new ISBN
    	Long isbn = generateISBNKey();
    	newBook.setIsbn(isbn);
    	// 	TODO: create and associate other fields such as author

    	// Finally, save the new book into the map
    	bookInMemoryMap.putIfAbsent(isbn, newBook);

    	return newBook;
    }

    /**
     * @see edu.sjsu.cmpe.library.repository.BookRepositoryInterface#getBookByISBN(java.lang.Long)
     */
    @Override
    public Book getBookByISBN(Long isbn)
    {
    	checkArgument(isbn > 0,
    			"ISBN was %s but expected greater than zero value", isbn);
    	return bookInMemoryMap.get(isbn);
    }

    @Override
    public List<Book> getAllBooks()
    {
    	return new ArrayList<Book>(bookInMemoryMap.values());
    }

    /*
     * Delete a book from the map by the isbn. If the given ISBN was invalid, do
     * nothing.
     * 
     * @see
     * edu.sjsu.cmpe.library.repository.BookRepositoryInterface#delete(java.
     * lang.Long)
     */
    @Override
    public void delete(Long isbn) {
	bookInMemoryMap.remove(isbn);
    }

    public Book update(Long isbn, Status status) throws JMSException {
    	
    	Book book = new Book();
    	//LibraryServiceConfiguration configuration = new LibraryServiceConfiguration();
    	book = getBookByISBN(isbn);
    	book.setStatus(status);
    	bookInMemoryMap.put(isbn, book);
    	System.out.println(" hello...!!");
    	if (config.getStompTopicName().equals("/topic/04165.book.computer"))
    	{
    		messageProducer("library-b:"+isbn);
    	}
    	else
    	{
    		messageProducer("library-a:"+isbn);
    	}
    	
    	return book;
        }

        public void updateStatus(Long isbn,Book.Status status) 
        { 
        	Book book = getBookByISBN(isbn); 
        	book.setStatus(status); 
        	bookInMemoryMap.put(isbn, book);  
        } 
        
        public void messageProducer(String isbn) throws JMSException{
        	
        	String user = env("APOLLO_USER",config.getApolloUser());
        	String password = env("APOLLO_PASSWORD", config.getApolloPassword());
        	String host = env("APOLLO_HOST", config.getApolloHost());
        	int port = config.getApolloPort();
        	String queue = "/queue/04165.book.orders";
        	String destination = queue;

        	StompJmsConnectionFactory factory = new StompJmsConnectionFactory();
        	factory.setBrokerURI("tcp://" + host + ":" + port);
        	//System.out.println("hi2");
        	Connection connection = factory.createConnection(user, password);
        	connection.start();
        	Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        	Destination dest = new StompJmsDestination(destination);
        	MessageProducer producer = session.createProducer(dest);
        	producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        	System.out.println("Sending messages to " + queue + "...");
    
        	TextMessage msg = session.createTextMessage(isbn);
        	msg.setLongProperty("id", System.currentTimeMillis());
        	producer.send(msg);
    
        	connection.close();
        	
        }
        
        public void addBooks(Long isbn,Book book) 
        { 
        	bookInMemoryMap.put(isbn,book);  
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