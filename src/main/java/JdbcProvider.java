import java.security.AlgorithmConstraints;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcProvider {

    public static final String URL = "jdbc:postgresql://localhost:5432/schoolmenu";
    public static final String USERNAME = "postgres";
    public static final String PASSWORD = "Hello2015";
    public static final String JDBC_POSTGRES_DRIVER = "org.postgresql.Driver";

    private Connection connection;

    private final Algorithm algorithm = new Algorithm(getConnection());
    private final RightMenu rightMenu = new RightMenu(getConnection());
    private final AlgorithmForPupils algorithmForPupils = new AlgorithmForPupils(getConnection());

    public JdbcProvider() {
        try {
            Class.forName(JDBC_POSTGRES_DRIVER);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection()  {
        if (connection != null) {
            return connection;
        }

        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connection;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public RightMenu getRightMenu() {
        return rightMenu;
    }

    public AlgorithmForPupils getAlgorithmForPupils() {
        return algorithmForPupils;
    }
}
