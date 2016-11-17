import io.undertow.Undertow;
import io.undertow.server.*;
import io.undertow.server.handlers.*;
import io.undertow.util.Headers;
import java.sql.*;
import java.util.*;

public class Server {
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_NAME = "phase2";

    private static final String URL = "jdbc:mysql://ec2-54-165-15-232.compute-1.amazonaws.com:3306/" + DB_NAME +"?useUnicode=true&characterEncoding=utf-8&autoReconnectForPools=true";

    private static final String DB_USER = "root";
    private static final String DB_PWD = "12345678";

    @SuppressWarnings("finally")
	private static String query(Connection conn,String userid,String hashtag,int mod) {
        Statement stmt = null;
        String result="";

        try {
            stmt = conn.createStatement();
            String sql = "SET NAMES utf8mb4";
            stmt.executeUpdate(sql);

            String tableName="twitter"+mod;
            
            sql = "SELECT sentimental_density,post_time,twitter_id,censored_text FROM "+tableName+
            	" WHERE user_id='"+userid+
            	"' AND hash_tag='"+hashtag+"'"+
            	" ORDER BY sentimental_density DESC,post_time ASC,twitter_id ASC";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
            	result+=String.format("%.3f",rs.getDouble(1))+":"+rs.getString(2)+":"+rs.getString(3)+":"+rs.getString(4)+"\n";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(result.equals(""))
            	result="\n\n";
            return result;
        }
    }

    public static void main(String[] args) {
    	PathHandler path = new PathHandler()
    		.addPrefixPath("create",new HttpHandler() {
    			public void handleRequest(final HttpServerExchange exchange) throws Exception {
					exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
					if (exchange.isInIoThread()) {
		              exchange.dispatch(this);
		              return;
		            }
					
                    Map<String, Deque<String>> parameters = exchange.getQueryParameters();

                    Deque<String> q1 = parameters.get("name");
                    String name = (String)q1.poll();

                    Deque<String> q2 = parameters.get("secret");
                    String secret = (String)q2.poll();

					exchange.getResponseSender().send(name + "\n" + secret);
                }
    		})
    		.addPrefixPath("update",new HttpHandler(){
    			public void handleRequest(final HttpServerExchange exchange) throws Exception {
					exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");

					if (exchange.isInIoThread()) {
		              exchange.dispatch(this);
		              return;
		            }
                }
    		})
    		.addPrefixPath("delete",new HttpHandler(){
    			public void handleRequest(final HttpServerExchange exchange) throws Exception {
					exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");

					if (exchange.isInIoThread()) {
		              exchange.dispatch(this);
		              return;
		            }
                }
    		});
        Undertow server = Undertow.builder()
                .addHttpListener(80, "ec2-52-207-220-237.compute-1.amazonaws.com")
                .setHandler(path)
                .build();
        server.start();
    }
}
