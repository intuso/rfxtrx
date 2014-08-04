package com.rfxcom.rfxtrx;

import com.intuso.utilities.log.Log;
import com.intuso.utilities.log.LogLevel;
import com.intuso.utilities.log.writer.StdOutWriter;
import com.rfxcom.rfxtrx.message.Interface;
import com.rfxcom.rfxtrx.message.InterfaceResponse;
import com.rfxcom.rfxtrx.message.Lighting2;
import com.rfxcom.rfxtrx.message.Message;
import com.rfxcom.rfxtrx.message.MessageWrapper;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by IntelliJ IDEA.
 * User: tomc
 * Date: 24/04/12
 * Time: 16:55
 * To change this template use File | Settings | File Templates.
 */
public class RFXtrx {

    private CommPortIdentifier portId;

    /**
     * The serial port connection to the relay card
     */
    private SerialPort port;

    /**
     * Output stream for the serial port
     */
    private OutputStream out;

    /**
     * Wrapped input stream for the serial port
     */
    private InputStream in;

    private Thread readerThread;
    private Thread parserThread;

    private List<MessageListener> listeners;

    /**
     * log to use
     */
    private final Log log;

    public RFXtrx(Log log) {
        this.log = log;
        listeners = new ArrayList<MessageListener>();
    }

    public RFXtrx(CommPortIdentifier portId, Log log) {
        this(log);
        setPortId(portId);
    }

    public void addListener(MessageListener listener) {
        listeners.add(listener);
    }

    public void removeListener(MessageListener listener) {
        listeners.remove(listener);
    }

    public void setPortId(CommPortIdentifier portId) {
        this.portId = portId;
    }

