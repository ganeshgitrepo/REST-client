package net.hamnaberg.rest;

import fj.data.Option;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.cache.CacheItem;
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
public class DefaultRESTClient extends RESTClient {

    protected DefaultRESTClient(HTTPCache cache, String username, String password) {
        super(cache, Option.fromNull(username), Option.fromNull(password));
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        ResourceHandle handle = new ResourceHandle(URI.create("http://www.digi.no/"));
        MemoryCacheStorage storage = new MemoryCacheStorage();
        DefaultRESTClient client = new DefaultRESTClient(new HTTPCache(storage, HTTPClientResponseResolver.createMultithreadedInstance()), null, null);
        Option<Resource<InputStream>> resourceOption = client.read(handle);
        if (resourceOption.isSome()) {
            Resource<InputStream> resource = resourceOption.some();
            System.out.println(resource.getHeaders());
            //String value = IOUtils.toString(resource.getData().some());
            //System.out.println(value);
        }
        System.out.println("");
        HTTPRequest req = new HTTPRequest(handle.getURI());
        CacheItem item = storage.get(req);
        System.out.println("item = " + item);
        System.out.println("item = " + item.isStale(req));
    }
}
