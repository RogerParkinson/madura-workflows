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
package nz.co.senanque.process.parser;

import java.util.Set;

import nz.co.senanque.parser.AbstractTextProvider;
import nz.co.senanque.parser.ParserException;
import nz.co.senanque.parser.ParserSource;
import nz.co.senanque.parser.TOCInterface;
import nz.co.senanque.process.instances.ProcessDefinition;
import nz.co.senanque.process.instances.QueueDefinition;
import nz.co.senanque.schemaparser.SchemaParser;
import nz.co.senanque.workflow.WorkflowManager;

/**
 * 
 * Short description
 * 
 * @author Roger Parkinson
 * @version $Revision: 1.4 $
 */
public class ProcessTextProvider extends AbstractTextProvider
{
    private final SchemaParser m_schemaParser;
    private String m_currentScope;
    private final WorkflowManager m_workflowManager;
    private int m_processCount=0;
    private final StringBuilder m_accumulate = new StringBuilder();
	private String m_packageName;
	private TOCInterface m_toc;
    
    public ProcessTextProvider(ParserSource parserSource, SchemaParser schemaParser, WorkflowManager workflowManager) throws ParserException
    {
        super(parserSource);
        m_workflowManager = workflowManager;
        m_schemaParser = schemaParser;
    }
    public SchemaParser getSchemaParser()
    {
        return m_schemaParser;
    }
    public String getCurrentScope()
    {
        return m_currentScope;
    }
    public void setCurrentScope(String currentScope)
    {
        m_currentScope = currentScope;
    }
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (ProcessDefinition pd:m_workflowManager.getMainProcesses())
        {
            sb.append(pd.toString());
        }
        return sb.toString();
    }
    public int incrementProcessCount()
    {
        return ++m_processCount;
    }
    public Set<ProcessDefinition> getProcesses()
    {
        return m_workflowManager.getMainProcesses();
    }
    public Set<QueueDefinition> getQueues()
    {
        return m_workflowManager.getQueues();
    }
    public void setLastToken(String lastToken)
    {
        super.setLastToken(lastToken);
        m_accumulate.append(lastToken);
    }
    public String getAccumulate()
    {
        String ret = m_accumulate.toString();
        m_accumulate.setLength(0);
        return ret;
    }
    public void accumulate(String string)
    {
        m_accumulate.append(string);
    }
	public WorkflowManager getWorkflowManager() {
		return m_workflowManager;
	}
	public void setPackageName(String packageName) {
		m_packageName = packageName+(packageName.endsWith(".")?"":".");
	}
	public String getPackageName() {
		return m_packageName;
	}
	public TOCInterface getToc() {
		return m_toc;
	}
	public void setToc(TOCInterface toc) {
		m_toc = toc;
	}
	public Object addTOCElement(Object parent, String name, long start,
			long end, int type) {
		if (m_toc != null) {
			return m_toc.addTOCElement(parent,name,start,end,type);
		}
		return null;
	}

}
