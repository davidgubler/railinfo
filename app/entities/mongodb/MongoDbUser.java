package entities.mongodb;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import entities.User;
import org.bson.types.ObjectId;
import utils.Generator;
import utils.SimplePBKDF2;

@Entity(value = "users", noClassnameStored = true)
public class MongoDbUser implements User {
    @Id
    private ObjectId _id;

    @Indexed
    private String email;

    private String name;

    private String passwordSalt;

    private String passwordHash;

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
}
