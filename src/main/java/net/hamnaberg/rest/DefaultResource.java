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

import fj.data.Option;
import org.apache.commons.lang.Validate;
import org.codehaus.httpcache4j.Headers;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class DefaultResource<R> implements Resource<R> {
    private final ResourceHandle handle;
    private final Headers headers;
    private final R data;

    private DefaultResource(ResourceHandle handle, Headers headers, R data) {
        Validate.notNull(handle, "Resource Handle may not be null");
        Validate.notNull(headers, "Headers may not be null");
        this.headers = headers;
        this.handle = handle;
        this.data = data;
    }

    public ResourceHandle getResourceHandle() {
        return handle;
    }

    public Headers getHeaders() {
        return headers;
    }

    public Option<R> getData() {
        if (data == null) {
            return Option.none();
        }
        return Option.some(data);
    }

    public static <R> DefaultResource<R> create(ResourceHandle handle, Headers headers, R data) {
        return new DefaultResource<R>(handle, headers, data);
    }
}
