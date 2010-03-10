package net.hamnaberg.rest;

import fj.data.Option;
import org.apache.commons.io.IOUtils;
import org.codehaus.httpcache4j.cache.HTTPCache;
import org.codehaus.httpcache4j.cache.MemoryCacheStorage;
import org.codehaus.httpcache4j.resolver.HTTPClientResponseResolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class DefaultRESTfulClient extends RESTfulClient {

    protected DefaultRESTfulClient(HTTPCache cache, String username, String password) {
        super(cache, Option.fromNull(username), Option.fromNull(password));
    }


    public static void main(String[] args) throws IOException {
        DefaultRESTfulClient client = new DefaultRESTfulClient(new HTTPCache(new MemoryCacheStorage(), HTTPClientResponseResolver.createMultithreadedInstance()), null, null);
        Option<Resource<InputStream>> resourceOption = client.read(new ResourceHandle(URI.create("http://www.vg.no")));
        if (resourceOption.isSome()) {
            Resource<InputStream> resource = resourceOption.some();
            String value = IOUtils.toString(resource.getData().some());
            System.out.println(value);
        }
        else {
            System.out.println("No option here");
        }
    }
}
