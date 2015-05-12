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
package nz.co.senanque.workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import nz.co.senanque.validationengine.ValidationEngine;
import nz.co.senanque.validationengine.ValidationSession;
import nz.co.senanque.workflow.nmcinstances.NMC;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class RulesTest
{
    private static final Logger log = LoggerFactory.getLogger(RulesTest.class);
    @Autowired private transient ValidationEngine m_validationEngine;

    @Test
    public void verifyRules() throws Exception
    {        
        ValidationSession validationSession = m_validationEngine.createSession();
        NMC nmc = new NMC();
        validationSession.bind(nmc);
        nmc.setReservationId("none");
        nmc.setSameClaim(false);
        assertEquals("rejected",nmc.getStatus());
        assertFalse(nmc.isHasFunds());
        nmc.setReservationId("1234");
        assertTrue(nmc.isHasFunds());
        assertNull(nmc.getStatus());
        nmc.setSameClaim(true);
        nmc.setSameAmount(false);
        assertTrue(nmc.isLoop());
        nmc.setSameClaim(true);
        nmc.setSameAmount(true);
        assertFalse(nmc.isLoop());
        assertFalse(nmc.isRejected());
        assertEquals("approved",nmc.getStatus());
        validationSession.close();
    }
}
