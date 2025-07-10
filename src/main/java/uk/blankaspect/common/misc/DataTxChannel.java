/*====================================================================*\

DataTxChannel.java

Class: data-transmission channel.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.misc;

//----------------------------------------------------------------------


// IMPORTS


import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import java.math.BigInteger;

import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import java.nio.charset.StandardCharsets;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import uk.blankaspect.common.exception2.ExceptionUtils;

import uk.blankaspect.common.function.IProcedure1;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.common.thread.DaemonFactory;

//----------------------------------------------------------------------


// CLASS: DATA-TRANSMISSION CHANNEL


public class DataTxChannel
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		int		MIN_PORT	= 49152;
	public static final		int		MAX_PORT	= 65535;

	public static final		int		MAX_NUM_DATA_ITEMS	= 1 << 12;  // 4096;

	private static final	String	HOST	= "localhost";

	private static final	char	END_OF_MESSAGE	= '\u0003';

	private static final	int		NUM_MESSAGE_PARTS	= 5;

	private static final	String	MESSAGE_SEPARATOR	= ":";

	private static final	String	CHANNEL_ID	= "\u03C6chan";

	private static final	BigInteger	ID_MASK;

	private static final	String	RECEIVER_THREAD_NAME	= DataTxChannel.class.getSimpleName() + "-receiver";
	private static final	String	CONNECTION_THREAD_NAME	= DataTxChannel.class.getSimpleName() + "-connection-port";

	private static final	int		CONNECTION_TIMEOUT			= 200;
	private static final	int		CONNECTION_ACCEPT_TIMEOUT	= 100;
	private static final	int		LISTENER_TIMEOUT			= 500;
	private static final	int		RESPONSE_TIMEOUT			= 400;

	private static final	String	UNEXPECTED_RESPONSE_STR	= "Unexpected response";
	private static final	String	INVALID_RESPONSE_STR	= "Invalid response";
	private static final	String	TIMED_OUT_STR			= "Timed out waiting for response";

	private interface MessageId
	{
		String	ACK		= "ACK";
		String	DATA	= "DATA.";
		String	END		= "END";
		String	NAK		= "NAK";
		String	START	= "START";
	}

	private enum ListenerState
	{
		READY,
		DATA,
		DONE,
		STOP
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String			id;
	private	ServerSocket	serverSocket;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		byte[] magnitude = new byte[8];
		Arrays.fill(magnitude, (byte)0xFF);
		ID_MASK = new BigInteger(1, magnitude);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public DataTxChannel(
		String	id)
	{
		// Initialise instance variables
		this.id = id;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static String getIdSuffix()
	{
		UUID uid = UUID.randomUUID();
		BigInteger u64 = BigInteger.valueOf(uid.getMostSignificantBits()).and(ID_MASK);
		BigInteger l64 = BigInteger.valueOf(uid.getLeastSignificantBits()).and(ID_MASK);
		BigInteger id128 = u64.shiftLeft(64).or(l64);
		return StringUtils.padBefore(id128.toString(36), 25, '0');
	}

	//------------------------------------------------------------------

	private static String read(
		Reader	reader,
		int		timeout)
		throws IOException
	{
		String message = null;
		StringBuilder buffer = new StringBuilder(1024);
		char[] readBuffer = new char[1024];
		long endTime = System.currentTimeMillis() + timeout;
		while ((timeout == 0) || (System.currentTimeMillis() < endTime))
		{
			// If there is something to read, read it
			if (reader.ready())
			{
				// Read into fixed-length buffer
				int readLength = reader.read(readBuffer);

				// If something was read, append it to main buffer and test for end of message
				if (readLength > 0)
				{
					// Test for end of message
					for (int i = readLength - 1; i >= 0; i--)
					{
						if (readBuffer[i] == END_OF_MESSAGE)
						{
							buffer.append(readBuffer, 0, i);
							message = buffer.toString();
							break;
						}
					}
					if (message != null)
						break;

					// Append data to main buffer
					buffer.append(readBuffer, 0, readLength);
				}
			}

			// If message has been read, stop reading
			if (message != null)
				break;

			// Allow other threads to run
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				// ignore
			}
		}
		return message;
	}

	//------------------------------------------------------------------

	private static MessageContent getMessageContent(
		String	message)
	{
		// Split message at separator
		String[] parts = message.split(MESSAGE_SEPARATOR, NUM_MESSAGE_PARTS);

		// If message is valid, return its content
		return ((parts.length == NUM_MESSAGE_PARTS) && parts[0].equals(CHANNEL_ID))
													? new MessageContent(parts[1], parts[2], parts[3], parts[4])
													: null;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public int openReceiver()
	{
		// Open server socket on automatically allocated port
		try
		{
			serverSocket = null;
			ServerSocket socket = new ServerSocket(0);
			socket.setSoTimeout(CONNECTION_ACCEPT_TIMEOUT);
			serverSocket = socket;
		}
		catch (BindException e)
		{
			// ignore
		}
		catch (IOException e)
		{
			ExceptionUtils.printStderrLocated(e);
		}

		// Return port number if server socket was opened
		return (serverSocket == null) ? -1 : serverSocket.getLocalPort();
	}

	//------------------------------------------------------------------

	public void listen(
		IProcedure1<String>	dataHandler)
	{
		// Test for open server socket
		if (serverSocket == null)
			throw new IllegalStateException("Receiver is not open");

		// Create and start receiver thread
		DaemonFactory.create(RECEIVER_THREAD_NAME, () ->
		{
			while (true)
			{
				// Accept connection from client
				Socket connectionSocket = null;
				try
				{
					connectionSocket = serverSocket.accept();
				}
				catch (SocketTimeoutException e)
				{
					// ignore
				}
				catch (IOException | SecurityException e)
				{
					ExceptionUtils.printStderrLocated(e);
				}

				// Start thread for new connection
				if (connectionSocket != null)
				{
					// Create and start connector thread
					Socket socket = connectionSocket;
					DaemonFactory.create(CONNECTION_THREAD_NAME + serverSocket.getLocalPort(), () ->
					{
						try
						{
							// Get input stream of socket
							InputStreamReader inStream =
									new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);

							// Get output stream of socket
							BufferedWriter outStream =
									new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),
													   StandardCharsets.UTF_8));

							// Read from input stream
							StringBuilder buffer = new StringBuilder(1024);
							int numDataItems = 0;
							int dataItemCount = 0;
							ListenerState state = ListenerState.READY;
							while (state != ListenerState.STOP)
							{
								// Read message
								String message = read(inStream, LISTENER_TIMEOUT);

								// Get message content
								MessageContent content = (message == null) ? null : getMessageContent(message);

								// If target of message was this listener, process message
								if ((content != null) && content.targetId.equals(id))
								{
									// Respond to message
									switch (state)
									{
										case READY:
											// Message: start
											if (content.messageId.equals(MessageId.START))
											{
												// Parse number of data items
												try
												{
													numDataItems = Integer.parseInt(content.data);
													if (numDataItems > MAX_NUM_DATA_ITEMS)
														numDataItems = -1;
												}
												catch (NumberFormatException e)
												{
													numDataItems = -1;
												}

												// If number of data items was invalid, disconnect ...
												if (numDataItems < 0)
												{
													// Send NAK
													writeNak(outStream, content);

													// Disconnect
													state = ListenerState.STOP;
												}

												// ... otherwise, send ACK
												else
												{
													// Send ACK
													writeAck(outStream, content);

													// Change state
													state = (numDataItems == 0)
																? ListenerState.DONE : ListenerState.DATA;
												}
											}

											// Unexpected message: disconnect
											else
											{
												// Send NAK
												writeNak(outStream, content);

												// Disconnect
												state = ListenerState.STOP;
											}
											break;

										case DATA:
											// Message: data
											if (content.messageId.startsWith(MessageId.DATA))
											{
												// Parse item index
												int index = -1;
												try
												{
													index = Integer.parseInt(content.messageId
																				.substring(MessageId.DATA.length()));
												}
												catch (NumberFormatException e)
												{
													// ignore
												}

												// If item index was correct, send ACK ...
												if (index == dataItemCount)
												{
													// Send ACK
													writeAck(outStream, content);

													// Append data item to buffer
													buffer.append(content.data);

													// Increment item count and test for last item
													if (++dataItemCount == numDataItems)
														state = ListenerState.DONE;
												}

												// ... otherwise, disconnect
												else
												{
													// Send NAK
													writeNak(outStream, content);

													// Disconnect
													state = ListenerState.STOP;
												}
											}

											// Unexpected message: disconnect
											else
											{
												// Send NAK
												writeNak(outStream, content);

												// Disconnect
												state = ListenerState.STOP;
											}
											break;

										case DONE:
											// Message: end
											if (content.messageId.equals(MessageId.END))
											{
												// Send acknowledgement
												writeAck(outStream, content);

												// Handle data
												dataHandler.invoke(buffer.toString());

												// Disconnect
												state = ListenerState.STOP;
											}

											// Unexpected message: disconnect
											else
											{
												// Send NAK
												writeNak(outStream, content);

												// Disconnect
												state = ListenerState.STOP;
											}
											break;

										case STOP:
											// do nothing
											break;
									}
								}
							}
						}
						catch (IOException | SecurityException e)
						{
							ExceptionUtils.printStderrLocated(e);
						}
						finally
						{
							// Close socket
							try
							{
								socket.close();
							}
							catch (IOException e)
							{
								// ignore
							}
						}
					})
					.start();
				}
			}
		})
		.start();
	}

	//------------------------------------------------------------------

	public boolean transmit(
		int			port,
		String		targetId,
		String...	dataItems)
	{
		return transmit(port, targetId, List.of(dataItems));
	}

	//------------------------------------------------------------------

	public boolean transmit(
		int				port,
		String			targetId,
		List<String>	dataItems)
	{
		boolean success = false;

		// Create socket
		Socket socket = new Socket();

		// Connect socket to local port
		try
		{
			socket.connect(new InetSocketAddress(HOST, port), CONNECTION_TIMEOUT);
		}
		catch (IOException e)
		{
			ExceptionUtils.printStderrLocated(e);
		}

		// If socket is connected, transmit data to target
		if (socket.isConnected())
		{
			try
			{
				// Get input stream of socket
				InputStreamReader inStream = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);

				// Get output stream of socket
				BufferedWriter outStream =
						new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

				// Send 'start' message
				writeMessage(outStream, targetId, MessageId.START, Integer.toString(dataItems.size()));

				// Read response
				MessageContent response = readResponse(inStream);

				// If expected response was received, send data
				if (response.matches(targetId, id, MessageId.ACK, MessageId.START))
				{
					// Send data items
					for (int i = 0; i < dataItems.size(); i++)
					{
						// Send data
						String messageId = MessageId.DATA + i;
						writeMessage(outStream, targetId, messageId, dataItems.get(i));

						// Wait for response
						response = readResponse(inStream);

						// Test for acknowledgement
						if (!response.matches(targetId, id, MessageId.ACK, messageId))
							throw new IOException(UNEXPECTED_RESPONSE_STR);
					}

					// Indicate success
					success = true;

					// Send 'end' message; don't wait for response
					writeMessage(outStream, targetId, MessageId.END, null);
				}
			}
			catch (IOException | SecurityException e)
			{
				ExceptionUtils.printStderrLocated(e);
			}
			finally
			{
				// Close socket
				try
				{
					if (socket != null)
						socket.close();
				}
				catch (IOException e)
				{
					// ignore
				}
			}
		}

		return success;
	}

	//------------------------------------------------------------------

	private MessageContent readResponse(
		Reader	reader)
		throws IOException
	{
		// Read message
		String message = read(reader, RESPONSE_TIMEOUT);
		if (message == null)
			throw new IOException(TIMED_OUT_STR);

		// Get message content
		MessageContent content = getMessageContent(message);
		if (content == null)
			throw new IOException(INVALID_RESPONSE_STR);

		// Return message content
		return content;
	}

	//------------------------------------------------------------------

	private void writeMessage(
		Writer	writer,
		String	targetId,
		String	messageId,
		String	data)
		throws IOException
	{
		writer.write(CHANNEL_ID);
		writer.write(MESSAGE_SEPARATOR);
		writer.write(id);
		writer.write(MESSAGE_SEPARATOR);
		writer.write(targetId);
		writer.write(MESSAGE_SEPARATOR);
		writer.write(messageId);
		writer.write(MESSAGE_SEPARATOR);
		if (data != null)
			writer.write(data);
		writer.write(END_OF_MESSAGE);
		writer.flush();
	}

	//------------------------------------------------------------------

	private void writeAck(
		Writer			writer,
		MessageContent	content)
		throws IOException
	{
		writeMessage(writer, content.sourceId, MessageId.ACK, content.messageId);
	}

	//------------------------------------------------------------------

	private void writeNak(
		Writer			writer,
		MessageContent	content)
		throws IOException
	{
		writeMessage(writer, content.sourceId, MessageId.NAK, content.messageId);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member records
////////////////////////////////////////////////////////////////////////


	// RECORD: MESSAGE CONTENT


	private record MessageContent(
		String	sourceId,
		String	targetId,
		String	messageId,
		String	data)
	{

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String toString()
		{
			return sourceId + MESSAGE_SEPARATOR + targetId + MESSAGE_SEPARATOR + messageId + MESSAGE_SEPARATOR + data;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private boolean matches(
			String	sourceId,
			String	targetId,
			String	messageId,
			String	data)
		{
			return this.sourceId.equals(sourceId) && this.targetId.equals(targetId) && this.messageId.equals(messageId)
					&& this.data.equals(data);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
