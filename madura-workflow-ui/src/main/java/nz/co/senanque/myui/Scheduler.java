/**
 * 
 */
package nz.co.senanque.myui;

import nz.co.senanque.workflow.Executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Roger Parkinson
 *
 */
@Component
@Conditional(ScheduleCondition.class)
public class Scheduler {
	
	@Autowired Executor m_executor;

	@Scheduled(fixedDelayString="${nz.co.senanque.myui.Scheduler.activeProcesses:10000}")
	public void activeProcesses() {
		m_executor.activeProcesses();
	}
	@Scheduled(fixedDelayString="${nz.co.senanque.myui.Scheduler.deferredEvents:10000}")
	public void deferredEvents() {
		m_executor.deferredEvents();
	}
	@Scheduled(fixedDelayString="${nz.co.senanque.myui.Scheduler.clearDeferredEvents:60000}")
	public void clearDeferredEvents() {
		m_executor.clearDeferredEvents();
	}
}
