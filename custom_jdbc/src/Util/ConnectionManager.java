package Util;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class ConnectionManager {
    private static final String URL_KEY = "db.url";
    private static final String USERNAME_KEY = "db.username";
    private static final String PASSWORD_KEY = "db.password";
    private static final String POOL_SIZE_KEY = "db.poolSize";
    private static final Integer DEFAULT_POOL_SIZE = 10;
    private static BlockingQueue<Connection> pool;

    static {
        initConnectionPool();
    }

    private static void initConnectionPool() {
       var poolSize = PropertiesUtil.get(POOL_SIZE_KEY);
       var size = poolSize == null ?  DEFAULT_POOL_SIZE : Integer.parseInt(poolSize);
       pool = new ArrayBlockingQueue<>(size);
       for (int i = 0; i < size; i++) {
           var connection = open();
           Proxy.newProxyInstance(ConnectionManager.class.getClassLoader(), new Class[]{Connection.class}, // - доавление прокси на объект и замена его свойств
                   ((proxy, method, args) -> method.getName().equals("close")
                   ? pool.add((Connection) proxy) : method.invoke(connection, args)));
           pool.add(open());
       }
    }

    public static Connection get() {
        try {
            return pool.take();
        } catch (InterruptedException e){
            throw new RuntimeException();
        }
    }

    public static Connection open(){
        try {
            return DriverManager.getConnection(
                    PropertiesUtil.get(URL_KEY),
                    PropertiesUtil.get(USERNAME_KEY),
                    PropertiesUtil.get(PASSWORD_KEY));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
