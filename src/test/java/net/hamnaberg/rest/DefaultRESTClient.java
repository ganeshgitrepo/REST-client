package net.hamnaberg.rest;

import fj.data.Either;
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
        Either<Failure, Option<Resource<InputStream>>> either = client.read(handle);
        if (either.isRight()) {
            Option<Resource<InputStream>> resource = either.right().value();
            if (resource.isSome()) {
                System.out.println(resource.some().getHeaders());
            }
            resource.some().getData().some().close();
            //String value = IOUtils.toString(resource.getData().some());
            //System.out.println(value);
        }
        System.out.println("");
        HTTPRequest req = new HTTPRequest(handle.getURI());
        CacheItem item = storage.get(req);
        System.out.println("item = " + item);
        System.out.println("item is stale = " + item.isStale(req));
    }
}
