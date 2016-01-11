package com.hazelcast.examples.application;

import com.hazelcast.examples.application.commands.Command;
import com.hazelcast.examples.application.dao.UserDao;
import com.hazelcast.examples.application.model.User;

import javax.cache.Cache;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Map;

/**
 * This class contains all context information to execute {@link com.hazelcast.examples.application.commands.Command}
 * implementations and allow them access to commands, cache, dao and shell input / output.
 */
@SuppressWarnings("unused")
public class Context {

    private final BufferedReader input;
    private final PrintStream output;
    private final UserDao userDao;
    private final Cache<Integer, User> userCache;
    private final Map<String, Command> commands;

    Context(InputStream in, OutputStream out, UserDao userDao, Cache<Integer, User> userCache, Map<String, Command> commands) {
        if (out instanceof PrintStream) {
            this.output = (PrintStream) out;
        } else {
            this.output = new PrintStream(out);
        }

        this.input = new BufferedReader(new InputStreamReader(in));
        this.userDao = userDao;
        this.userCache = userCache;
        this.commands = Collections.unmodifiableMap(commands);
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public Cache<Integer, User> getUserCache() {
        return userCache;
    }

    public Map<String, Command> getCommands() {
        return commands;
    }

    public void write(String string) {
        output.print(string);
    }

    public void writeln(String string) {
        output.println(string);
    }

    public void newLine() {
        output.println("");
    }

    public int readInt() throws IOException {
        return Integer.parseInt(read());
    }

    public double readDouble() throws IOException {
        return Double.parseDouble(read());
    }

    public String readLine() throws IOException {
        return read();
    }

    public boolean readBoolean() throws IOException {
        return Boolean.parseBoolean(read());
    }

    public int readUserId() throws IOException {
        write("UserId: ");
        return readInt();
    }

    public String readUsername() throws IOException {
        write("Username: ");
        return readLine();
    }

    public User readUser() throws IOException {
        int userId = readUserId();
        String username = readUsername();
        return new User(userId, username);
    }

    private String read() throws IOException {
        String line = input.readLine();
        return line != null ? line.trim() : null;
    }
}
