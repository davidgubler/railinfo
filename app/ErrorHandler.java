import play.http.HttpErrorHandler;
import play.mvc.*;
import play.mvc.Http.*;
import utils.NotAllowedException;
import utils.NotFoundException;
import utils.RailinfoLogger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Singleton;

@Singleton
public class ErrorHandler implements HttpErrorHandler {
    private String extractCode(RequestHeader request) {
        try {
            String path = request.path();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            int pos = path.indexOf("/");
            if (pos > 0) {
                return path.substring(0, pos);
            }
            return path;
        } catch (Exception e) {
            return null;
        }
    }

    public CompletionStage<Result> onClientError(
            RequestHeader request, int statusCode, String message) {
        return CompletableFuture.completedFuture(Results.status(statusCode, views.html.error.render(request, message, null, extractCode(request))));
    }

    public CompletionStage<Result> onServerError(RequestHeader request, Throwable exception) {
        if (exception instanceof NotFoundException) {
            return CompletableFuture.completedFuture(Results.notFound(views.html.error.render(request, "404: " + exception.getMessage(), null, extractCode(request))));
        }
        if (exception instanceof NotAllowedException) {
            return CompletableFuture.completedFuture(Results.forbidden(views.html.error.render(request, "403: " + exception.getMessage(), null, extractCode(request))));
        }
        RailinfoLogger.error(request, exception);
        return CompletableFuture.completedFuture(Results.internalServerError(views.html.error.render(request, "500: Whoops, something went wrong", null, extractCode(request))));
    }
}