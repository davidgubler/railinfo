import entities.User;
import play.http.HttpErrorHandler;
import play.mvc.*;
import play.mvc.Http.*;
import utils.NotAllowedException;
import utils.NotFoundException;

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
            Results.notFound(views.html.error.render(request, exception.getMessage(), null));
        }
        if (exception instanceof NotAllowedException) {
            Results.notFound(views.html.error.render(request, exception.getMessage(), null));
        }
        return CompletableFuture.completedFuture(Results.internalServerError(views.html.error.render(request, exception.getMessage(), null)));
    }
}