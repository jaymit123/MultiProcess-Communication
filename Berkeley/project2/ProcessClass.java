/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project2;

import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import project2.Message.MessageType;

/**
 *
 * @author Jaymit Desai
 */
public class ProcessClass implements Runnable {

    private static int counter;
    private InetAddress hostAddress;
    private boolean isCoordinator;
    private DatagramSocket socket;
    private int portNo;
    private LinkedHashMap<Integer, Integer> processInfo;
    private int processID;

    public int getProcessID() {
        return processID;
    }

    public void setProcessID(int processID) {
        this.processID = processID;
    }

    public LinkedHashMap<Integer, Integer> getProcessInfo() {
        return processInfo;
    }

    public void setProcessInfo(LinkedHashMap<Integer, Integer> processInfo) {
        this.processInfo = processInfo;
    }

    public ProcessClass() throws IOException {
        socket = new DatagramSocket();
        portNo = socket.getLocalPort();
        hostAddress = InetAddress.getLocalHost();
        counter = new Random().nextInt(20);

    }

    public ProcessClass(int portNo) {
        try {
            hostAddress = InetAddress.getLocalHost();
            this.portNo = portNo;
            socket = new DatagramSocket(portNo, hostAddress);
            counter = new Random().nextInt(20);
        } catch (SocketException ex) {
            System.out.println("Error while creating process:" + ex);
        } catch (UnknownHostException ex) {

        }
    }

    public void run() {
        if (isCoordinator) {
            initCoordinator();
            startBerkleyAsCoordinator();
        } else {
            initSlave();
            startBerkleyAsSlave();
        }
        System.out.println("Berkleys Algorithm Complete. \n\n");
        System.out.println("Non Ordered Multicasting Started");
        initVectorClock();

    }

    //-- COORDINATOR METHODS START--//
    public void initCoordinator() {
        try {
            System.out.print("Enter the number of additional processes communicating(minimum : 2): ");
            int max_process = 0;
            Scanner readIP = new Scanner(System.in);

            while (((max_process += readIP.nextInt())) < 2) {
                System.out.println("Number of additional processes should be 2 or more : ");
            }
            ++max_process;
            System.out.println("Please create " + (max_process - 1) + " processes of SlaveProcess ");

            processInfo = new LinkedHashMap<>();
            processInfo.put(portNo, 0);
            processID = 0; // Coordinator process ID is always 0
            for (int i = 0; i < max_process - 1; i++) {
                System.out.println("Waiting for " + (max_process - 1 - i) + " more processes to be started.");
                Object readData[] = recieveInformation(this.socket);
                DatagramPacket pkt = (DatagramPacket) readData[0];
                processInfo.put(pkt.getPort(), i + 1);
            }
            System.out.print("\n\n");
            this.setProcessInfo(processInfo);
            sendProcesstList(processInfo);
        } catch (IOException ex) {
            System.out.println("Sorry, could not recieve file from specified machine: " + ex);
        }
    }

    private void sendProcesstList(LinkedHashMap<Integer, Integer> pList) {

        byte[] sendList = convertObjecttoBytes(pList);
        Message m = new Message(processID, counter, sendList, MessageType.SEND_PROCESS_LIST);
        byte[] sendMsg = convertObjecttoBytes(m);
        Iterator<Entry<Integer, Integer>> i = pList.entrySet().iterator();
        System.out.println("Coordinator is Process " + processID + " who has time: " + counter);
        while (i.hasNext()) {
            try {
                int currentPort = i.next().getKey();
                if (currentPort != this.portNo) {
                    DatagramPacket pkt = new DatagramPacket(sendMsg, sendMsg.length, hostAddress, currentPort);
                    socket.send(pkt);
                }
            } catch (IOException ex) {
                System.out.println("Sorry, could not send file to process : " + ex);
            }
        }

    }

    private void startBerkleyAsCoordinator() {
        System.out.println("Berkleys Algorithm Started...");
        if (processInfo == null || processInfo.isEmpty()) {
            System.out.println("PortList is empty");
            return;
        }
        sendTime();
        calculateAverage();

    }

    private void sendTime() {
        Iterator<Entry<Integer, Integer>> i = processInfo.entrySet().iterator();
        System.out.println("Coordinator (Process " + processID + ") sending its time: " + counter + " to all processes.");
        while (i.hasNext()) {
            try {
                Message m = new Message(processID, counter, null, MessageType.DAEMON_COUNTER);
                byte[] sendMsg = convertObjecttoBytes(m);
                int recipientPort = i.next().getKey();
                if (recipientPort != this.portNo) {
                    DatagramPacket pkt = new DatagramPacket(sendMsg, sendMsg.length, hostAddress, recipientPort);
                    socket.send(pkt);
                }
            } catch (UnknownHostException ex) {
                System.out.println("Sorry, could not find specified machine: " + ex);
            } catch (IOException ex) {
                System.out.println("Sorry, could not send file to specified machine: " + ex);
            }
        }

    }

