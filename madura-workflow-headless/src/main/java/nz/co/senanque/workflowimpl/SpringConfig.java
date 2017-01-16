/**
 * 
 */
package nz.co.senanque.workflowimpl;

import nz.co.senanque.locking.LockFactory;
import nz.co.senanque.locking.sql.SQLLockFactory;
import nz.co.senanque.madura.bundle.BundleExport;
import nz.co.senanque.madura.bundle.spring.BundledInterfaceRegistrar;
import nz.co.senanque.workflow.WorkflowDAO;
import nz.co.senanque.workflow.WorkflowJPA;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.atomikos.jdbc.AtomikosDataSourceBean;

/**
 * @author Roger Parkinson
 *
 */
@Configuration
@ImportResource("classpath:applicationContext.xml")
@EnableScheduling
@Import(BundledInterfaceRegistrar.class)
@ComponentScan(basePackages = {
//		"nz.co.senanque.vaadin",			// madura-vaadin
		"nz.co.senanque.validationengine",	// madura-objects
		"nz.co.senanque.workflowui.conf"})
@PropertySource("classpath:config.properties")
public class SpringConfig {

	@Autowired MessageSource messageSource;
	@Autowired AtomikosDataSourceBean atomikosDataSourceBean;
	// needed for @PropertySource
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	@Bean(name="workflowDAO")
	@BundleExport
	public WorkflowDAO getWorkflowDAO() {
		return new WorkflowJPA();
	}
	@Bean(name="lockFactory")
	@BundleExport
	public LockFactory getLockFactory() {
//		return new SimpleLockFactory();
		SQLLockFactory ret = new SQLLockFactory();
		ret.setDataSource(atomikosDataSourceBean);
		return ret;
	}

}
