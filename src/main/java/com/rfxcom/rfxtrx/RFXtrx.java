package com.rfxcom.rfxtrx;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intuso.utilities.listener.ListenerRegistration;
import com.intuso.utilities.listener.Listeners;
import com.intuso.utilities.log.Log;
import com.intuso.utilities.log.LogLevel;
import com.intuso.utilities.log.writer.StdOutWriter;
import com.rfxcom.rfxtrx.message.*;
import jssc.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: tomc
 * Date: 24/04/12
 * Time: 16:55
 * To change this template use File | Settings | File Templates.
 */
public class RFXtrx {

    private final Log log;
    private List<Pattern> patterns;
    private final Listeners<MessageListener> listeners = new Listeners(Lists.newCopyOnWriteArrayList());
    private final EventListener eventListener = new EventListener();
    private final OutputStream out = new OutputStreamWrapper();
    private final LinkedBlockingDeque<byte[]> readData = new LinkedBlockingDeque<byte[]>();
    private final Thread parserThread = new Thread(new Parser());
    private final Timer heartbeatTimer = new Timer("heartbeatTimer", true);

    private SerialPort port;

    public RFXtrx(Log log, List<Pattern> patterns) {
        this.log = log;
        this.patterns = patterns;
        parserThread.start();
        heartbeatTimer.schedule(new Heartbeat(), 600000L, 600000L); // every 10 minutes, 10 * 60 * 1000 = 600000
    }

    public List<Pattern> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<Pattern> patterns) {
        this.patterns = patterns;
    }

    public ListenerRegistration addListener(MessageListener listener) {
        return listeners.addListener(listener);
    }

    public final synchronized void openPort() throws IOException {
        outer: for(Pattern pattern : patterns) {
            log.d("Looking for comm ports matching " + pattern);
            Set<String> pns = Sets.newHashSet(SerialPortList.getPortNames(pattern));
            if (pns.size() > 0) {
                log.d("Found comm ports " + Joiner.on(",").join(pns));
                for(String pn : pns) {
                    log.d("Trying " + pn);
                    try {
                        openPort(pn);
                        break outer;
                    } catch(Throwable t) {
                        log.w("Failed to open " + pn);
                    }
                }
            } else
                throw new IOException("No ports match the pattern " + pattern);
        }
        if(port == null)
            throw new IOException("No ports available");
    }

    private synchronized void openPort(String portName) throws IOException {
        try {
            if (portName == null)
                throw new IOException("No port name set");

            log.d("Attempting to open serial port " + portName);
            port = new SerialPort(portName);
            port.openPort();
            port.setParams(SerialPort.BAUDRATE_38400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            port.addEventListener(eventListener, SerialPort.MASK_RXCHAR);
            sendMessage(new Interface(Interface.Command.Reset));
            try {
                Thread.sleep(100); // min 50ms, max 9 seconds
            } catch (InterruptedException e) {}
            port.readBytes(port.getOutputBufferBytesCount());
            sendMessage(new Interface(Interface.Command.GetStatus));
            log.d("Successfully opened serial port");
        } catch (SerialPortException e) {
            throw new IOException(e);
        }
    }

    /**
     * Open a serial port
     */
    public final synchronized void openPortSafe() {
        // Open the port
        try {
            openPort();
        } catch (IOException e) {
            log.e("Couldn't open serial port", e);
            this.port = null;
        }
    }

    /**
     * Close the relay port
     */
    public final synchronized void closePort() {

        if(port != null) {
            try {
                port.removeEventListener();
            } catch (Throwable t) {
                // do nothing, closing down anyway
            }
            try {
                port.closePort();
            } catch (Throwable t) {
                // do nothing, closing down anyway
            }
            port = null;
        }

        readData.clear();
    }

    public synchronized void sendMessage(MessageWrapper messageWrapper) throws IOException {
        log.d("Sending message: " + messageWrapper.toString());
        try {
            messageWrapper.writeTo(out, (byte) 0);
        } catch(IOException e) {
            log.w("Failed to write to socket, attempting close, open and re-write before failing", e);
            closePort();
            openPortSafe();
            if(port == null)
                throw new IOException("Socket is not open");
            messageWrapper.writeTo(out, (byte) 0);
        }
    }

    private void messageReceived(MessageWrapper messageWrapper) {
        for(MessageListener listener : listeners)
            listener.messageReceived(messageWrapper);
    }

    private class EventListener implements SerialPortEventListener {
        @Override
        public void serialEvent(SerialPortEvent serialPortEvent) {
            int available;
            try {
                while((available = port.getInputBufferBytesCount()) > 0)
                    readData.add(port.readBytes(available));
            } catch(SerialPortException e) {
                log.e("Failed to read data from serial port", e);
            }
        }
    }

    private class OutputStreamWrapper extends OutputStream {
        @Override
        public void write(int oneByte) throws IOException {
            try {
                if(port == null || !port.writeByte((byte) oneByte))
                    throw new IOException("Could not write data to serial port");
            } catch (SerialPortException e) {
                throw new IOException(e);
            }
        }
    }

    private class Parser implements Runnable {

        @Override
        public void run() {
            try {
                while(!Thread.currentThread().isInterrupted()) {
                    int packetLength;
                    byte packetType, packetSubType, sequenceNumber;
                    byte[] packetData;
                    outer:
                    while (true) {
                        packetLength = readBytes(1)[0];
                        if (packetLength < 0) {
                            log.e("Packet length was -ve, stream was closed");
                            break outer;
                        } else if (packetLength < 3) {
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
                }
            } catch(InterruptedException e) {
                log.e("Error reading from stream");
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
                case InterfaceResponse.PACKET_TYPE:
                    messageWrapper = new InterfaceResponse(message);
                    break;
                case Lighting1.PACKET_TYPE:
                    messageWrapper = new Lighting1(message);
                    break;
                case Lighting2.PACKET_TYPE:
                    messageWrapper = new Lighting2(message);
                    break;
                case TemperatureSensors.PACKET_TYPE:
                    messageWrapper = new TemperatureSensors(message);
                    break;
                case Undecoded.PACKET_TYPE:
                    messageWrapper = new Undecoded(message);
                    break;
                default:
                    log.d("Unknown packet type");
            }
            if(messageWrapper != null)
                RFXtrx.this.messageReceived(messageWrapper);
        }
    }

    private class Heartbeat extends TimerTask {

        @Override
        public void run() {
            if(port != null) {
                try {
                    sendMessage(new Interface(Interface.Command.GetStatus));
                } catch (IOException e) {
                    log.w("Failed to get status for heartbeat", e);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        RFXtrx rfxtrx = new RFXtrx(new Log(new StdOutWriter(LogLevel.DEBUG)), Lists.newArrayList(Pattern.compile(".*ttyUSB.*")));
        rfxtrx.openPortSafe();
        rfxtrx.sendMessage(new Interface(Interface.Command.EnableAllModes));
        rfxtrx.sendMessage(new Interface(Interface.Command.EnableUndecoded));
        rfxtrx.sendMessage(new Interface(Interface.Command.GetStatus));
        System.in.read();
        rfxtrx.closePort();
    }
}
