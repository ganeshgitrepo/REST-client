package net.hamnaberg.rest;

import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.Status;

import java.net.URI;

public class Failure {
    private final URI uri;
    private final Status status;
    private final Headers headers;


    public Failure(URI uri, Status status, Headers headers) {
        this.uri = uri;
        this.status = status;
        this.headers = headers;
    }

    public URI getUri() {
        return uri;
    }

    public Status getStatus() {
        return status;
    }

    public Headers getHeaders() {
        return headers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Failure failure = (Failure) o;

        if (headers != null ? !headers.equals(failure.headers) : failure.headers != null) return false;
        if (status != null ? !status.equals(failure.status) : failure.status != null) return false;
        if (uri != null ? !uri.equals(failure.uri) : failure.uri != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uri != null ? uri.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (headers != null ? headers.hashCode() : 0);
        return result;
    }
}
