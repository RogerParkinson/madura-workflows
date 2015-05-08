package nz.co.senanque.workflow;

import java.util.Map;

import nz.co.senanque.process.instances.ComputeType;
import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflow.nmcinstances.NMC;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Roger Parkinson
 *
 */
public class HCAUnreserve implements ComputeType<NMC> {

	private static final Logger log = LoggerFactory
			.getLogger(HCAUnreserve.class);
	public void execute(ProcessInstance processInstance, NMC context, Map<String, String> map) {
		// does nothing in a demo
	}

}
