/*******************************************************************************
 * Copyright (c)2014 Prometheus Consulting
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
/*******************************************************************************
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package nz.co.senanque.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import nz.co.senanque.workflow.instances.ProcessInstance;
import nz.co.senanque.workflow.instances.TaskStatus;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Roger Parkinson
 * 
 * Writes some test data to the database.
 * The data is not necessarily internally consistent.
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class DatabaseLoadDAOImpl implements DatabaseLoadDAO {

	private static final Logger log = LoggerFactory
			.getLogger(DatabaseLoadDAOImpl.class);

	@PersistenceContext(unitName="pu-workflow")
	private EntityManager m_workflow;
	
	/* (non-Javadoc)
	 * @see nz.co.senanque.database.DatabaseLoadDAO#clear()
	 */
	@Transactional
	public int clear() throws Exception {
		  int deletedCount = m_workflow.createQuery("DELETE FROM nz.co.senanque.workflow.instances.ProcessInstance").executeUpdate();
		  log.info("deleted {} records",deletedCount);
		  return deletedCount;
	}
		
	/* (non-Javadoc)
	 * @see nz.co.senanque.database.DatabaseLoadDAO#load()
	 */
	@Transactional
	public void load() throws Exception {
			
		ProcessInstance pi = new ProcessInstance();
		pi.setComment("c1");
		pi.setLastUpdated(new Date());
		pi.setQueueName("Q1");
		pi.setStatus(TaskStatus.WAIT);
		pi.setProcessDefinitionName("P1");
		m_workflow.persist(pi);

		pi = new ProcessInstance();
		pi.setComment("c2");
		pi.setLastUpdated(new Date());
		pi.setQueueName("Q1");
		pi.setStatus(TaskStatus.WAIT);
		pi.setProcessDefinitionName("P2");
		m_workflow.persist(pi);

		pi = new ProcessInstance();
		pi.setComment("c3");
		pi.setLastUpdated(new Date());
		pi.setQueueName("Q1");
		pi.setStatus(TaskStatus.WAIT);
		pi.setProcessDefinitionName("P2");
		m_workflow.persist(pi);

		pi = new ProcessInstance();
		pi.setComment("c4");
		pi.setLastUpdated(new Date());
		pi.setQueueName("Q2");
		pi.setStatus(TaskStatus.WAIT);
		pi.setProcessDefinitionName("P3");
		m_workflow.persist(pi);
		m_workflow.flush();
	}
	
	/* (non-Javadoc)
	 * @see nz.co.senanque.database.DatabaseLoadDAO#query()
	 */
	@Override
	@Transactional(readOnly=true)
	public List<Long> query()
	{
		List<Long> ret = new ArrayList<Long>();
		Query query = m_workflow.createQuery("select x from nz.co.senanque.workflow.instances.ProcessInstance x");
		int size = 0;
		for (Object pi: query.getResultList()) {
			log.info("ProcessInstance id={}",((ProcessInstance)pi).getId());
			ret.add(((ProcessInstance)pi).getId());
			size++;
		}
		log.info("found {} records",size);
		return ret;
	}

}
