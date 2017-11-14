/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project2;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import static project2.Message.MessageType;

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
    private ArrayList<Message> requestQueue;
    private final String fileName = "Counter.property";
    private String filePath;
    private MessageType LOCK;
    private final int CoordinatorPort = 5454;

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
            initFileAccess();
            initCoordinator();
            startSystemAsCoordinator();
        } else {
            initSlave();
            startSystemAsSlave();
        }

    }

    //-- COORDINATOR METHODS START--//
    public void initFileAccess() {
        Properties fileProp;
        System.out.print("Please enter complete path to the file \""+fileName+"\":  ");
        boolean isFileRight = false;
        while (!isFileRight) {
            try {
                String fileLocn = new Scanner(System.in).nextLine();
                File f = new File(fileLocn);
                fileProp = new Properties();
                fileProp.load(new FileInputStream(f));
                if (!f.isFile() && f.getName().equals(fileName)) {
                    System.out.println("File not found: Please correctly enter the path to " + fileName);

                } else if (!fileProp.containsKey("COUNTER")) {
                    System.out.println("No variable COUNTER in specified file. please add the line \'COUNTER = 0\' to the file.");
                } else {
                    filePath = fileLocn;
                    isFileRight = true;
                }
            } catch (IOException ex) {
                System.out.println("Could not check file: Please correctly enter the path to " + fileName);
            }

        }

    }

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
            processInfo.put(0, portNo);
            processID = 0; // Coordinator process ID is always 0
            for (int i = 0; i < max_process - 1; i++) {
                System.out.println("Waiting for " + (max_process - 1 - i) + " more processes to be started.");
                Object readData[] = recieveInformation(this.socket);
                DatagramPacket pkt = (DatagramPacket) readData[0];
                processInfo.put(i + 1, pkt.getPort());
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
                int currentPort = i.next().getValue();
                if (currentPort != this.portNo) {
                    DatagramPacket pkt = new DatagramPacket(sendMsg, sendMsg.length, hostAddress, currentPort);
                    socket.send(pkt);
                }
            } catch (IOException ex) {
                System.out.println("Sorry, could not send file to process : " + ex);
            }
        }

    }

    private void startSystemAsCoordinator() {
        if (processInfo == null || processInfo.isEmpty()) {
            System.out.println("PortList is empty");
            return;
        }

        int max_inmsg = (processInfo.size() - 1) * 2;
        requestQueue = new ArrayList<>();
        LOCK = MessageType.UNLOCKED;
        try {
            for (int i = 0; i < max_inmsg; i++) {
                Object[] incomingMsg = recieveInformation(this.socket);
                Message cm = (Message) incomingMsg[1];

                switch (cm.getType()) {
                    case REQUEST:
                        System.out.println("Request Message Recieved from Process " + cm.getProcessID());
                        handleRequest(cm);

                        break;

                    case RELEASE:
                        System.out.println("Release Message Recieved from Process " + cm.getProcessID());
                        handleRelease(cm);
                        break;
                }
                Thread.sleep(2000);
            }
        } catch (IOException ex) {
            System.out.println("Sorry, coordinator could not accept incoming requests: " + ex);
        } catch (InterruptedException ex) {
            System.out.println("Sorry, coordinator could not accept incoming requests: " + ex);
        }
    }

    private void handleRequest(Message m) throws IOException {
        if (LOCK == MessageType.UNLOCKED) {
            System.out.println("Granting Lock of file to Process " + m.getProcessID());
            sendAcquireMsg(m);
        } else if (LOCK == MessageType.LOCKED) {
            System.out.println("File is currently locked. Adding Process " + m.getProcessID() + "'s request in queue");
            requestQueue.add(m);
        }
    }

    private void sendAcquireMsg(Message m) throws IOException {
        Message sendMsg = new Message(processID, counter, filePath.getBytes(), MessageType.ACQUIRE);
        byte sendData[] = convertObjecttoBytes(sendMsg);
        int processPort = processInfo.get(m.getProcessID());
        DatagramPacket pkt = new DatagramPacket(sendData, sendData.length, hostAddress, processPort);
        socket.send(pkt);
        LOCK = MessageType.LOCKED;
    }

    private void handleRelease(Message m) throws IOException {
        if (LOCK == MessageType.LOCKED) {
            System.out.println("Checking queue for pending requests.");
            if (requestQueue.size() > 0) {

                Message firstInQueue = requestQueue.remove(0);
                System.out.println("Granting Lock of file to Process " + firstInQueue.getProcessID());
                sendAcquireMsg(firstInQueue);
            } else {
                System.out.println("No Process in Queue. File State set to Unlocked");
                LOCK = MessageType.UNLOCKED;
            }
        }
    }

    //-- COORDINATOR METHODS END--//
    //-- SLAVE METHODS START--//
    public void initSlave() {
        try {
            Message m = new Message(processID, counter, null, MessageType.TEXT);  //deal with MessageType is this enum needed
            byte[] data = convertObjecttoBytes(m);
            DatagramPacket pkt = new DatagramPacket(data, data.length, hostAddress, CoordinatorPort);
            socket.send(pkt);
            Object[] readData = recieveInformation(this.socket);
            m = (Message) readData[1];
            this.setProcessInfo((LinkedHashMap<Integer, Integer>) (readObject(m.getMessage())));
            Iterator<Entry<Integer, Integer>> i = this.getProcessInfo().entrySet().iterator();
            while (i.hasNext()) {
                Entry<Integer, Integer> entry = i.next();
                if (entry.getValue() == portNo) {
                    processID = entry.getKey();
                    break;
                }
            }
        } catch (IOException ex) {
            System.out.println("Sorry, could not send file to process : " + ex);
        }
    }

    private void startSystemAsSlave() {
        try {
            Message m = new Message(processID, counter, fileName.getBytes(), MessageType.REQUEST);
            byte[] data = convertObjecttoBytes(m);
            DatagramPacket pkt = new DatagramPacket(data, data.length, hostAddress, CoordinatorPort);
            socket.send(pkt);
            Object[] requests = recieveInformation(this.socket);
            Message recvMsg = (Message) requests[1];
            modifyFile(recvMsg);

        } catch (IOException ex) {
            System.out.println("Sorry, could not recieve file from specified machine: " + ex);
        }
    }

    private void modifyFile(Message m) {
        Properties fileProps = null;
        if (m.getType() == MessageType.ACQUIRE) {
            System.out.println("FILE LOCK ACQUIRED by Process " + processID);

            try {
                Thread.sleep(2000);
                String fileLocn = m.getMessageString();
                File f = new File(fileLocn);
                fileProps = new Properties();
                fileProps.load(new FileInputStream(fileLocn));
                if (!f.isFile() && f.getName().equals(fileName)) {
                    throw new IOException();

                } else if (!fileProps.containsKey("COUNTER")) {
                    throw new IOException();
                }
                System.out.println("Reading Counter Value.......");
                int counterValue = Integer.parseInt(fileProps.getProperty("COUNTER"));

                Thread.sleep(2000);
                System.out.println("Counter Value : " + counterValue);
                Thread.sleep(2000);
                System.out.println("Writing / Updating counter value by 1.");
                counterValue++;
                fileProps.setProperty("COUNTER", String.valueOf(counterValue));
                fileProps.store(new FileOutputStream(fileLocn), "");

                Thread.sleep(1000);
                System.out.println("Reading new value of Counter......");
                counterValue = Integer.parseInt(fileProps.getProperty("COUNTER"));
                System.out.println("Counter Value : " + counterValue);
                Thread.sleep(2000);
                sendFileRelease();
            } catch(NumberFormatException ex){
                System.out.println("Process could not access/find file.");
                 sendFileRelease();
            }catch (IOException ex) {
                System.out.println("Process could not access/find file.");
                sendFileRelease();
            } catch (InterruptedException ex) {
                System.out.println("Process could not access/find file.");
            }
        }
    }

    private void sendFileRelease() {
        try {
            System.out.println("RELEASED FILE LOCK.");
            Message sendRelease = new Message(processID, counter, null, MessageType.RELEASE);
            byte[] d = convertObjecttoBytes(sendRelease);
            DatagramPacket p = new DatagramPacket(d, d.length, hostAddress, CoordinatorPort);
            socket.send(p);
        } catch (IOException ex) {
            System.out.println("Sorry could not send message to coodinator.");
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
