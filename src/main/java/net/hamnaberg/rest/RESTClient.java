/*
 * Copyright 2009 JavaBin
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package net.hamnaberg.rest;

import fj.data.Either;
import net.hamnaberg.rest.spi.HandlerSpi;
import org.codehaus.httpcache4j.cache.HTTPCache;
import org.codehaus.httpcache4j.cache.MemoryCacheStorage;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.*;
import org.apache.commons.lang.Validate;
import fj.data.Option;

import static fj.data.Option.fromNull;
import static fj.data.Option.none;

import fj.Unit;
import org.codehaus.httpcache4j.resolver.ResponseResolver;

import static fj.Unit.unit;

import java.util.*;
import java.net.URI;


/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class RESTClient {
    private final HTTPCache cache;
    private final Set<Handler> handlers = new HashSet<Handler>();
    private final Challenge challenge;

    public RESTClient(HTTPCache cache, Option<String> username, Option<String> password) {
        Validate.notNull(cache, "Cache may not be null");
        Validate.notNull(username, "Username option may not be null");
        Validate.notNull(password, "password option may not be null");
        if (username.isSome() && password.isSome()) {
            challenge = new UsernamePasswordChallenge(username.some(), password.some());
        }
        else {
            challenge = null;
        }
        this.cache = cache;
        ServiceLoader<HandlerSpi> spis = ServiceLoader.load(HandlerSpi.class);
        for (HandlerSpi spi : spis) {
            registerHandler(spi.createHandler());
        }
    }

    public RESTClient(HTTPCache cache) {
       this(cache, Option.<String>none(), Option.<String>none());
    }

    public RESTClient(ResponseResolver responseResolver) {
       this(new HTTPCache(new MemoryCacheStorage(), responseResolver), Option.<String>none(), Option.<String>none());
    }

    public RESTClient(ResponseResolver responseResolver, String username, String password) {
       this(new HTTPCache(new MemoryCacheStorage(), responseResolver), fromNull(username), fromNull(password));
    }

    protected void registerHandler(Handler handler) {
        handlers.add(handler);
    }

    protected Set<Handler> getHandlers() {
        return handlers;
    }

    protected HTTPCache getCache() {
        return cache;
    }

    public Either<Failure, Unit> update(ResourceHandle handle, Payload payload) {
        Validate.notNull(handle, "Handle may not be null");
        Validate.notNull(payload, "Payload may not be null");
        HTTPRequest request = new HTTPRequest(handle.getURI(), HTTPMethod.PUT);
        request = request.challenge(challenge);
        request = request.payload(payload);
        if (handle.isTagged()) {
            request = request.conditionals(request.getConditionals().addIfMatch(handle.getTag().some()));
        }
        List<Status> acceptedStatuses = Arrays.asList(Status.OK, Status.NO_CONTENT);
        HTTPResponse response = cache.execute(request);
        response.consume();
        if (!acceptedStatuses.contains(response.getStatus())) {
            return Either.left(new Failure(request.getRequestURI(), response.getStatus(), response.getHeaders()));
        }
        return Either.right(unit());
    }

    public Either<Failure, Unit> remove(ResourceHandle handle) {
        Validate.notNull(handle, "Handle may not be null");
        List<Status> acceptedStatuses = Arrays.asList(Status.OK, Status.NO_CONTENT);
        HTTPRequest request = new HTTPRequest(handle.getURI(), HTTPMethod.DELETE).challenge(challenge);        
        HTTPResponse response = cache.execute(request);
        response.consume();
        if (!acceptedStatuses.contains(response.getStatus())) {
            return Either.left(new Failure(request.getRequestURI(), response.getStatus(), response.getHeaders()));
        }
        return Either.right(unit());
    }

    public <R> Either<Failure, Option<Resource<R>>> process(ResourceHandle handle, Payload payload) {
        Validate.notNull(handle, "Handle may not be null");
        Validate.notNull(payload, "Payload may not be null");
        HTTPRequest request = new HTTPRequest(handle.getURI(), HTTPMethod.POST);
        request = request.challenge(challenge);
        request = request.payload(payload);
        HTTPResponse response = cache.execute(request);
        if (response.getStatus().isClientError() || response.getStatus().isServerError()) {
            return Either.left(new Failure(request.getRequestURI(), response.getStatus(), response.getHeaders()));
        }
        if (response.hasPayload()) {
            return Either.right(this.<R>handle(handle, response));
        }
        return Either.right(Option.<Resource<R>>none());
    }

    public Either<Failure, Headers> inspect(ResourceHandle handle) {
        Validate.notNull(handle, "Handle may not be null");
        HTTPRequest request = new HTTPRequest(handle.getURI(), HTTPMethod.HEAD);
        
        request = request.challenge(challenge);
        HTTPResponse response = cache.execute(request);

        response.consume();
        if (response.getStatus().isClientError() || response.getStatus().isServerError()) {
            return Either.left(new Failure(request.getRequestURI(), response.getStatus(), response.getHeaders()));
        }
        return Either.right(response.getHeaders());
    }

    public <R> Either<Failure, Option<Resource<R>>> createAndRead(ResourceHandle handle, Payload payload, List<MIMEType> types) {
        Validate.notNull(handle, "Handle may not be null");
        Validate.notNull(payload, "Payload may not be null");
        HTTPRequest request = new HTTPRequest(handle.getURI(), HTTPMethod.POST);
        request = request.challenge(challenge);
        request = request.payload(payload);
        HTTPResponse response = cache.execute(request);
        if (response.getStatus() != Status.CREATED) {
            return Either.left(new Failure(request.getRequestURI(), response.getStatus(), response.getHeaders()));
        }
        if (!response.hasPayload()) {
            String location = response.getHeaders().getFirstHeaderValue("Location");
            return read(new ResourceHandle(URI.create(location)), types);
        }
        return Either.right(this.<R>handle(handle, response));
    }

    public Either<Failure, ResourceHandle> create(ResourceHandle handle, Payload payload) {
        Validate.notNull(handle, "Handle may not be null");
        Validate.notNull(payload, "Payload may not be null");
        HTTPRequest request = new HTTPRequest(handle.getURI(), HTTPMethod.POST);
        request = request.challenge(challenge);
        request = request.payload(payload);
        HTTPResponse response = cache.execute(request);
        if (response.getStatus() != Status.CREATED) {
            return Either.left(new Failure(request.getRequestURI(), response.getStatus(), response.getHeaders()));
        }
        return Either.right(new ResourceHandle(URI.create(response.getHeaders().getFirstHeaderValue("Location")), Option.<Tag>none()));
    }

    public <R> Either<Failure, Option<Resource<R>>> read(ResourceHandle handle) {
        return read(handle, Collections.<MIMEType>emptyList());
    }

    public <R> Either<Failure, Option<Resource<R>>> read(ResourceHandle handle, List<MIMEType> types) {
        Validate.notNull(handle, "Handle may not be null");
        HTTPRequest request = new HTTPRequest(handle.getURI(), HTTPMethod.GET);
        if (handle.isTagged()) {
            request = request.conditionals(request.getConditionals().addIfNoneMatch(handle.getTag().some()));
        }
        if (types != null) {
            for (MIMEType type : types) {
                request = request.preferences(request.getPreferences().addMIMEType(type));
            }
        }
        request = request.challenge(challenge);
        HTTPResponse response = cache.execute(request);
        ResourceHandle updatedHandle = new ResourceHandle(handle.getURI(), fromNull(response.getETag()));
        if (updatedHandle.equals(handle) && response.getStatus() == Status.NOT_MODIFIED) {
            return Either.right(Option.<Resource<R>>none());
        }
        else if (response.getStatus() == Status.OK) {
            return Either.right(this.<R>handle(updatedHandle, response));
        }
        return Either.right(this.<R>handle(handle, response));
    }

    protected <R> Option<Resource<R>> handle(ResourceHandle handle, HTTPResponse response) {
        if (response.hasPayload()) {
            for (Handler handler : getHandlers()) {
                if (handler.supports(response.getPayload().getMimeType())) {
                    Handler<R> typedHandler = handler; // may cause ClassCastException...
                    return Option.<Resource<R>>some(DefaultResource.<R>create(handle, response.getHeaders(), typedHandler.handle(response.getPayload())));
                }
            }
        }
        return none();
    }   
}
