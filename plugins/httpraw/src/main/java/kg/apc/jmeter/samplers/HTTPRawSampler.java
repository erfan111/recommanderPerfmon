package kg.apc.jmeter.samplers;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Scanner;
import java.util.regex.Pattern;
import kg.apc.io.SocketChannelWithTimeouts;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;


// FIXME: actually keep-alive does not work!
public class HTTPRawSampler extends AbstractIPSampler {
    
    private static final String FILE_NAME = "fileName";
    private static final String KEEPALIVE = "keepalive";
    private static final String PARSE = "parse";
    private static final String RNpattern = "\\r\\n";
    private static final String SPACE = " ";
    // =e
    private static String Uname;
    private static String LATENCYFILENAME;// = "/home/erfan/file.txt";
    private static File latencyFile;
    private BufferedWriter bw;
    private FileWriter fw;
    //

    // 
    protected static final Logger log = LoggingManager.getLoggerForClass();
    private static final Pattern anyContent = Pattern.compile(".+", Pattern.DOTALL);
    private SocketChannel savedSock;
    private static final int fileSendingChunk = JMeterUtils.getPropDefault("kg.apc.jmeter.samplers.FileReadChunkSize", 1024 * 4);
    
    public HTTPRawSampler() {
        super();

        Uname = System.getProperty("user.name");
        LATENCYFILENAME = "/home/" + Uname + "/file.txt";

        //  =E
        latencyFile = new File(LATENCYFILENAME);
        try {
            if (!latencyFile.exists()) {
                latencyFile.createNewFile();
//                System.out.println("not exists");
            }
            fw = new FileWriter(latencyFile.getAbsoluteFile(), true);
//            System.out.println("file writer");
            bw = new BufferedWriter(fw);
//            System.out.println("buffered writer");
            latencyFile = new File(LATENCYFILENAME);
//            System.out.println("file");
        } catch (Exception e) {
            System.out.println("file writer exception");
            e.printStackTrace();
        }

        //
        log.debug("File reading chunk size: " + fileSendingChunk);
    }
    
    @Override
    public SampleResult sample(Entry entry) {
        SampleResult res = super.sample(entry);
        if (isParseResult()) {
            parseResponse(res);
        }
        return res;
    }
    
    protected byte[] readResponse(SocketChannel channel, SampleResult res) throws IOException {
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        
        ByteBuffer recvBuf = getRecvBuf();
        recvBuf.clear();
        
        boolean firstPack = true;
        int cnt;
        int responseSize = 0;
        
        if (log.isDebugEnabled()) {
            log.debug("Start reading response");
        }
        
        try {
            while ((cnt = channel.read(recvBuf)) != -1) {
                responseSize += cnt;
                if (firstPack) {
                    res.latencyEnd();
                    firstPack = false;
                }
                recvBuf.flip();
                if (response.size() <= recvDataLimit) {
                    byte[] bytes = new byte[cnt];
                    recvBuf.get(bytes);
                    response.write(bytes);
                }
                
                recvBuf.clear();
            }
            
            if (response.size() < 1) {
                log.warn("Read no bytes from socket, seems it was closed. Let it be so.");
                channel.close();
            }
        } catch (IOException ex) {
            channel.close();
            throw ex;
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Done reading response");
        }
        
        res.sampleEnd();
        if (!isUseKeepAlive()) {
            channel.close();
        }
        
        res.setBytes(responseSize);
        return response.toByteArray();
    }
    
