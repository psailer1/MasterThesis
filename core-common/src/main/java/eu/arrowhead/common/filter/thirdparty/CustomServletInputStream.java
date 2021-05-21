package eu.arrowhead.common.filter.thirdparty;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class CustomServletInputStream extends ServletInputStream {
	
	//=================================================================================================
	// members

	private byte[] myBytes;

    private int lastIndexRetrieved = -1;
    private ReadListener readListener = null;

    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
	public CustomServletInputStream(final String s) {
        try {
            this.myBytes = s.getBytes(StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException ex) {
            throw new IllegalStateException("JVM did not support UTF-8", ex);
        }
    }

    //-------------------------------------------------------------------------------------------------
	public CustomServletInputStream(final byte[] inputBytes) {
        this.myBytes = inputBytes;
    }

    //-------------------------------------------------------------------------------------------------
	@Override
    public boolean isFinished() {
        return (lastIndexRetrieved == myBytes.length - 1);
    }

    //-------------------------------------------------------------------------------------------------
	@Override
    public boolean isReady() {
        // This implementation will never block
        // We also never need to call the readListener from this method, as this method will never return false
        return isFinished();
    }

    //-------------------------------------------------------------------------------------------------
	@Override
    public void setReadListener(final ReadListener readListener) {
        this.readListener = readListener;
        if (!isFinished()) {
            try {
                readListener.onDataAvailable();
            } catch (final IOException e) {
                readListener.onError(e);
            }
        } else {
            try {
                readListener.onAllDataRead();
            } catch (final IOException e) {
                readListener.onError(e);
            }
        }
    }

    //-------------------------------------------------------------------------------------------------
	@Override
    public int read() throws IOException {
        int i;
        if (!isFinished()) {
            i = myBytes[lastIndexRetrieved + 1];
            lastIndexRetrieved++;
            if (isFinished() && (readListener != null)) {
                try {
                    readListener.onAllDataRead();
                } catch (final IOException ex) {
                    readListener.onError(ex);
                    throw ex;
                }
            }
            return i;
        } else {
            return -1;
        }
    }
}