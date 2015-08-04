/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.procletService.connect;

import java.io.*;
import java.net.*;

/** <p>
 * <h2>JavaCPN</h2> (otherwise known as Comms/Java) - The java
 * implmentation of a
 * peer entity matching the Comms/Java implementation for Design/CPN. (See 
 * http://www.daimi.au.dk/designCPN/libs/commscpn/ for more details.)
 * The purpose of Java/CPN is to allow Java processes to communicate with
 * Design/CPN through Comms/CPN.  The current implementation of Java/CPN
 * is the minimal implementation necessary to enable communication.  It
 * incorporates the equivalent functionality of the Messaging and
 * Communication layers from Comms/CPN.  The Communication Layer
 * functionality from Comms/CPN and TCP/IP is already encapsulated
 * in Socket objects provided by Java.
 * </p>
 * <p>
 * No connection management has been implemented within Java/CPN as
 * this is a minimal implementation, however the important thing is that
 * it implements the same protocol at the Messaging Layer as the peer
 * entity.  Generic <CODE>send</CODE> and <CODE>receive</CODE> 
 * functions have been provided at
 * the level of the Messaging Layer, meaning that sequences of bytes are
 * passed to the send method and returned from the receive method.  The
 * <CODE>connect</CODE>, <CODE>accept</CODE>, and <CODE>disconnect</CODE>
 * methods have been provided at
 * the level of the Communication Layer from Comms/CPN.  The
 * deliberate attempt was made to make the interface as close to that of
 * Comms/CPN as possible. 
 * </p>
 * <p>
 * Methods external to the Java/CPN class must be used to convert
 * from data (i.e. a string) into a ByteArrayInputStream object, and from
 * a ByteArrayOutputStream object back into data.  This is akin to the
 * encoding and decoding functions passed into the send and receive
 * functions of the Connection Management Layer in Comms/CPN.  They are
 * contained within the <a href="EncodeDecode.html">EncodeDecode</a> class.
 * </p>
 * @author Guy Gallasch
 * @version 0.6
 * 
 * Change Log:
 * 
 * 10th February 2002:
 * - Improved robustness of Receive method - reads multiple times
 *   from the socket in a loop if not all bytes can be read on the first
 *   attempt.
 * 
 */
