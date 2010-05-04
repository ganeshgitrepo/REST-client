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

import net.hamnaberg.rest.spi.HandlerSpi;
import org.codehaus.httpcache4j.cache.HTTPCache;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.*;
import org.apache.commons.lang.Validate;
import fj.data.Option;

import static fj.data.Option.fromNull;
import static fj.data.Option.none;
import static fj.data.Option.some;
import fj.Unit;
import static fj.Unit.unit;

import java.util.*;
import java.net.URI;


/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public abstract class RESTfulClient {
    private final HTTPCache cache;
    private final Set<Handler> handlers = new HashSet<Handler>();
    private final Challenge challenge;

    protected RESTfulClient(HTTPCache cache, Option<String> username, Option<String> password) {
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

    protected RESTfulClient(HTTPCache cache) {
       this(cache, Option.<String>none(), Option.<String>none());
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

    public Unit update(ResourceHandle handle, Payload payload) {
        Validate.notNull(handle, "Handle may not be null");
        Validate.notNull(payload, "Payload may not be null");
        HTTPRequest request = new HTTPRequest(handle.getURI(), HTTPMethod.PUT);
        request = request.challenge(challenge);
        request = request.payload(payload);
        if (handle.isTagged()) {
            request = request.conditionals(request.getConditionals().addIfMatch(handle.getTag().some()));
        }
        HTTPResponse response = cache.doCachedRequest(request);
        if (response.getStatus() != Status.OK) {
            throw new RESTException(handle.getURI(), response.getStatus());
        }
        return unit();
    }

    public void remove(ResourceHandle handle) {
        Validate.notNull(handle, "Handle may not be null");
        List<Status> acceptedStatuses = Arrays.asList(Status.OK, Status.NO_CONTENT);
        HTTPRequest request = new HTTPRequest(handle.getURI(), HTTPMethod.DELETE);
        HTTPResponse response = cache.doCachedRequest(request);
        if (!acceptedStatuses.contains(response.getStatus())) {
            throw new RESTException(handle.getURI(), response.getStatus());
        }
    }

    public <T> Option<Resource<T>> process(ResourceHandle handle, Payload payload) {
        Validate.notNull(handle, "Handle may not be null");
        Validate.notNull(payload, "Payload may not be null");
        HTTPRequest request = new HTTPRequest(handle.getURI(), HTTPMethod.POST);
        request = request.challenge(challenge);
        request = request.payload(payload);
        HTTPResponse response = cache.doCachedRequest(request);
        if (response.getStatus().isClientError() || response.getStatus().isServerError()) {
            throw new RESTException(handle.getURI(), response.getStatus());
        }
        if (response.hasPayload()) {
            return handle(handle, response);
        }
        return Option.none();
    }

    public <T> Option<Resource<T>> createAndRead(ResourceHandle handle, Payload payload, List<MIMEType> types) {
        Validate.notNull(handle, "Handle may not be null");
        Validate.notNull(payload, "Payload may not be null");
        HTTPRequest request = new HTTPRequest(handle.getURI(), HTTPMethod.POST);
        request = request.challenge(challenge);
        request = request.payload(payload);
        HTTPResponse response = cache.doCachedRequest(request);
        if (response.getStatus() != Status.CREATED) {
            throw new RESTException(handle.getURI(), response.getStatus());
        }
        if (!response.hasPayload()) {
            String location = response.getHeaders().getFirstHeaderValue("Location");
            return read(new ResourceHandle(URI.create(location)), types);
        }
        else {
            return handle(handle, response);
        }
    }

    public ResourceHandle create(ResourceHandle handle, Payload payload) {
        Validate.notNull(handle, "Handle may not be null");
        Validate.notNull(payload, "Payload may not be null");
        HTTPRequest request = new HTTPRequest(handle.getURI(), HTTPMethod.POST);
        request = request.challenge(challenge);
        request = request.payload(payload);
        HTTPResponse response = cache.doCachedRequest(request);
        if (response.getStatus() != Status.CREATED) {
            throw new RESTException(handle.getURI(), response.getStatus());
        }
        return new ResourceHandle(URI.create(response.getHeaders().getFirstHeaderValue("Location")), Option.<Tag>none());
    }

    public <T> Option<Resource<T>> read(ResourceHandle handle) {
        return read(handle, Collections.<MIMEType>emptyList());
    }

    public <T> Option<Resource<T>> read(ResourceHandle handle, List<MIMEType> types) {
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
        HTTPResponse response = cache.doCachedRequest(request);
        ResourceHandle updatedHandle = new ResourceHandle(handle.getURI(), fromNull(response.getETag()));
        if (updatedHandle.equals(handle) && response.getStatus() == Status.NOT_MODIFIED) {
            return Option.none();
        }
        if (response.getStatus() == Status.OK) {
            return handle(updatedHandle, response);
        }
        throw new RESTException(handle.getURI(), response.getStatus());
    }

    protected <T> Option<Resource<T>> handle(ResourceHandle handle, HTTPResponse response) {
        if (response.hasPayload()) {
            for (Handler<T> handler : getHandlers()) {
                if (handler.supports(response.getPayload().getMimeType())) {
                    Resource<T> some = DefaultResource.create(handle, response.getHeaders(), handler.handle(response.getPayload()));
                    return Option.some(some);
                }
            }
        }
        return none();
    }   
}
