package entities.mongodb;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import entities.Session;
import entities.User;
import org.bson.types.ObjectId;
import utils.Generator;
import utils.SimplePBKDF2;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Entity(value = "users", useDiscriminator = false)
public class MongoDbUser implements User {
    @Id
    private ObjectId _id;

    @Indexed
    private String email;

    private String name;

    private String passwordSalt;

    private String passwordHash;

    private List<MongoDbSession> sessions = new LinkedList<>();

    public MongoDbUser() {
        // constructor for Morphia
    }

    public MongoDbUser(String email, String name, String password) {
        this.email = email;
        this.name = name;
        setPassword(password);
    }

    public void setPassword(String password) {
        passwordSalt = Generator.generatePasswordSaltHex();
        passwordHash = SimplePBKDF2.hash(passwordSalt, password);
    }

    public ObjectId getObjectId() {
        return _id;
    }

    @Override
    public String getId() {
        return _id.toString();
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public List<? extends Session> getSessions() {
        return sessions;
    }

    public MongoDbSession startSession() {
        MongoDbSession mongoDbSession = new MongoDbSession();
        sessions.add(mongoDbSession);
        return mongoDbSession;
    }

    public void killSessions() {
        sessions = Collections.emptyList();
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public boolean checkPassword(String password) {
        return SimplePBKDF2.hash(passwordSalt, password).equals(passwordHash);
    }

    @Override
    public String toString() {
        return "user:" + email;
    }
}
