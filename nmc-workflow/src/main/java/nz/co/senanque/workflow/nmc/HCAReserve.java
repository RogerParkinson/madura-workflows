package nz.co.senanque.workflow.nmc;

import java.util.Map;

import nz.co.senanque.process.instances.ComputeType;
import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflow.nmcinstances.NMC;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Roger Parkinson
 *
 */
@Component("HCAReserve")
public class HCAReserve implements ComputeType<NMC> {

	private static final Logger log = LoggerFactory.getLogger(HCAReserve.class);

	public void execute(ProcessInstance processInstance, NMC context,
			Map<String, String> map) {
		if (context.getClaimId().startsWith("1")) {
			context.setReservationId("1234");
		}

	}
}
