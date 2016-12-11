package nodebox.network;

import java.net.URI;
import java.nio.ByteBuffer;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.websocket.DeploymentException;

import java.io.IOException;
import java.util.concurrent.*;
//import java.util.UUID;
//import java.util.concurrent.ConcurrentLinkedQueue;
//import java.util.concurrent.ConcurrentHashMap;
//import static java.lang.System.out;
/*
Flow:
Whenever a node wants to send websocket data it adds a value to the OUT map by calling sendMessage(), send message returns a GUID which is also the key for the map entry
The node then blocks in a loop quierying the IN map with the GUID given to it
Meanwhile another thread is constantly looking at the OUT map and sends websocket data when a new map entry is created
the same thread is also reading websocket data and adding to the IN queue.
the same thread also picks up on special incoming request for application control

 */

@ClientEndpoint
public class webSocketClientEndpoint {
    public boolean isConnected = false;
    Session userSession = null;
    private MessageHandler messageHandler;
    private URI endpointURI;
    private final ExecutorService pool = Executors.newFixedThreadPool(10);

    private Future<Session> asyncConnectToServer(Object annotatedEndpointInstance, URI uri) {
        return pool.submit(new Callable<Session>() {
            @Override
            public Session call() throws Exception {
                try {
                    WebSocketContainer container = ContainerProvider.getWebSocketContainer();
                    return(container.connectToServer(annotatedEndpointInstance, uri));
                } catch (DeploymentException | IOException | IllegalStateException  e) {
                    //throw new RuntimeException(e);
                    return(null);
                }
            }
        });
    }


    public webSocketClientEndpoint(URI endpointURI, long timeout) {

        this.endpointURI = endpointURI;
        final Future<Session> futureSes = asyncConnectToServer(this, endpointURI);

        try {
            Session ses = futureSes.get(timeout, TimeUnit.MILLISECONDS);
        } catch(InterruptedException | ExecutionException | TimeoutException  e) {
            System.out.println("Time out...");
        }
    }

    // Callback hook for Connection open events.
    @OnOpen
    public void onOpen(Session userSession) {
        System.out.println("Connected!");
        this.userSession = userSession;
        this.isConnected = true;
    }

    // Callback hook for Connection close events.
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        System.out.println("Disconnected from server.");
        this.userSession = null;
        this.isConnected = false;
    }

    // Callback hook for Message Events. This method will be invoked when a client send a message.
    @OnMessage
    public void onMessage(String message) {
        if (this.messageHandler != null)
            this.messageHandler.handleMessage(message);
    }

    // register message handler
    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    // Send a message to the server.
    public boolean sendMessage(String message) {
        try {
            this.userSession.getAsyncRemote().sendText(message);
            return true;
        } catch(Exception e) {
            return false;
        }

    }

    public void ping() {
        try{
            System.out.println("Send ping...");
            this.userSession.getAsyncRemote().sendPing(ByteBuffer.wrap("ping".getBytes()));
        } catch(IOException e) {

        }

    }

    // Message handler.
    public static interface MessageHandler {
        public void handleMessage(String message);
    }
}