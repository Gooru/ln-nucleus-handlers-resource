package org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.dbhandlers.helpers;

import java.net.MalformedURLException;
import java.net.URL;

import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.converters.FieldConverter;
import org.gooru.nucleus.handlers.resources.processors.repositories.activejdbc.entities.AJEntityOriginalResource;
import org.gooru.nucleus.handlers.resources.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.resources.processors.responses.MessageResponseFactory;

import io.vertx.core.json.JsonObject;

/**
 * @author ashish on 1/11/16.
 */
public final class ResourceUrlHelper {

    private ResourceUrlHelper() {
        throw new AssertionError();
    }

    public static ExecutionResult<MessageResponse> handleUrl(AJEntityOriginalResource resource, JsonObject request) {
        if (request.getBoolean(AJEntityOriginalResource.IS_REMOTE, true)) {
            try {
                UrlAnalyzer analyzer = new DefaultUrlAnalyzer(request.getString(AJEntityOriginalResource.URL));
                resource.setString(AJEntityOriginalResource.HTTP_HOST, analyzer.getHost());
                resource.setString(AJEntityOriginalResource.HTTP_PATH, analyzer.getPath());
                resource.setString(AJEntityOriginalResource.HTTP_QUERY, analyzer.getQueryParams());
                resource.set(AJEntityOriginalResource.HTTP_PROTOCOL, FieldConverter
                    .convertFieldToNamedType(analyzer.getProtocol(), AJEntityOriginalResource.HTTP_PROTOCOL_TYPE));
                resource.setInteger(AJEntityOriginalResource.HTTP_PORT, analyzer.getPort());
                resource.setString(AJEntityOriginalResource.HTTP_DOMAIN, analyzer.getDomain());
                resource.setString(AJEntityOriginalResource.HTTP_FRAGMENT, analyzer.getFragment());
                return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
            } catch (MalformedURLException e) {
                return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Unable to parse URL"),
                    ExecutionResult.ExecutionStatus.FAILED);
            }
        } else {
            return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
        }
    }

    interface UrlAnalyzer {
        String getProtocol();

        String getHost();

        Integer getPort();

        String getDomain();

        String getPath();

        String getQueryParams();
        
        String getFragment();
    }

    static class DefaultUrlAnalyzer implements UrlAnalyzer {

        final URL url;

        DefaultUrlAnalyzer(String url) throws MalformedURLException {
            this.url = new URL(url);
        }

        @Override
        public String getProtocol() {
            return url.getProtocol().toLowerCase();
        }

        @Override
        public String getHost() {
            return url.getHost().toLowerCase();
        }

        @Override
        public Integer getPort() {
            return url.getPort();
        }

        @Override
        public String getDomain() {
            String domain = getHost();
            if (domain.startsWith("www.") || domain.startsWith("WWW.")) {
                domain = domain.substring(4);
            }
            if (getPort() != 80 && getPort() != 443 && getPort() != -1) {
                domain = domain + ':' + String.valueOf(getPort());
            }
            return domain.toLowerCase();
        }

        @Override
        public String getPath() {
            return url.getPath() != null ? url.getPath().toLowerCase() : null;
        }

        @Override
        public String getQueryParams() {
            return url.getQuery() != null ? url.getQuery().toLowerCase() : null;
        }

        @Override
        public String getFragment() {
            return url.getRef() != null ? url.getRef().toLowerCase() : null;
        }
    }
}
