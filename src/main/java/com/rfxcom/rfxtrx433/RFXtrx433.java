package com.rfxcom.rfxtrx433;

import com.housemate.utils.log.Log;
import com.rfxcom.rfxtrx433.message.Message;
import com.rfxcom.rfxtrx433.message.MessageListener;
import com.rfxcom.rfxtrx433.message.MessageWrapper;
import com.rfxcom.rfxtrx433.message.transceiver.Lighting2;
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
public class RFXtrx433 {

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

    private List<MessageListener<MessageWrapper>> listeners;

    /**
     * log to use
     */
    private final Log log;

    public RFXtrx433(Log log) {
       this.log = log;
        listeners = new ArrayList<MessageListener<MessageWrapper>>();
    }

    public void addListener(MessageListener<MessageWrapper> listener) {
        listeners.add(listener);
    }

    public void removeListener(MessageListener<MessageWrapper> listener) {
        listeners.remove(listener);
    }

    /**
     * Find and open the serial port for the card
     */
    private final void findAndOpenPort() {

        long delay = 1000;

        while(true) {
            List<CommPortIdentifier> suitable_ports = getSuitablePorts();
            log.d("Found " + suitable_ports.size() + " suitable port(s)");

            // check num suitable ports
            if(suitable_ports.size() == 0)
                log.e("There are no suitable ports");
            else if(suitable_ports.size() > 1)
                log.e("There are too many suitable ports");
            else
                openPort(suitable_ports.get(0));

            try {
                Thread.sleep(delay);
                delay *= 2;
            } catch(InterruptedException e) {
                log.e("Interrupted waiting for suitable port. Stopping");
                return;
            }
        }
    }

    /**
     * Get a list of descriptions of potential serial ports
     * @return a list of descriptions of potential serial ports
     */
    private final List<CommPortIdentifier> getSuitablePorts() {

        log.d("Finding suitable ports");
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

    /**
     * Open a serial port
     * @param port the port to open
     */
    private final void openPort(CommPortIdentifier port) {

        // Open the port
        try {
            log.d("Attempting to open serial port");
            this.port = (SerialPort)port.open(RFXtrx433.class.getName(), 38400);
            this.port.setSerialPortParams(38400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            in = this.port.getInputStream();
            out = this.port.getOutputStream();
            readerThread = new Thread(new Reader());
            readerThread.start();
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
    private final void closePort() {

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
    
    private void readerStopped() {
        // execute in a different thread so read thread can finish
        new Thread() {
            public void run() {
                closePort();
                findAndOpenPort();
            }
        }.start();
    }
    
    public void sendMessage(MessageWrapper messageWrapper) throws IOException {
        if(out == null)
            throw new IOException("Socket is not open");
        messageWrapper.writeTo(out, (byte)0);
    }
    
    private void messageReceived(MessageWrapper messageWrapper) {
        for(MessageListener<MessageWrapper> listener : listeners)
            listener.messageReceived(messageWrapper);
    }
    
    private class Reader implements Runnable {
        @Override
        public void run() {
            try {
                int packetLength;
                byte packetType, packetSubType, sequenceNumber;
                byte[] packetData;
                while((packetLength = in.read()) > 0) {
                    packetType = (byte)in.read();
                    packetSubType = (byte)in.read();
                    sequenceNumber = (byte)in.read();
                    packetData = new byte[packetLength - 3];
                    if(packetData.length > 0)
                        in.read(packetData);
                    messageReceived(new Message(packetType, packetSubType, packetData), sequenceNumber);
                }
                log.e("Packet length was -ve, stream was closed");
            } catch(IOException e) {
                log.e("Error reading from stream");
            }
            readerStopped();
        }
        
        public void messageReceived(Message message, byte sequenceNumber) {
            MessageWrapper messageWrapper = null;
            switch(message.getPacketType()) {
                case(Lighting2.PACKET_TYPE) :
                    messageWrapper = new Lighting2(message);
                    break;
            }
            RFXtrx433.this.messageReceived(messageWrapper);
        }
    }
}
