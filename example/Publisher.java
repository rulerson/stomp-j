package example;

import pk.aamir.stompj.Connection;
import pk.aamir.stompj.StompJException;

import java.util.HashMap;

public class Publisher {

    public static void main(String[] args) throws StompJException {
        // connection
        Connection con = new Connection("service.coolshare.pw", 61613, "user-test", "123");

        // connect
        HashMap<String, String> connectOptionalHeaders = new HashMap<>();
        connectOptionalHeaders.put("host", "vhost-test");
        con.connect(connectOptionalHeaders);

        // send
        con.send("Java test message, from sender1!", "/topic/send2.sub2");

        // disconnect
        con.disconnect();
    }
}
