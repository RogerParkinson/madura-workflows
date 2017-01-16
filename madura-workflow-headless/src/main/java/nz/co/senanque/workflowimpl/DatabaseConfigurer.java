/**
 * 
 */
package nz.co.senanque.workflowimpl;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author Roger Parkinson
 *
 */
public class DatabaseConfigurer {

	@Value("${database.max.pool.size:50}")
	public int maxPoolSize;
	@Value("${database.min.pool.size:2}")
	public int minPoolSize;
	@Value("${database.borrow.connection.timeout:300}")
	public int borrowConnectionTimeout;
	@Value("${database.dialect:org.hibernate.dialect.H2Dialect}")
	public String dialect;
	@Value("${database.datasource.class:org.h2.jdbcx.JdbcDataSource}")
	public String datasourceClass;
	@Value("${database.url.prefix:jdbc:h2:mem:}")
	public String urlPrefix;
	@Value("${database.url.suffix:;DB_CLOSE_ON_EXIT=FALSE;MVCC=true}")
	public String urlsuffix;
	@Value("${database.user:abc}")
	public String user;
	@Value("${database.password:}")
	public String password;
	@Value("${database.type:H2}")
	public String dbType;
	private Properties extraProperties = new Properties();
	public Properties properties(String dbname) {
		Properties ret = new Properties();
		ret.put("url", urlPrefix+dbname+urlsuffix);
		ret.put("user", user);
		ret.put("password", password);
		ret.putAll(extraProperties);
		return ret;
	}
	public Properties getExtraProperties() {
		return extraProperties;
	}
	public void setExtraProperties(Properties extraProperties) {
		this.extraProperties = extraProperties;
	}
}
