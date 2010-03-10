package net.hamnaberg.rest.spi;

import net.hamnaberg.rest.DefaultHandler;
import net.hamnaberg.rest.Handler;

import java.io.InputStream;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class URIListHandlerSpi extends HandlerSpi<InputStream> {
    public URIListHandlerSpi() {
        super("HTTPCache4j", "uri-list", "1.0");
    }

    @Override
    public Handler<InputStream> createHandler() {
        return new DefaultHandler(this);
    }
}