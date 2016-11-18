import io.undertow.Undertow;
import io.undertow.server.*;
import io.undertow.server.handlers.*;
import io.undertow.util.Headers;
import java.sql.*;
import java.util.*;
import java.sql.*;

public class Server {
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final String URL = "jdbc:mysql://localhost:3306/secrets?user=root&password=123456&useUnicode=true&characterEncoding=utf-8&autoReconnectForPools=true";

	private static Connection getConnection() {
		Connection conn = null;

		try {
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(URL);
		} catch (SQLException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return conn;
	}

	private static String doCreate(String name, String secret) {
		Statement stmt = null;
		Connection conn = getConnection();

		try {
			stmt = conn.createStatement();
			String sql = "INSERT INTO secrets_table(user_name, secrets_content) VALUES('" + name + "', '" + secret
					+ "')";
			System.out.println(sql);
			stmt.executeUpdate(sql);
			return "create secret succeeds";
		} catch (SQLException e) {
			e.printStackTrace();
			return "create secret fails";
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		PathHandler path = new PathHandler().addPrefixPath("create", new HttpHandler() {
			public void handleRequest(final HttpServerExchange exchange) throws Exception {
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
				if (exchange.isInIoThread()) {
					exchange.dispatch(this);
					return;
				}

				Map<String, Deque<String>> parameters = exchange.getQueryParameters();
				Deque<String> q1 = parameters.get("name");
				String name = (String) q1.poll();
				Deque<String> q2 = parameters.get("secret");
				String secret = (String) q2.poll();

				String response = doCreate(name, secret);

				exchange.getResponseSender().send(response);
			}
		}).addPrefixPath("view", new HttpHandler() {
			public void handleRequest(final HttpServerExchange exchange) throws Exception {
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");

				if (exchange.isInIoThread()) {
					exchange.dispatch(this);
					return;
				}
			}
		}).addPrefixPath("update", new HttpHandler() {
			public void handleRequest(final HttpServerExchange exchange) throws Exception {
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");

				if (exchange.isInIoThread()) {
					exchange.dispatch(this);
					return;
				}
			}
		}).addPrefixPath("delete", new HttpHandler() {
			public void handleRequest(final HttpServerExchange exchange) throws Exception {
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");

				if (exchange.isInIoThread()) {
					exchange.dispatch(this);
					return;
				}
			}
		});

		Undertow server = Undertow.builder().addHttpListener(80, "ec2-52-207-220-237.compute-1.amazonaws.com")
				.setHandler(path).build();
		server.start();
	}
}
