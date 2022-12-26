package models.mongodb;

import com.google.inject.Inject;
import com.google.inject.Injector;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;
import entities.User;
import entities.mongodb.MongoDbUser;
import models.UsersModel;
import org.bson.types.ObjectId;
import services.MongoDb;
import utils.Config;

import java.util.List;

public class MongoDbUsersModel implements UsersModel {

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
            System.out.println("Updating password!");
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
}
