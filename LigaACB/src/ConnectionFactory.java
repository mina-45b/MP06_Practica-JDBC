import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Clase ConnectionFactory proporciona una única conexión a la base de datos utilizando el patrón Singleton
 * Se encarga de cargar la configuración de la base de datos desde el archivo db.properties y de gestionar la conexión
 */
public class ConnectionFactory {
	// Constantes para la configuración de la base de datos
	private static final String DATABASE_DRIVER = "org.postgresql.Driver";

	private static final String MAX_POOL = "250";
	private static ConnectionFactory instance;

	// Variables de instancia para la configuración de la base de datos
	private String dbname;
	private String host;
	private String port;
	private String user;
	private String password;
	private String schema;

	// Objeto de conexión a la base de datos
	private Connection connection;

	// Objeto de propiedades para la configuración de la conexión
	private Properties properties;

	/**
	 * Constructor privado para implementar el patrón Singleton.
	 * Inicializa la configuración de la base de datos al llamar al método init().
	 */
	private ConnectionFactory() {
		super();
		init();
	}

	/**
	 * Método estático que devuelve la única instancia de ConnectionFactory utilizando el patrón Singleton.
	 * @return La única instancia de ConnectionFactory.
	 */
	public static ConnectionFactory getInstance() {
		if (instance == null) {
			instance = new ConnectionFactory();
		}
		return instance;
	}
	
	/**
	 * Initializes the class loading the database properties file and assigns values
	 * to the instance variables
	 * @throws RuntimeException Properties file could not be found.
	 */
	public void init() {
		Properties prop = new Properties();
		InputStream propStream = this.getClass().getClassLoader().getResourceAsStream("db.properties");

		try {
			prop.load(propStream);
			this.host = prop.getProperty("host");
			this.port = prop.getProperty("port");
			this.user = prop.getProperty("user");
			this.password = prop.getProperty("password");
			this.dbname = prop.getProperty("dbname");
			this.schema = prop.getProperty("schema");
		} catch (IOException e) {
			String message = "ERROR: db.properties file could not be found";
			System.err.println(message);
			throw new RuntimeException(message, e);
		}
	}

	/**
	 * Obtiene las propiedades de conexión, creándolas si aún no existen.
	 * @return Las propiedades de conexión.
	 */
	private Properties getProperties() {
		if (properties == null) {
			properties = new Properties();
			properties.setProperty("user", this.user);
			properties.setProperty("password", this.password);
			properties.setProperty("MaxPooledStatements", MAX_POOL);
		}
		return properties;
	}

	/**
	 * Establece una conexión a la base de datos y la devuelve.
	 * Si la conexión ya está establecida, simplemente la devuelve.
	 * @return La conexión a la base de datos.
	 */
	public Connection connect() {
		if (connection == null) {

			try {
				String url = null;

				Class.forName(DATABASE_DRIVER);
				
				// Preprara connexió a la base de dades
				StringBuffer sbUrl = new StringBuffer();
				sbUrl.append("jdbc:postgresql:");
				if (host != null && !host.equals("")) {
					sbUrl.append("//").append(host);
					if (port != null && !port.equals("")) {
						sbUrl.append(":").append(port);
					}
				}
				sbUrl.append("/").append(dbname);
				url = sbUrl.toString();
				
				System.out.println(url);
				System.out.println(getProperties());
						
				connection = DriverManager.getConnection(url, getProperties());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return connection;
	}

	/**
	 * Desconecta la base de datos cerrando la conexión si está abierta.
	 */
	public void disconnect() {
		if (connection != null) {
			try {
				connection.close();
				connection = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	
}
