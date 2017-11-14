/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.Random;
import project2.Message.MessageType;

/**
 *
 * @author Jaymit Desai
 */
public class SendThread implements Runnable {

    private int vectorClock[];
    private InetAddress multicastAddr;
    private MulticastSocket multiSocket;
    private ProcessClass ownerProcess;
    private int processID;
    private int portNo;

    public SendThread(ProcessClass ownerP, int[] vc, String multiCastAddress, int portNo) {
        try {
            this.portNo = portNo;
            ownerProcess = ownerP;
            vectorClock = vc;
            multicastAddr = InetAddress.getByName(multiCastAddress);
            multiSocket = new MulticastSocket(portNo);
            processID = ownerProcess.getProcessID();
        } catch (IOException ex) {
            System.out.println("Sorry, could not create multicast connection: " + ex);
        }
    }

    public void run() {
        startSend();
    }

    public void startSend() {
        int max = 1;
        try {
            for (int i = 0; i < max; i++) {

                sendMsg(MessageType.TIMESTAMP);
            }
            sendMsg(MessageType.TIMESTAMP_FINAL);;
        } catch (InterruptedException ex) {
            System.out.println("Exception while sendThread running sleep : " + ex);
        } catch (NullPointerException ex) {
            System.out.println(" NPEException while sendThread sending packet : " + ex);
        } catch (IOException ex) {
            System.out.println("Exception while sendThread sending packet : " + ex);
        }

    }

    public void sendMsg(MessageType msg) throws InterruptedException, IOException, NullPointerException {
    Thread.sleep(new Random().nextInt(2) * 1000);
        ++vectorClock[processID];
        byte[] vcData = ownerProcess.convertObjecttoBytes(vectorClock);
        Message myMsg = new Message(processID, vectorClock[processID], vcData, msg);
        byte[] sendData = ownerProcess.convertObjecttoBytes(myMsg);
        DatagramPacket pkt = new DatagramPacket(sendData, sendData.length, multicastAddr, portNo);
        multiSocket.send(pkt);
    }


}
