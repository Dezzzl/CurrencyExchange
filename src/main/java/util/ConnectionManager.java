package util;


import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class ConnectionManager {
    private static final Integer DEFAULT_POOL_SIZE = 5;
    private static final String NAME_KEY = "db.name";
    private static BlockingQueue<Connection> pool;
    private static List<Connection> sourceConnections;

    static {
        loadDriver();
//        initConnectionPull();
    }

    public static Connection open() throws SQLException {
            //  return DriverManager.getConnection(PropertiesUtil.get(PATH_KEY));
            return DriverManager.getConnection("jdbc:sqlite:"+getPath());
    }

//    private static void initConnectionPull() {
//        String poolSize = PropertiesUtil.get("db.pool.size");
//        int size = poolSize == null ? DEFAULT_POOL_SIZE : Integer.parseInt(poolSize);
//        pool = new ArrayBlockingQueue<>(size);
//        sourceConnections = new ArrayList<>(size);
//        for (int i = 0; i < size; i++) {
//            var connection = open();
//            var proxyConnection = (Connection) Proxy.newProxyInstance(ConnectionManager.class.getClassLoader(), new Class[]{Connection.class},
//                    (proxy, method, args) -> method.getName().equals("close")
//                            ? pool.add((Connection) proxy)
//                            : method.invoke(connection, args));
//            pool.add(proxyConnection);
//            sourceConnections.add(connection);
//        }
//    }

//    public static Connection get() {
//        try {
//            return pool.take();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }



    private static void loadDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

//    public static void closePool() {
//        try {
//            for (Connection sourceConnection : sourceConnections) {
//                sourceConnection.close();
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private ConnectionManager() {
    }

    private static String getPath(){
        URL url = ConnectionManager.class.getClassLoader().getResource(PropertiesUtil.get(NAME_KEY));
        File file = new File(url.getFile());
        return file.getAbsolutePath();
    }
}
