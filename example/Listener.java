package example;

import pk.aamir.stompj.*;

import java.io.IOException;
import java.util.HashMap;

public class Listener {

    public static void main(String[] args) throws StompJException, IOException {
        // connect
        Connection con = new Connection("service.coolshare.pw", 61613, "user-test", "123");

        // add msg listener
        con.setMessageHandler(new MessageHandler() {
            public void onMessage(Message msg) {
                System.out.println("message---------");
                String[] propNameS = msg.getPropertyNames();
                for (String propName : propNameS) {
                    System.out.println(propName + ":" + msg.getProperty(propName));
                }
                System.out.println(msg.getContentAsString());
            }
        });

        // add error listener
        con.setErrorHandler(new ErrorHandler() {
            @Override
            public void onError(ErrorMessage errormessage) {
                System.out.println(errormessage.getMessage());
            }
        });

        // connect
        HashMap<String, String> connectOptionalHeaders = new HashMap<>();
        connectOptionalHeaders.put("host", "vhost-test");
        con.connect(connectOptionalHeaders);

        // subscribe
        con.subscribe("/topic/*.sub2");

        System.in.read();

        // disconnect
        con.disconnect();
    }
}
