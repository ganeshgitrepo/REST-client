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

package net.hamnaberg.rest.spi;

import net.hamnaberg.rest.Handler;

public abstract class HandlerSpi<T> {
    private final String vendor;
    private final String name;
    private final String versions;

    public HandlerSpi(String vendor, String name, String versions) {
        this.vendor = vendor;
        this.name = name;
        this.versions = versions;
    }

    public abstract Handler<T> createHandler();
}
