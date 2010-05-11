package net.hamnaberg.rest.spi;

import net.hamnaberg.rest.DefaultHandler;
import net.hamnaberg.rest.Handler;

import java.io.InputStream;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class DefaultHandlerSpi extends HandlerSpi {
    @Override
    public Handler createHandler() {
        return new DefaultHandler(this);
    }
}
