package net.hamnaberg.rest.spi;

import net.hamnaberg.rest.Handler;
import net.hamnaberg.rest.URIListHandler;

import java.net.URI;
import java.util.List;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class URIListHandlerSpi extends HandlerSpi<List<URI>> {
    public URIListHandlerSpi() {
        super("HTTPCache4j", "uri-list", "1.0");
    }

    @Override
    public Handler<List<URI>> createHandler() {
        return new URIListHandler(this);
    }
}