package com.rfxcom.rfxtrx;

import com.intuso.utils.log.Log;
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
import java.util.List;

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
    
    private Thread readerThread = null;

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
            readerThread = new Thread(new Reader());
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
            log.e("Couldn't open serial port");
            log.st(e);
            this.port = null;
        }
    }

    /**
     * Close the relay port
     */
    public final void closePort() {

        readerThread.interrupt();
        try {
            readerThread.join();
        } catch(InterruptedException e) {
            // do nothing, closing down anyway
        }
        readerThread = null;
        
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
    public static List<CommPortIdentifier> listSuitablePorts() {

        List<CommPortIdentifier> suitable_ports = new ArrayList<CommPortIdentifier>();

        // Get a list of suitable ports;
        @SuppressWarnings("unchecked")
        java.util.Enumeration<CommPortIdentifier> comm_ports = CommPortIdentifier.getPortIdentifiers();
        while(comm_ports.hasMoreElements())
        {
            CommPortIdentifier comm_port_id = comm_ports.nextElement();
            if(comm_port_id.getPortType() == CommPortIdentifier.PORT_SERIAL && !comm_port_id.isCurrentlyOwned())
                suitable_ports.add(comm_port_id);
        }

        return suitable_ports;
    }
    
    private class Reader implements Runnable {
        @Override
        public void run() {
            try {
                int packetLength;
                byte packetType, packetSubType, sequenceNumber;
                byte[] packetData;
                outer: while(true) {
                    try {
                        waitForAvailableBytes(1);
                    } catch(InterruptedException e) {
                        log.e("Interrupted waiting for message to read");
                        break outer;
                    }
                    packetLength = in.read();
                    if(packetLength < 0) {
                        log.e("Packet length was -ve, stream was closed");
                        break outer;
                    } else if(packetLength < 3) {
                        log.e("Packet length was < 3. Should be at least 3!");
                        break outer;
                    } else
                        log.d("Read packet length as 0x" + Integer.toHexString(packetLength));
                    packetType = (byte)in.read();
                    packetSubType = (byte)in.read();
                    sequenceNumber = (byte)in.read();
                    packetData = new byte[packetLength - 3];
                    try {
                        waitForAvailableBytes(packetData.length);
                    } catch(InterruptedException e) {
                        log.e("Interrupted waiting for packet data");
                        break outer;
                    }
                    if(in.read(packetData) != packetData.length) {
                        log.e("Did not read enough data");
                        break outer;
                    }
                    messageReceived(new Message(packetType, packetSubType, packetData), sequenceNumber);
                }
            } catch(IOException e) {
                log.e("Error reading from stream");
            }
            closePort();
        }

        private void waitForAvailableBytes(int numNeeded) throws IOException, InterruptedException {
            while(in.available() < numNeeded) {
                Thread.sleep(1);
            }
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

    public static void main(String[] args) {
        for(CommPortIdentifier cpi : listSuitablePorts())
            System.out.println(cpi.getName());
    }
}
