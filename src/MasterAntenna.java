
import java.nio.channels.Channel;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class MasterAntenna {

private Map<String, List<Message>> storage = new ConcurrentHashMap<>();
    
    private final Connection connection;
    private final Channel channel;
    private final String masterExchange = "master_broadcast_exchange";
    private final String storageQueue = "master_storage_queue";



    public MasterAntenna(Connection connection) throws Exception {
            this.connection = connection;
            this.channel = connection.createChannel();
            
            // Fanout Exchange for broadcasting to all antennas
            this.channel.exchangeDeclare(masterExchange, "fanout");
            
            // Setup the queue to receive looped messages from antennas
            this.channel.queueDeclare(storageQueue, false, false, false, null);
            
            // Start listening for messages to store
            listenForLoopedMessages();
        }

    public String sendAll(){
        return "Message sent to all antennas";}

    public String storeMessagStringe(Message msg){
        return "Message stored in MasterAntenna: " + msg.getContent();}
    
    


    
}
