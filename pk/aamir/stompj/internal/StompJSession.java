// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   StompJSession.java

package pk.aamir.stompj.internal;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import pk.aamir.stompj.*;

// Referenced classes of package pk.aamir.stompj.internal:
//            FrameReceiver

public class StompJSession {

	public StompJSession(String host, int port, String userid, String password,
			Connection con) {
		this.host = host;
		this.port = port;
		this.userid = userid;
		this.password = password;
		connection = con;
		autoAckMap = new HashMap();
	}

	public ErrorMessage connect(HashMap<String, String> optionalHeaders) throws StompJException {
		try {
			socket = new Socket(host, port);
			input = new BufferedInputStream(socket.getInputStream());
			output = new BufferedOutputStream(socket.getOutputStream());
			output.write(createCONNECTFrame(userid, password, optionalHeaders));
			output.flush();
			frameReceiver = new FrameReceiver(this, input);
			ErrorMessage errorMsg = frameReceiver.processFirstResponse();
			if (errorMsg == null) {
                frameReceiver.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        //System.out.println("uncaughtException");
                    }
                });
				frameReceiver.start();
			} else {
				disconnect(optionalHeaders);
				return errorMsg;
			}
		} catch (UnknownHostException e) {
			disconnect(optionalHeaders);
			throw new StompJException(e.getMessage(), e);
		} catch (IOException e) {
			disconnect(optionalHeaders);
			throw new StompJException(e.getMessage(), e);
		}
		return null;
	}

	public void disconnect(HashMap<String, String> optionalHeaders) {
		sendFrame(createDISCONNECTFrame(optionalHeaders));
		frameReceiver.interrupt();
        try {
			input.close();
			output.close();
			socket.close();
		} catch (IOException ioexception) {
		}
	}

	public boolean isConnected() {
		if (socket == null)
			return false;
		else
			return !socket.isClosed() || !socket.isConnected();
	}

	public void subscribe(String destination, boolean autoAck, HashMap<String, String> optionalHeaders) {
		sendFrame(createSUBSCRIBEFrame(destination, autoAck, optionalHeaders));
		autoAckMap.put(destination, Boolean.valueOf(autoAck));
	}

	public void unsubscribe(String destination, HashMap<String, String> optionalHeaders) {
		sendFrame(createUNSUBSCRIBEFrame(destination, optionalHeaders));
	}

	public String getSessionId() {
		return sessionId;
	}

	private synchronized void sendFrame(byte frame[])
			throws StompJRuntimeException {
		if (!isConnected())
			throw new StompJRuntimeException("Not connected to the server");
		try {
			output.write(frame);
			output.flush();
		} catch (IOException e) {
			throw new StompJRuntimeException(e.getMessage(), e);
		}
	}

	void sendAckIfNeeded(Message msg) {
		if (!((Boolean) autoAckMap.get(msg.getDestination())).booleanValue())
			sendFrame(createACKFrame(msg.getMessageId()));	// always auto ack
	}

	public void send(Message msg, String destination) {
		sendFrame(createSENDFrame(msg, destination));
	}

	private byte[] createCONNECTFrame(String userid, String password, HashMap<String, String> optionalHeaders)
			throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(prepareBytes("CONNECT"));
		os.write('\n');
		os.write(prepareBytes((new StringBuilder("login: ")).append(userid)
				.toString()));
		os.write('\n');
        if (optionalHeaders != null){
            for (String optkey : optionalHeaders.keySet()){
                os.write(prepareProperty(optkey, optionalHeaders.get(optkey)));
                os.write('\n');
            }
        }
		os.write(prepareBytes((new StringBuilder("passcode:")).append(password)
				.toString()));
		os.write('\n');
		os.write('\n');
		os.write(0);
		return os.toByteArray();
	}

	private byte[] createSENDFrame(Message msg, String destination) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(prepareBytes("SEND"));
			os.write('\n');
			os.write(prepareProperty("destination", destination));
			os.write('\n');
			String propNames[] = msg.getPropertyNames();
			String as[];
			int j = (as = propNames).length;
			for (int i = 0; i < j; i++) {
				String p = as[i];
				os.write(prepareProperty(p, msg.getProperty(p)));
				os.write('\n');
			}

			os.write('\n');
			os.write(msg.getContentAsBytes());
			os.write(0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return os.toByteArray();
	}

	private byte[] createSUBSCRIBEFrame(String destination, boolean autoAck, HashMap<String, String> optionalHeaders) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(prepareBytes("SUBSCRIBE"));
			os.write('\n');
			os.write(prepareProperty("destination", destination));
			os.write('\n');
			os.write(prepareProperty("ack", autoAck ? "auto" : "client"));
			os.write('\n');
            if (optionalHeaders != null){
                for (String optkey : optionalHeaders.keySet()){
                    os.write(prepareProperty(optkey, optionalHeaders.get(optkey)));
                    os.write('\n');
                }
            }
			os.write('\n');
			os.write(0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return os.toByteArray();
	}

	private byte[] createUNSUBSCRIBEFrame(String destination, HashMap<String, String> optionalHeaders) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(prepareBytes("UNSUBSCRIBE"));
			os.write('\n');
			os.write(prepareProperty("destination", destination));
			os.write('\n');
            if (optionalHeaders != null){
                for (String optkey : optionalHeaders.keySet()){
                    os.write(prepareProperty(optkey, optionalHeaders.get(optkey)));
                    os.write('\n');
                }
            }
			os.write('\n');
			os.write(0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return os.toByteArray();
	}

	private byte[] createACKFrame(String msgId) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(prepareBytes("ACK"));
			os.write('\n');
			os.write(prepareProperty("message-id", msgId));
			os.write('\n');
			os.write('\n');
			os.write(0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return os.toByteArray();
	}

	private byte[] createDISCONNECTFrame(HashMap<String, String> optionalHeaders) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(prepareBytes("DISCONNECT"));
			os.write('\n');
            if (optionalHeaders != null){
                for (String optkey : optionalHeaders.keySet()){
                    os.write(prepareProperty(optkey, optionalHeaders.get(optkey)));
                    os.write('\n');
                }
            }
			os.write('\n');
			os.write(0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return os.toByteArray();
	}

	private byte[] prepareBytes(String s) {
		try {
			return s.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private byte[] prepareProperty(String propName, String prop) {
		return prepareBytes((new StringBuilder(String.valueOf(propName)))
				.append(":").append(prop).toString());
	}

	Connection getConnection() {
		return connection;
	}

	private String host;
	private int port;
	private String userid;
	private String password;
	private String sessionId;
	private Socket socket;
	private BufferedInputStream input;
	private BufferedOutputStream output;
	private Connection connection;
	private HashMap autoAckMap;
	private FrameReceiver frameReceiver;
}