    /**
     * Open a serial port
     */
    public final void openPort() {

        if(portId == null)
            return;

        // Open the port
        try {
            log.d("Attempting to open serial port " + portId.getName());
            this.port = (SerialPort)portId.open(RFXtrx.class.getName(), 38400);
            port.setInputBufferSize(0);
            port.setOutputBufferSize(0);
            this.port.setSerialPortParams(38400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            in = this.port.getInputStream();
            out = this.port.getOutputStream();
            parserThread = new Thread(new Parser());
            readerThread = new Thread(new Reader());
            parserThread.start();
            readerThread.start();
            sendMessage(new Interface(Interface.Command.Reset));
            try {
                Thread.sleep(100); // min 50ms, max 9 seconds
            } catch(InterruptedException e) {}
            sendMessage(new Interface(Interface.Command.GetStatus));
            log.d("Successfully opened serial port");
        } catch (PortInUseException e) {
            log.e("Serial port is already in use. Is the service already running elsewhere?");
            this.port = null;
        } catch (UnsupportedCommOperationException e) {
            log.e("Couldn't set serial port parameters");
            this.port = null;
        } catch (IOException e) {
            log.e("Couldn't open serial port", e);
            this.port = null;
        } /*catch(TooManyListenersException e) {
            log.e("Couldn't add listener to serial port");
            log.st(e);
            this.port = null;
        }*/
    }

    /**
     * Close the relay port
     */
    public final void closePort() {

        // close the IO streams
        try {
            if(out != null)
                out.close();
        } catch (IOException e) {
            // do nothing, closing down anyway
        }
        try {
            if(in != null)
                in.close();
        } catch (IOException e) {
            // do nothing, closing down anyway
        }

        // close the port
        if(port != null)
            port.close();

        port = null;
        in = null;
        out = null;

        parserThread.interrupt();
        try {
            parserThread.join();
        } catch(InterruptedException e) {
        }
        readerThread.interrupt();
        try {
            readerThread.join();
        } catch(InterruptedException e) {
        }
    }

    public void sendMessage(MessageWrapper messageWrapper) throws IOException {
        if(out == null)
            throw new IOException("Socket is not open");
        log.d("Sending message: " + messageWrapper.toString());
        messageWrapper.writeTo(out, (byte)0);
    }

    private void messageReceived(MessageWrapper messageWrapper) {
        for(MessageListener listener : listeners)
            listener.messageReceived(messageWrapper);
    }

    /**
     * Get a list of descriptions of potential serial ports
     * @return a list of descriptions of potential serial ports
     */
    public static List<CommPortIdentifier> listSuitablePorts(Log log) {

        List<CommPortIdentifier> suitablePorts = new ArrayList<CommPortIdentifier>();

        // Get a list of suitable ports;
        @SuppressWarnings("unchecked")
        java.util.Enumeration<CommPortIdentifier> commPorts = CommPortIdentifier.getPortIdentifiers();
        while(commPorts.hasMoreElements())
        {
            CommPortIdentifier commPortId = commPorts.nextElement();
            log.d("Found comm port " + commPortId.getName());
            if(commPortId.getPortType() != CommPortIdentifier.PORT_SERIAL)
                log.d("Comm port is not serial type");
            else if(commPortId.isCurrentlyOwned())
                log.d("Comm port is already owned");
            else
                suitablePorts.add(commPortId);
        }

        return suitablePorts;
    }

    private final LinkedBlockingDeque<byte[]> readData = new LinkedBlockingDeque<byte[]>();

    private class Reader implements Runnable {

        private final byte[] buffer = new byte[1024];
        private int len;

        @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    len = in.read(buffer);
                    if(len < 0)
                        break;
                    byte[] readBytes = new byte[len];
                    System.arraycopy(buffer, 0, readBytes, 0, len);
                    log.d("Read data from socket: " + Arrays.toString(readBytes));
                    readData.addLast(readBytes);
                } catch(IOException e) {
                    log.e("Failed to read data from serial port", e);
                    closePort();
                }
            }
        }
    }

    private class Parser implements Runnable {

        @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    int packetLength;
                    byte packetType, packetSubType, sequenceNumber;
                    byte[] packetData;
                    outer: while(true) {
                        packetLength = readBytes(1)[0];
                        if(packetLength < 0) {
                            log.e("Packet length was -ve, stream was closed");
                            break outer;
                        } else if(packetLength < 3) {
                            log.e("Packet length was " + packetLength + ". Should be at least 3!");
                            break outer;
                        } else
                            log.d("Read packet length as 0x" + Integer.toHexString(packetLength));
                        packetType = readBytes(1)[0];
                        packetSubType = readBytes(1)[0];
                        sequenceNumber = readBytes(1)[0];
                        packetData = readBytes(packetLength - 3);
                        messageReceived(new Message(packetType, packetSubType, packetData), sequenceNumber);
                    }
                } catch(InterruptedException e) {
                    log.e("Error reading from stream");
                }
            }
        }

        private byte[] readBytes(int numNeeded) throws InterruptedException {

            // declare result array and how many bytes have been read
            byte[] result = new byte[numNeeded];
            int readSoFar = 0;

            // while we need to read more
            while(readSoFar < numNeeded) {
                // get the next piece of data
                byte[] nextReadData = readData.takeFirst();

                // work out how much to copy and copy it
                int toCopy = Math.min(nextReadData.length, numNeeded - readSoFar);
                System.arraycopy(nextReadData, 0, result, readSoFar, toCopy);

                // increment how much we've read
                readSoFar += toCopy;

                // if we didn't read all from that line, put it back minus the data we already read
                if(toCopy < nextReadData.length) {
                    byte[] toPutBack = new byte[nextReadData.length - toCopy];
                    System.arraycopy(nextReadData, toCopy, toPutBack, 0, toPutBack.length);
                    readData.addFirst(toPutBack);
                }
            }
            return result;
        }

        private void messageReceived(Message message, byte sequenceNumber) {
            log.d("Message received: " + message.toString());
            MessageWrapper messageWrapper = null;
            switch(message.getPacketType()) {
                case(InterfaceResponse.PACKET_TYPE) :
                    messageWrapper = new InterfaceResponse(message);
                    break;
                case(Lighting2.PACKET_TYPE) :
                    messageWrapper = new Lighting2(message);
                    break;
                default:
                    log.d("Unknown packet type");
            }
            if(messageWrapper != null)
                RFXtrx.this.messageReceived(messageWrapper);
        }
    }

    /**
     * Created by IntelliJ IDEA.
     * User: tomc
     * Date: 24/04/12
     * Time: 18:34
     * To change this template use File | Settings | File Templates.
     */
    public static interface MessageListener {
        public void messageReceived(MessageWrapper messageWrapper);
    }

    public static void main(String[] args) throws IOException {
        Log log = new Log(new StdOutWriter(LogLevel.DEBUG));

        List<CommPortIdentifier> cpis = listSuitablePorts(log);
        for(CommPortIdentifier cpi : cpis)
            System.out.println(cpi.getName());

        CommPortIdentifier cpi = cpis.get(0);

        RFXtrx rfxtrx = new RFXtrx(log);
        rfxtrx.setPortId(cpi);

        rfxtrx.openPort();

        System.in.read();

        rfxtrx.closePort();
    }
}