    private void parseResponse(SampleResult res) {
        Scanner scanner = new Scanner(res.getResponseDataAsString());
        scanner.useDelimiter(RNpattern);
        
        if (!scanner.hasNextLine()) {
            return;
        }

        // =E

        // true = append file
        try {
            bw.write(res.getLatency() + "\n");
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println(res.getLatency() + " " + res.getResponseCode());
        //
        String httpStatus = scanner.nextLine();
        
        int s = httpStatus.indexOf(SPACE);
        int e = httpStatus.indexOf(SPACE, s + 1);
        if (s < e) {
            String rc = httpStatus.substring(s, e).trim();
            try {
                int rcInt = Integer.parseInt(rc);
                if (rcInt < 100 || rcInt > 599) {
                    return;
                }
                res.setResponseCode(rc);
                res.setResponseMessage(httpStatus.substring(e).trim());
            } catch (NumberFormatException ex) {
                return;
            }
        } else {
            return;
        }
        
        if (!scanner.hasNextLine()) {
            return;
        }
        StringBuilder headers = new StringBuilder();
        String line;
        while (scanner.hasNextLine() && !(line = scanner.nextLine()).isEmpty()) {
            headers.append(line).append(CRLF);
        }
        res.setResponseHeaders(headers.toString());
        
        if (scanner.hasNext()) {
            res.setResponseData(scanner.next(anyContent).getBytes());
        } else {
            res.setResponseData("".getBytes());
        }
        
    }
    
    @Override
    protected byte[] processIO(SampleResult res) throws Exception {
        SocketChannel sock = getSocketChannel();
        
        if (!getRequestData().isEmpty()) {
            ByteBuffer sendBuf = ByteBuffer.wrap(getRequestData().getBytes()); // cannot cache it because of variable processing
            sock.write(sendBuf);
        }
        sendFile(getFileToSend(), sock);
        if (log.isDebugEnabled()) {
            log.debug("Sent request");
        }
        return readResponse(sock, res);
    }
    
    protected SocketChannel getSocketChannel() throws IOException {
        if (isUseKeepAlive() && savedSock != null) {
            return savedSock;
        }
        
        int port;
        try {
            port = Integer.parseInt(getPort());
        } catch (NumberFormatException ex) {
            log.warn("Wrong port number: " + getPort() + ", defaulting to 80", ex);
            port = 80;
        }
        InetSocketAddress address = new InetSocketAddress(getHostName(), port);

        //log.info("Open sock");
        savedSock = (SocketChannel) getChannel();
        savedSock.connect(address);
        return savedSock;
    }
    
    public boolean isUseKeepAlive() {
        return getPropertyAsBoolean(KEEPALIVE);
    }
    
    public void setUseKeepAlive(boolean selected) {
        setProperty(KEEPALIVE, selected);
    }
    
    @Override
    protected AbstractSelectableChannel getChannel() throws IOException {
        int t = getTimeoutAsInt();
        if (t > 0) {
            SocketChannelWithTimeouts res = (SocketChannelWithTimeouts) SocketChannelWithTimeouts.open();
            res.setConnectTimeout(t);
            res.setReadTimeout(t);
            return res;
        } else {
            return SocketChannel.open();
        }
    }
    
    public boolean isParseResult() {
        return getPropertyAsBoolean(PARSE);
    }
    
    public void setParseResult(boolean selected) {
        setProperty(PARSE, selected);
    }
    
    public String getFileToSend() {
        return getPropertyAsString(FILE_NAME);
    }
    
    public void setFileToSend(String text) {
        setProperty(FILE_NAME, text);
    }
    
    @Override
    public boolean interrupt() {
        if (savedSock != null && savedSock.isOpen()) {
            try {
                savedSock.close();
            } catch (IOException ex) {
                log.warn("Exception while interrupting channel: ", ex);
                return false;
            }
        }
        return true;
    }
    
    private void sendFile(String filename, SocketChannel sock) throws IOException {
        if (filename.isEmpty()) {
            return;
        }
        
        FileInputStream is = new FileInputStream(new File(filename));
        FileChannel source = is.getChannel();
        
        ByteBuffer sendBuf = ByteBuffer.allocateDirect(fileSendingChunk);
        while (source.read(sendBuf) > 0) {
            sendBuf.flip();
            if (log.isDebugEnabled()) {
                log.debug("Sending " + sendBuf);
            }
            sock.write(sendBuf);
            sendBuf.rewind();
        }
        
        source.close();
    }
}
