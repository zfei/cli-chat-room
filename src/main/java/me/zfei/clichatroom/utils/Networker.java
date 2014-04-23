package me.zfei.clichatroom.utils;

import me.zfei.clichatroom.ChatRoom;
import me.zfei.clichatroom.models.Member;
import me.zfei.clichatroom.models.Message;
import me.zfei.clichatroom.models.Sequencer;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zfei on 4/2/14.
 */
public class Networker {

    private Member owner;
    private TimeStamp timestamp;

    private Map<String, ArrayList<Integer>> ackWaitList;

    public Networker(Member owner, TimeStamp timestamp) {
        this.ackWaitList = new ConcurrentHashMap<String, ArrayList<Integer>>();
        // start ack daemon
        ackDaemon();

        this.owner = owner;
        this.timestamp = timestamp;
    }

    private Logger logger = LoggerFactory.getLogger(Networker.class);

    public int getPort() {
        return this.owner.getPort();
    }

    public TimeStamp getTimestamp() {
        return timestamp;
    }

    public int getIdentifier() {
        return this.owner.getIdentifier();
    }

    private boolean packetWillDrop() {
        Random rand = new Random();
        return rand.nextDouble() < ChatRoom.DROP_RATE;
    }

    private void unicastSend(int senderId, InetAddress receiverIp, int receiverPort, int receiverId, String msg) throws IOException {
        if (packetWillDrop()) {
            logger.debug(String.format("OOPS, MEMBER %d DROPPED A PACKET SENT TO %d\n", senderId, receiverId));
            return;
        }

        DatagramSocket outgoingSocket = new DatagramSocket();
//        InetAddress IPAddress = InetAddress.getByName(receiverIp);
        InetAddress IPAddress = receiverIp;
        byte[] sendData;
        sendData = msg.getBytes("UTF-8");
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, receiverPort);
        outgoingSocket.send(sendPacket);
        outgoingSocket.close();
    }

    public void addToAckWaitList(String msg, Integer receiverId) {
        synchronized (ackWaitList) {
            if (!this.ackWaitList.containsKey(msg)) {
                this.ackWaitList.put(msg, new ArrayList<Integer>());
            }
            this.ackWaitList.get(msg).add(receiverId);
        }
    }

    public void removeFromAckWaitList(String msg, Integer receiverId) {
        synchronized (ackWaitList) {
            this.ackWaitList.get(msg).remove((Object) receiverId);
        }
    }

    public void processAck(Message msgObj) {
        removeFromAckWaitList(msgObj.getMessage(), msgObj.getSenderId());
    }

    public void ackDaemon() {
        Thread ackDaemonThread = new Thread() {

            @Override
            public void run() {
                while (true) {
                    try {
                        sleep(200);

                        synchronized (ackWaitList) {
                            Iterator<String> msgIt = ackWaitList.keySet().iterator();
                            while (msgIt.hasNext()) {
                                String msg = msgIt.next();
                                for (int receiverId : ackWaitList.get(msg)) {
                                    int receiverPort = receiverId + ChatRoom.PORT_BASE;
                                    logger.debug(getIdentifier() + " DIDN'T RECEIVE ACK FROM " + receiverId + ", RESENDING");
                                    unicastSend(getIdentifier(), owner.getMemberIp(receiverId), receiverPort, receiverId, msg);
                                }
                            }
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        };

        ackDaemonThread.start();
    }

    public void multicast(ArrayList<Member> members, Member sender, String message) {
        int senderId = sender.getIdentifier();
        String serializedMessage;
        switch (ChatRoom.MULTICAST_TYPE) {
            case BASIC_MULTICAST:
                synchronized (this.timestamp) {
                    // increment timestamp
                    this.timestamp.increment();

                    serializedMessage = serializeToJson(message, senderId, this.timestamp.toString());
                }
                basicMulticast(members, sender, serializedMessage, false);
                break;
            case RELIABLE_MULTICAST:
                synchronized (this.timestamp) {
                    // increment timestamp
                    this.timestamp.increment();

                    serializedMessage = serializeToJson(message, senderId, this.timestamp.toString());
                }
                basicMulticast(members, sender, serializedMessage, true);
                break;
            case RELIABLE_CAUSAL_ORDERING:
                synchronized (this.timestamp) {
                    VectorTimeStamp incrementedStamp = new VectorTimeStamp((VectorTimeStamp) this.timestamp);
                    incrementedStamp.increment();

                    serializedMessage = serializeToJson(message, senderId, incrementedStamp.toString());
                }
                basicMulticast(members, sender, serializedMessage, true);
                break;
            case RELIABLE_TOTAL_ORDERING:
                serializedMessage = serializeToJson(message, senderId, String.valueOf(UUID.randomUUID().getMostSignificantBits()));
                basicMulticast(members, sender, serializedMessage, true);
                break;
            default:
                break;
        }
    }

    private void initiateUnicastSend(final int senderId, final InetAddress receiverIp, final int receiverPort, final int receiverId, final String msg) {
        Thread senderThread = new Thread() {

            @Override
            public void run() {
                try {
                    sleep(new Random().nextInt(ChatRoom.DELAY_DEVIATION * 2) - ChatRoom.DELAY_DEVIATION + ChatRoom.MEAN_DELAY);
                    unicastSend(senderId, receiverIp, receiverPort, receiverId, msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        };

//        System.out.println(getIdentifier() + " ADDING TO ACK LIST: " + msg);
        addToAckWaitList(msg, receiverId);

        senderThread.start();
    }

    public void basicMulticast(ArrayList<Member> members, Member sender, String serializedMessage, boolean includeMyself) {
        if (sender instanceof Sequencer) {
            logger.info("SEQUENCER SENDS ORDER\n");
        } else {
            logger.debug(String.format("MEMBER %d SENDS MESSAGE AT %s\n", sender.getIdentifier(), this.timestamp.toString()));
        }

        for (Member m : members) {
            if (!includeMyself && m == sender) {
                continue;
            }

            initiateUnicastSend(sender.getIdentifier(), m.getIp(), m.getPort(), m.getIdentifier(), serializedMessage);
        }
    }

    private String serializeToJson(String message, int senderId, String tsString) {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("message", message);
            jsonObj.put("timestamp", tsString);
            jsonObj.put("sender", senderId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObj.toString();
    }

    public void sendAck(InetAddress toIp, DatagramPacket packet) {
        String serializedMessage = Message.decodePacket(packet);
        Message msg = new Message(serializedMessage);
        int senderId = msg.getSenderId();

        JSONObject ackJsonObj = new JSONObject();
        try {
            ackJsonObj.put("ack", true);
            ackJsonObj.put("sender", this.getIdentifier());
            ackJsonObj.put("message", serializedMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            unicastSend(this.getIdentifier(), toIp, ChatRoom.PORT_BASE + senderId, senderId, ackJsonObj.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DatagramPacket unicastReceive(DatagramSocket serverSocket) {
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            serverSocket.receive(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!new Message(receivePacket).isAck())
            sendAck(receivePacket.getAddress(), receivePacket);

        return receivePacket;
    }


}
