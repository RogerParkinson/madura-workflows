/**
 * 
 */
package nz.co.senanque.workflowimpl;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author Roger Parkinson
 *
 */
public class ScheduleCondition implements Condition {

	/* (non-Javadoc)
	 * @see org.springframework.context.annotation.Condition#matches(org.springframework.context.annotation.ConditionContext, org.springframework.core.type.AnnotatedTypeMetadata)
	 */
	public boolean matches(ConditionContext context,
			AnnotatedTypeMetadata metadata) {
		String p = context.getEnvironment().getProperty("nz.co.senanque.workflowimpl.ScheduleCondition");
		if ("off".equals(p)) {
			return false;
		}
		return true;
	}

}
