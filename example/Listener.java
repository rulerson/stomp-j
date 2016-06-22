package example;

import pk.aamir.stompj.*;

import java.io.IOException;
import java.util.HashMap;

public class Listener {

    public static void main(String[] args) throws StompJException, IOException {
        // connect
        Connection con = new Connection("localhost", 61613, "admin", "password");
        con.connect();

        // add msg listener
        con.addMessageHandler("/topic/msg-exchange", new MessageHandler() {
            public void onMessage(Message msg) {
                String[] propNameS = msg.getPropertyNames();
                for (String propName : propNameS){
                    System.out.println(propName + ":" + msg.getProperty(propName));
                }
                System.out.println(msg.getContentAsString());
            }
        });

        // add error listener
        con.setErrorHandler(new ErrorHandler() {
            @Override
            public void onError(ErrorMessage errormessage) {
                System.out.println(errormessage.getContentAsString());
            }
        });

        // subscribe
        HashMap<String, String> optionalHeaders = new HashMap<>();
        optionalHeaders.put("selector", "target = 'mee'");

        con.subscribe("/topic/msg-exchange", optionalHeaders);

        System.in.read();

        // disconnect
        con.disconnect();
    }
}
