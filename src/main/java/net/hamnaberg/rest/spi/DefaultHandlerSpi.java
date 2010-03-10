package net.hamnaberg.rest.spi;

import net.hamnaberg.rest.DefaultHandler;
import net.hamnaberg.rest.Handler;

import java.io.InputStream;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class DefaultHandlerSpi extends HandlerSpi<InputStream> {
    public DefaultHandlerSpi() {
        super("HTTPCache4j", "default", "1.0");
    }

    @Override
    public Handler<InputStream> createHandler() {
        return new DefaultHandler(this);
    }
}