    private void calculateAverage() {
        try {
            int length = processInfo.size() - 1; // removed one account of its own counter
            int average = 0;
            LinkedHashMap<DatagramPacket, Message> processData = new LinkedHashMap<>(); //stores information of processes that responsed.
            for (int i = 0; i < length; i++) {
                Object[] msg = recieveInformation(this.socket);
                DatagramPacket pkt = (DatagramPacket) msg[0];
                Message m = (Message) msg[1];
                processData.put(pkt, m);
                System.out.println("Coordinator (Process " + processID + ") recieved time difference : " + m.getMessageString() + " from Process " + m.getProcessID());
                int msgDiff = Integer.valueOf(m.getMessageString());
                average += msgDiff;
            }

            average = average / (length + 1);
            int previous_time = counter;
            counter += average;
            System.out.println("Coordinator (Process " + processID + ") calculated Average of all time difference is: " + average + "");
            System.out.println("Coordinator (Process " + processID + ") Sending time changes to all processes.");
            System.out.println("Coordinator (Process " + processID + ") Current Time : " + previous_time + " ----> New Time : " + counter);
            sendTimeChanges(processData);
        } catch (IOException ex) {
            System.out.println("Sorry, could not recieve file from specified machine: " + ex);
        }
    }

    private void sendTimeChanges(LinkedHashMap<DatagramPacket, Message> chg) {
        Iterator<Entry<DatagramPacket, Message>> i = chg.entrySet().iterator();

        while (i.hasNext()) {
            try {
                Entry<DatagramPacket, Message> entry = i.next();
                DatagramPacket currentPkt = (DatagramPacket) entry.getKey();
                Message currentMsg = (Message) entry.getValue();
                int time_change = counter - (currentMsg.getCounter());
                int port = entry.getKey().getPort();
                Message m = new Message(processID, time_change, null, MessageType.CHANGE_TIME);
                byte[] sendMsg = convertObjecttoBytes(m);
                DatagramPacket pkt = new DatagramPacket(sendMsg, sendMsg.length, hostAddress, port);
                System.out.println("Coordinator (Process " + processID + ") sending time change of " + time_change + " to Process " + currentMsg.getProcessID());
                socket.send(pkt);
            } catch (UnknownHostException ex) {
                System.out.println("Sorry, could not find specified machine: " + ex);
            } catch (IOException ex) {
                System.out.println("Sorry, could not send file to specified machine: " + ex);
            }
        }
    }

    //-- COORDINATOR METHODS END--//
    //-- SLAVE METHODS START--//
    public void initSlave() {
        try {
            Message m = new Message(processID, counter, null, MessageType.TEXT);  //deal with MessageType is this enum needed
            byte[] data = convertObjecttoBytes(m);
            DatagramPacket pkt = new DatagramPacket(data, data.length, hostAddress, 5454);
            socket.send(pkt);
            Object[] readData = recieveInformation(this.socket);
            m = (Message) readData[1];
            this.setProcessInfo((LinkedHashMap<Integer, Integer>) (readObject(m.getMessage())));
            processID = this.getProcessInfo().get(portNo);
            System.out.println("Process " + processID + "  has time: " + counter);
        } catch (IOException ex) {
            System.out.println("Sorry, could not send file to process : " + ex);
        }
    }

    private void startBerkleyAsSlave() {
        try {
            Object[] requests = recieveInformation(this.socket);
            System.out.println("Berkleys Algorithm Started...");
            DatagramPacket Pkt = (DatagramPacket) requests[0];
            Message requestMsg = (Message) requests[1];
            System.out.println("Process " + processID + " recieved coordinator's time: " + requestMsg.getCounter());
            byte[] data;
            if (requestMsg.getType().equals(MessageType.DAEMON_COUNTER)) {
                int timedif = this.counter - requestMsg.getCounter();
                data = String.valueOf(timedif).getBytes();  //time diffrence
                Message responseMsg = new Message(processID, counter, data, MessageType.TIME_DIFF);
                byte[] sendMsgBytes = convertObjecttoBytes(responseMsg);
                Pkt = new DatagramPacket(sendMsgBytes, sendMsgBytes.length, hostAddress, Pkt.getPort());
                System.out.println("Process " + processID + " sending time difference : " + timedif + " to Coordinator");
                socket.send(Pkt);
                changeTime();
            }

        } catch (IOException ex) {
            System.out.println("Sorry, could not recieve file from specified machine: " + ex);
        }
    }

