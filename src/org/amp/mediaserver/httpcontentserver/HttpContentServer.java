package org.amp.mediaserver.httpcontentserver;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

import org.amp.mediaserver.httpcontentserver.handlers.HttpCoreFileHandler;
import org.amp.mediaserver.httpcontentserver.handlers.NotFoundHandler;
import org.amp.mediaserver.support.AvailabilityChangeListener;
import org.amp.mediaserver.support.FileNamespace;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

public class HttpContentServer implements AvailabilityChangeListener<File> {
	
	////////////////////////////////////////////////////////////////////////////////////
	// Singleton pattern.
	////////////////////////////////////////////////////////////////////////////////////
	private static HttpContentServer instance = null;
	
	final private static Logger log = Logger.getLogger(HttpContentServer.class.getName());
	
		
	public static HttpContentServer getInstance() throws Exception {
		if(instance == null) {
			instance = new HttpContentServer();
		}
		return instance;
	}
	
	public synchronized static void start(int maxConnections) throws Exception {
		if(getInstance().serverSocket != null) {		
			throw new IllegalStateException("Attempting to start server twice.");
		}
		
		if(maxConnections > 0)
			getInstance().lock = new Semaphore(maxConnections);
		
		getInstance().serverSocket = new ServerSocket(0);				
		log.info(String.format("New HTTP server on %s:%d", getInstance().serverSocket.getInetAddress(), getInstance().getPort()));
			
		getInstance().service = Executors.newCachedThreadPool(new HttpContentServerThreadFactory());
		getInstance().service.submit(getInstance().new HostService());	
	}	
	
	public synchronized static void stop() throws Exception {
		// This might hurt a little.
		log.info("HTTP server shutdown.");
		List<Runnable> unfinished = getInstance().service.shutdownNow();
		log.fine( String.format("Service shutdown complete (%d unfinished).", unfinished.size()) );
		getInstance().service = null;
		getInstance().lock = null;
		getInstance().serverSocket = null;
	}

	////////////////////////////////////////////////////////////////////////////////////
	// 
	////////////////////////////////////////////////////////////////////////////////////
	protected final HttpParams params = new BasicHttpParams();
	protected final HttpRequestHandlerRegistry handlerRegistry = new HttpRequestHandlerRegistry();	
	protected final BasicHttpProcessor httpproc = new BasicHttpProcessor();	
	protected final HttpService httpService;
	protected ExecutorService service;
	private Semaphore lock = null;
	protected ServerSocket serverSocket;
	
	protected HttpContentServer() throws Exception {
		params.setParameter(CoreProtocolPNames.ORIGIN_SERVER, "MediaHttpServer/1.1");
		params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
		params.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024);
		params.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false);
		params.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true);
		
		httpproc.addInterceptor(new ResponseServer());
		httpproc.addInterceptor(new ResponseDate());					
		httpproc.addInterceptor(new ResponseConnControl());
		httpproc.addInterceptor(new ResponseContent(true));
		httpproc.addInterceptor(new ResponseContentStrict());
		
		// TODO: StrictContentLengthStrategy?
				
		handlerRegistry.register("*", new NotFoundHandler());
				
		httpService = new HttpService( httpproc,
									   new DefaultConnectionReuseStrategy(),
									   new DefaultHttpResponseFactory(), handlerRegistry, params);
		
		// The following line is an example of a handler that forwards jar resources.
		//  Potential usage: Sharing html,css,js for custom pages.			
		// handlerRegistry.register("/debug/restest", new JarResourceForwardingHandler("/Icon120x120.jpg", "image/jpeg"));		
	}
	
	////////////////////////////////////////////////////////////////////////////////////
	// 
	////////////////////////////////////////////////////////////////////////////////////
	private static class HttpContentServerThreadFactory implements ThreadFactory {		
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			// log.fine("New thread " + t.getId());
			// TODO: ..Configure daemon status?
			// TODO: Name thread?
			t.setName("HttpContentServer-" + t.getId());
			return t;
		}		
	}
	
	/////////////////////////////////////////////////////////////////////
	// We're a threaded server!
	/////////////////////////////////////////////////////////////////////
	private class HostService implements Runnable {
		@Override
		public void run() {									
			while (!Thread.interrupted()) {
				try {
					if(lock != null) {
						lock.acquire();
					}
					
					Socket clientSocket = serverSocket.accept();						
					DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
					conn.bind(clientSocket, params);	
					service.submit(new Transaction(conn));							
				} catch (SocketException ex) {
					// 
					if (!Thread.interrupted()) {
						log.fine("Socket exception: " + ex.getMessage());
					}					
					if(service.isShutdown())
						break;					
				} catch (InterruptedIOException ex) {
					log.fine("Thread interrupted: " + ex.getMessage());
					break;
				} catch (IOException ex) {
					log.fine("IOException: " + ex.getMessage());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			
			if (!serverSocket.isClosed()) {
				try {
					serverSocket.close();
				} catch (IOException e) { e.printStackTrace(); }
			}
			log.fine("Server thread leaving scope.");
		}
	}
	/////////////////////////////////////////////////////////////////////
	// Handle client connections (Not scary. Just a huge-ass try-catch).
	/////////////////////////////////////////////////////////////////////
	/* unused exception types. We're generalizing all these into one exception handler now.
	 * we can break this back out if we decide to do anything unique per error type.
	 } catch (SocketTimeoutException ex) {
							
			} catch (IOException ex) {
				// Could be a peer connection reset, no warning
				System.err.println(getClass().getName() + ex.getMessage());
			} catch (HttpException ex) {
				System.err.println(getClass().getName() + ex.getMessage());
	 */
	protected class Transaction extends BasicHttpContext implements Runnable {
		private final HttpServerConnection conn;
		
		Transaction(DefaultHttpServerConnection connection) {
			super(null);			
			conn = connection;			
		}
		
		@Override
		public void run() {
			try {
				while (!Thread.interrupted() && conn.isOpen()) {
					httpService.handleRequest(conn, this);					
				}		
			} catch (ConnectionClosedException ex) {
				// Client close the connection. Leave quietly.
			} catch (SocketTimeoutException ex){
				// SocketTimeout is expected. Don't display error message.			
			} catch(SocketException ex) {
				// Ignore this, socket closed early.
			} catch (Exception ex) {				
				ex.printStackTrace();
			} finally {
				try {					
					conn.shutdown();
				} catch (IOException ex) {
					log.fine("Transaction$conn.shutdown(): " +  ex.getMessage());
				}
				
				if(lock != null) {
					lock.release();
				}
			}
		}	
	}

	
	@Override
	public void add(Object source, File file) {
		String suffix = FileNamespace.get(file);		
		handlerRegistry.register("/" + suffix + "*", new HttpCoreFileHandler(file));		
	}

	@Override
	public void remove(Object source, File file) {
		String suffix = FileNamespace.get(file);
		handlerRegistry.unregister("/" + suffix + "*");
	}
	
	public int getPort() {
		if(serverSocket == null) {
			throw new IllegalStateException();
		}
		return serverSocket.getLocalPort();
	}
	
}
