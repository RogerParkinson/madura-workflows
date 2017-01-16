/**
 * 
 */
package nz.co.senanque.workflowimpl;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author Roger Parkinson
 *
 */
public class Main {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		ApplicationContext ctx = new AnnotationConfigApplicationContext(nz.co.senanque.workflowimpl.SpringConfig.class);
		while (true) {
			Thread.sleep(1000);
		}
	}

}
