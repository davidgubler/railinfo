package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;
import entities.Session;
import entities.User;
import entities.mongodb.MongoDbSession;
import entities.mongodb.MongoDbUser;
import models.UsersModel;
import org.bson.types.ObjectId;
import play.mvc.Http;
import services.MongoDb;
import utils.Config;
import utils.InputUtils;

import java.util.*;
import java.util.stream.Collectors;

public class MongoDbUsersModel implements UsersModel {

    private static final long LAST_ACTIVE_REFRESH_MS = 1000L * 60L * 5L;

    @Inject
    private Injector injector;

    @Inject
    private MongoDb mongoDb;

    private Query<MongoDbUser> query() {
        return mongoDb.getDs(Config.GLOBAL_DB).createQuery(MongoDbUser.class);
    }

    private Query<MongoDbUser> query(User user) {
        return query().field("_id").equal(((MongoDbUser)user).getObjectId());
    }

    private UpdateOperations<MongoDbUser> ops() {
        return mongoDb.getDs(Config.GLOBAL_DB).createUpdateOperations(MongoDbUser.class);
    }

    @Override
    public List<? extends User> getAll() {
        return query().asList();
    }

    @Override
    public User getByEmailAndPassword(String email, String password) {
        User user = query().field("email").equal(email).first();
        if (user == null) {
            return null;
        }
        if (!user.checkPassword(password)) {
            return null;
        }
        return user;
    }

    @Override
    public User get(String id) {
        return query().field("_id").equal(new ObjectId(id)).first();
    }

    @Override
    public User getFromRequest(Http.Request request) {
        Http.Cookie sessionIdCookie = request.cookies().get("sessionId").orElse(null);
        if (sessionIdCookie == null) {
            return null;
        }
        String sessionId = InputUtils.trimToNull(sessionIdCookie.value());
        if (sessionId == null) {
            return null;
        }
        User user = query().field("sessions.sessionId").equal(sessionId).first();
        if (user == null) {
            return null;
        }
        String method = request.method();
        if (!"GET".equals(method) && !"HEAD".equals(method)) {
            // We require the csrfToken to be sent as a form value whenever methods other than GET or HEAD are used
            Http.Cookie csrfTokenCookie = request.cookies().get("csrfToken").orElse(null);
            if (csrfTokenCookie == null) {
                return null;
            }
            String cookieCsrfToken = InputUtils.trimToNull(csrfTokenCookie.value());
            String formCsrfToken = InputUtils.trimToNull(request.body().asFormUrlEncoded().get("csrfToken"));
            if (cookieCsrfToken == null || formCsrfToken == null || !formCsrfToken.equals(cookieCsrfToken)) {
                return null;
            }
        }
        Session session = user.getSessions().stream().filter(s -> s.getSessionId().equals(sessionId)).collect(Collectors.toList()).get(0);
        if (session.getLastActive().getTime() < (new Date().getTime() - LAST_ACTIVE_REFRESH_MS)) {
            ((MongoDbSession)session).setLastActive(new Date());
            UpdateOperations<MongoDbUser> ops = ops().set("sessions.$.lastActive", session.getLastActive());
            mongoDb.getDs(Config.GLOBAL_DB).update(query().field("sessions.sessionId").equal(sessionId), ops);
        }
        return user;
    }

    @Override
    public User create(String email, String name, String password) {
        MongoDbUser mongoDbUser = new MongoDbUser(email, name, password);
        mongoDb.getDs(Config.GLOBAL_DB).save(mongoDbUser);
        return mongoDbUser;
    }

    @Override
    public void update(User user, String email, String name, String password) {
        MongoDbUser mongoDbUser = (MongoDbUser)user;
        UpdateOperations<MongoDbUser> ops = ops();
        mongoDbUser.setEmail(email);
        ops.set("email", mongoDbUser.getEmail());
        mongoDbUser.setName(name);
        ops.set("name", mongoDbUser.getName());
        if (password != null) {
            mongoDbUser.setPassword(password);
            ops.set("passwordSalt", mongoDbUser.getPasswordSalt());
            ops.set("passwordHash", mongoDbUser.getPasswordHash());
        }
        mongoDb.getDs(Config.GLOBAL_DB).update(query(mongoDbUser), ops);
    }

    @Override
    public void delete(User user) {
        mongoDb.getDs(Config.GLOBAL_DB).delete(query(user));
    }

    @Override
    public void startSession(User user) {
        MongoDbUser mongoDbUser = ((MongoDbUser)user);
        MongoDbSession mongoDbSession = mongoDbUser.startSession();
        UpdateOperations<MongoDbUser> ops = ops();
        if (mongoDbUser.getSessions().size() == 1) {
            ops.set("sessions", Arrays.asList(mongoDbSession));
        } else {
            ops.push("sessions", mongoDbSession);
        }
        mongoDb.getDs(Config.GLOBAL_DB).update(query(mongoDbUser), ops);
    }

    @Override
    public void killSessions(User user) {
        MongoDbUser mongoDbUser = ((MongoDbUser)user);
        mongoDbUser.killSessions();
        UpdateOperations<MongoDbUser> ops = ops().unset("sessions");
        mongoDb.getDs(Config.GLOBAL_DB).update(query(mongoDbUser), ops);
    }
}
