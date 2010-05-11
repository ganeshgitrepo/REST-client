package net.hamnaberg.rest.spi;

import net.hamnaberg.rest.Handler;
import net.hamnaberg.rest.URIListHandler;

import java.net.URI;
import java.util.List;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class URIListHandlerSpi extends HandlerSpi {
    @Override
    public Handler createHandler() {
        return new URIListHandler(this);
    }
}