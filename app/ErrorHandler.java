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
    public CompletionStage<Result> onClientError(
            RequestHeader request, int statusCode, String message) {
        return CompletableFuture.completedFuture(Results.status(statusCode, views.html.error.render(request, message, null)));
    }

    public CompletionStage<Result> onServerError(RequestHeader request, Throwable exception) {
        if (exception instanceof NotFoundException) {
            return CompletableFuture.completedFuture(Results.notFound(views.html.error.render(request, "404: " + exception.getMessage(), null)));
        }
        if (exception instanceof NotAllowedException) {
            return CompletableFuture.completedFuture(Results.forbidden(views.html.error.render(request, "403: " + exception.getMessage(), null)));
        }
        RailinfoLogger.error(request, exception);
        return CompletableFuture.completedFuture(Results.internalServerError(views.html.error.render(request, "500: Whoops, something went wrong", null)));
    }
}