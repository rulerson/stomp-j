package example;

import pk.aamir.stompj.Connection;
import pk.aamir.stompj.DefaultMessage;
import pk.aamir.stompj.StompJException;

import java.util.HashMap;

public class Publisher {

    public static void main(String[] args) throws StompJException {
        // connect
        Connection con = new Connection("localhost", 61613, "admin", "password");
        con.connect();

        // send
        /*
        DefaultMessage msg = new DefaultMessage();
        msg.setProperty("type", "text/plain");
        msg.setProperty("target", "mee");
        msg.setContent("Java test message!");

        con.send(msg, "/topic/msg-exchange");
        */
        HashMap<String, String> optionalHeaders = new HashMap<>();
        optionalHeaders.put("target", "mee1");

        con.send("Java test message!", "/topic/msg-exchange", optionalHeaders);

        // disconnect
        con.disconnect();
    }
}