public class JavaCPN implements JavaCPNInterface
{
    
/** Internal reference to the socket being used
 */
    private Socket socket;
/** internal reference to the socket input stream
 */
    private InputStream input;
/** internal reference to the socket output stream
 */
    private OutputStream output;
    
/** Constructor to create a new JavaCPN object. Simply initialises the internal
 * references. In order to establish a connection either the <CODE>connect
 * </CODE> or the <CODE>accept</CODE> methods need to be called.
 */
    public JavaCPN()
    {
        socket = null;
        input = null;
        output = null;
    }
    
/** Method to actively establish a connection. It takes a
 * host name and port number as arguments, and attempts to establish a
 * connection as a client to the given port on the given host.  Once the
 * connection has been established (i.e. the socket opened) input and
 * output streams are extracted from the socket to enable the transmission
 * and reception of bytes.
 * @param hostName The host to attempt to connect to
 * @param port The port number to attempt to connect to
 * @throws IOException Thrown when there is a communication error
 * @throws UnknownHostException Thrown when the host name provided as the argument 
 * cannot be resolved into an IP address
 */
    public void connect(String hostName, int port) throws IOException, UnknownHostException
    {
        socket = new Socket(InetAddress.getByName(hostName), port);
        input = socket.getInputStream();
        output = socket.getOutputStream();
    }
    
/** Method to passively open a connection. It takes a port number as an
 * argument and, acting as a server, listens on the given port number for
 * an incoming connection request.  When received, it establishes the
 * connection.  Again, once the connection has been established, input
 * and output streams are extracted from the socket to enable the
 * transmission and reception of bytes.   The method will block until a
 * connection is established.
 * @param port The port number to attempt to connect to
 * @throws IOException Thrown when there is a communication error
 */
    public void accept(int port) throws IOException
    {
        ServerSocket serverSocket = new ServerSocket(port);
        socket = serverSocket.accept();
        input = socket.getInputStream();
        output = socket.getOutputStream();
        serverSocket.close();
    }
    
    
/** Method used to send a ByteArrayInputStream via an established
 * connection. This method takes a ByteArrayInputStream object
 * as the argument.  The segmentation into packets occurs in this method. 
 * Bytes are read from the ByteArrayInputStream object, a maximum
 * of 127 at a time, and a single byte header is added indicating the
 * number of payload bytes (header is 1 to 127) or that there is more data 
 * in a following packet (header is 255). The data packets formed are then
 * transmitted to the external process through methods acting on the
 * output stream of the socket.  
 * @param sendBytes The byte stream to be sent to the receiving end of the connection
 * @throws SocketException Thrown if there is a problem sending the byte stream
 */
    public synchronized void send(ByteArrayInputStream sendBytes) throws SocketException
    {
        // A byte array representing a data packet
        byte[] packet;
        
        // While there are more than 127 bytes still to send ...
        while (sendBytes.available() > 127)
        {
            // ... create a 128 byte packet, ...
            packet = new byte[128];
            
            // ... set the header to 255, ...
            packet[0] = (byte)255;
            
            // ... read 127 bytes from the sequence of bytes to send, ...
            sendBytes.read(packet, 1, 127);
            
            // ... and send the packet to the external process.
            try
            {
                output.write(packet);
                output.flush();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        
        // Create a packet for any remaining data
        packet = new byte[sendBytes.available() + 1];
        
        // Set the header appropriately
        packet[0] = (byte)(sendBytes.available());
        
        // Read the remaining bytes into the packet
        sendBytes.read(packet, 1, sendBytes.available());
        
        // Send the packet to the external process
        try
        {
            output.write(packet);
            output.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
/** Method used to receive a ByteArrayOutputStream from an established
 * connection. This method has no arguments.  It uses methods that
 * act on the input stream of the socket to firstly receive a header
 * byte, and then receive the number of payload bytes specified in the
 * header, from the external process.  The payload bytes are stored in a
 * ByteArrayOutputStream object as each segment of payload data is
 * received. This process is repeated until all data has been received for
 * the current transmission. 
 * @return sendBytes The byte stream received from the other end of the connection
 * @throws SocketException Thrown if there is a problem sending the byte stream
 */
    public ByteArrayOutputStream receive() throws SocketException
    {
        // The complete sequence of bytes received from the external process
        ByteArrayOutputStream receivedBytes = new ByteArrayOutputStream();
        
        // The header received from the external process
        int header = -1;
        
        // The number of payload bytes received from the external process for a packet
        int numberRead = 0;

        // The total number of payload bytes received from the external process for
        // a packet, if not all are received immediately.
        int totalNumberRead = 0;

        // The payload received from the external process for each packet
        byte[] payload;
        
        while(true)
        {
            // Read a header byte from the input stream
            try
            {
                header = (int)input.read();
            }
            catch (SocketException e)
            {
                throw new SocketException("Socket closed while blocking to receive header.");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            
            // If the header shows that the socket has closed ...
            if (header == -1)
                // ... throw a SocketException
                throw new SocketException("Socket closed by external process.");
            
            // If the header indicates another packet to follow ...
            else if (header >= 127)
            {
                // ... create 127 bytes of payload storage ...
                payload = new byte[127];
            }
            
            // ... else create storage of the appropriate size
            else
                payload = new byte[header];
            
            // Read the payload bytes from the input stream

            // Reset the total bytes received to 0 for this iteration 
            totalNumberRead = 0;

            // Loop until all data has been read for this packet.
            while(totalNumberRead < payload.length && numberRead != -1) 
            {
                try
                {
                    // Try to read all bytes in this packet
                    numberRead = input.read(payload,totalNumberRead, payload.length - totalNumberRead);
            
                    // If some bytes were read ...
                    if(numberRead != -1)

                        // ... record this many bytes as having been read
                        totalNumberRead = totalNumberRead + numberRead; 

                }
                catch (SocketException e)
                {
                  throw new SocketException("Socket closed while receiving data.");
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            
            // If not all bytes could be read ...
            if ((totalNumberRead < header || numberRead == -1) && header != 255) 

                // ... throw a SocketException
                throw new SocketException("Error receiving data.");

            // Write the payload data to the complete sequence of received bytes
            receivedBytes.write(payload,0,payload.length);
            
            // If no more bytes to follow, break from the loop.
            if (header <= 127)
                break;
        }

        // Return the received bytes
        return receivedBytes;
    }
    
/** Method to disconnect the established connection. This method has no
 * arguments, and returns no value.  It closes the input and output
 * streams from the socket before closing the socket itself.
 * @throws IOException if there is a problem closing the connection
 */
    public void disconnect() throws IOException
    {
        input.close();
        output.close();
        socket.close();
    }
}