    private void changeTime() {
        try {
            Object[] requests = recieveInformation(this.socket);
            DatagramPacket Pkt = (DatagramPacket) requests[0];
            Message requestMsg = (Message) requests[1];
            if (requestMsg.getType().equals(MessageType.CHANGE_TIME)) {
                int previous_time = counter;
                counter += requestMsg.getCounter();
                System.out.println("Recieved request from Coordinator to change time by " + requestMsg.getCounter());
                System.out.println("Process " + processID + " Current Time : " + previous_time + " ----> New Time : " + counter);

            }

        } catch (IOException ex) {
            System.out.println("Sorry, could not recieve file from specified machine: " + ex);
        }
    }

    //-- SLAVE METHODS END--//
    //--SHARED COORD_SLAVE_METHODS//
    public Object[] recieveInformation(DatagramSocket socket) throws IOException {
        byte[] data = new byte[1024];
        DatagramPacket Pkt = new DatagramPacket(data, data.length);
        socket.receive(Pkt);
        byte[] storeData = getPktData(data, Pkt.getLength());
        Message requestMsg = (Message) readObject(storeData);
        return new Object[]{Pkt, requestMsg};
    }

    private byte[] getPktData(byte[] data, int length) {
        byte[] storeData = new byte[length];
        for (int i = 0; i < length; i++) {
            storeData[i] = data[i];
        }
        return storeData;
    }

    public Object readObject(byte[] byteMsg) {
        Object response = null;
        ObjectInputStream ois = null;
        try {
            ByteArrayInputStream ais = new ByteArrayInputStream(byteMsg);
            ois = new ObjectInputStream(ais);
            response = ois.readObject();
            ois.close();
            ois = null;
        } catch (ClassNotFoundException ex) {
            System.out.println("Sorry Process couldnt read message:" + ex);
            closeObjectInputStream(ois);
        } catch (IOException ex) {
            System.out.println("Sorry Process couldnt read message:" + ex);
            closeObjectInputStream(ois);
        }

        return response;
    }

    private void closeObjectInputStream(ObjectInputStream obj) {
        if (obj != null) {
            try {
                obj.close();
                obj = null;
            } catch (IOException ex) {
                System.out.println("Sorry could not close message: " + ex);
            }

        }
    }

    public byte[] convertObjecttoBytes(Object m) {
        byte[] msgInBytes = null;
        ObjectOutputStream os = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            os = new ObjectOutputStream(bos);
            os.writeObject(m);
            msgInBytes = bos.toByteArray();
            os.close();
            os = null;
        } catch (IOException ex) {
            System.out.println("Sorry Process couldnt convert message for sending:" + ex);
            closeObjectOutputStream(os);
        }
        return msgInBytes;
    }

    private void closeObjectOutputStream(ObjectOutputStream obj) {
        if (obj != null) {
            try {
                obj.close();
                obj = null;
            } catch (IOException ex) {
                System.out.println("Sorry could not close message: " + ex);
            }

        }
    }
//--SHARED CODE ENDS-//
//--VECTOR CLOCK START--//

    private void initVectorClock() {
        int[] vc = new int[processInfo.size()];
        //init all clocks to time set by berkleys algorithm
        for (int i = 0; i < vc.length; i++) {
            vc[i] = counter;
        }
        String multicastAddr = "239.193.129.14";
        int multiCPort = 6789;
        new Thread(new RecieveThread(this, vc, multicastAddr, multiCPort)).start();
        new Thread(new SendThread(this, vc, multicastAddr, multiCPort)).start();

    }

    //-VECTOR CLOCK END--//   
    public int getPort() {
        return portNo;
    }

    public boolean isIsCoordinator() {
        return isCoordinator;
    }

    public void setIsCoordinator(boolean isCoordinator) {
        this.isCoordinator = isCoordinator;
    }

    public Thread getRecieveMsg() {
        return recieveMsg;
    }

    public void setRecieveMsg(Thread recieveMsg) {
        this.recieveMsg = recieveMsg;
    }
    private Thread recieveMsg;

    public Integer getCounter() {
        return counter;
    }

    public void setCounter(Integer Counter) {
        this.counter = Counter;
    }

}
