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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.co.senanque.messaging.MessageSender;
import nz.co.senanque.parser.Parser;
import nz.co.senanque.parser.ParserException;
import nz.co.senanque.parser.TextProvider;
import nz.co.senanque.process.instances.ComputeType;
import nz.co.senanque.process.instances.ProcessDefinition;
import nz.co.senanque.process.instances.QueueDefinition;
import nz.co.senanque.process.instances.TaskAbort;
import nz.co.senanque.process.instances.TaskBase;
import nz.co.senanque.process.instances.TaskCompute;
import nz.co.senanque.process.instances.TaskContinue;
import nz.co.senanque.process.instances.TaskDoFor;
import nz.co.senanque.process.instances.TaskDoUntil;
import nz.co.senanque.process.instances.TaskDoWhile;
import nz.co.senanque.process.instances.TaskEnd;
import nz.co.senanque.process.instances.TaskFork;
import nz.co.senanque.process.instances.TaskForm;
import nz.co.senanque.process.instances.TaskIf;
import nz.co.senanque.process.instances.TaskMessage;
import nz.co.senanque.process.instances.TaskRetry;
import nz.co.senanque.process.instances.TaskStart;
import nz.co.senanque.process.instances.TaskTry;
import nz.co.senanque.process.instances.TimeoutProvider;
import nz.co.senanque.process.instances.TimeoutProviderSimple;
import nz.co.senanque.schemaparser.FieldDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses the source processes, extends generic parser.
 * 
 * @author Roger Parkinson
 * @version $Revision: 1.11 $
 */

public final class ParsePackage extends Parser
{
    private static final Logger log = LoggerFactory.getLogger(ParsePackage.class);
	public static final int TYPE_PROCESS = 4;
	public static final int TYPE_QUEUE = 5;

    
	/**
	 * Constructor for ParsePackage.
	 */
	public ParsePackage()
	{
	}

	public void parse(TextProvider textProvider) {
		ProcessTextProvider ptp = (ProcessTextProvider) textProvider;

		while (true) {
        	textProvider.commit();
    		clearLeadingSpaces(textProvider);
			if (endOfData(textProvider))
				break;
			ptp.getAccumulate();
			if (exact("package",textProvider,false)) {
				if (!JavaClass(textProvider)) {
					throw new ParserException("package must have a package name: ", textProvider);
				}
				ptp.setPackageName(ptp.getLastToken());
				exactOrError(";",textProvider);
				commit(textProvider);
				continue;
			}
			if (exact("queue:",textProvider,false)) {
				processQueue(ptp);
				continue;
			}
			if (exact("process:", textProvider, false)) {
				processProcess(ptp);
				continue;
			}
			if (endOfData(textProvider))
				break;
			throw new ParserException("unexpected token: ", textProvider);
		}
	}

	private void processQueue(ProcessTextProvider textProvider) {
		 int start = textProvider.getPos();
		exactOrError("name",textProvider);
		exactOrError("=",textProvider);
		if (!quotedString('"',textProvider))
		{
			throw new ParserException("queue must have a name");
		}
		QueueDefinition queue = new QueueDefinition(textProvider.getLastToken());
        textProvider.addTOCElement(null, queue.getName(), start, textProvider.getPos(), TYPE_QUEUE);

		while (!nexact(";",true,textProvider)) {
			if (exact("permission", textProvider, false)) {
				exactOrError("=",textProvider);
				if (!quotedString('"',textProvider))
				{
					throw new ParserException("missing permission value");
				}
				queue.setPermission(textProvider.getLastToken());
			}
			if (exact("read-permission", textProvider, false)) {
				exactOrError("=",textProvider);
				if (!quotedString('"',textProvider))
				{
					throw new ParserException("missing read-permission value");
				}
				queue.setReadPermission(textProvider.getLastToken());
			}
		}
		textProvider.getQueues().add(queue);
		exactOrError(";",textProvider);
		commit(textProvider);
	}

	/**
	 * Method getProcessName. The process name is normally enclosed in quotes
	 * but it might be omitted in which case we generate a unique one.
	 * @return String
	 * @throws ParserException
	 */
    private String getProcessName(ProcessTextProvider textProvider)
    {
		if (!quotedString('"',textProvider))
		{
			return "P"+(textProvider.incrementProcessCount());
		}
    	if (textProvider.getLastToken().contains("_"))
    	{
    		throw new ParserException("Process names nust not include underbar: "+textProvider.getLastToken());
    	}
    	return textProvider.getLastToken();
    }
    private void processTimeout(TaskTry task, ProcessDefinition catchProcess, ProcessTextProvider textProvider) {
    	TimeoutProvider timeoutProvider = null;
    	
    	if (CVariable(textProvider)) {
    		timeoutProvider = catchProcess.getWorkflowManager().getTimeoutProvider(textProvider.getLastToken());
    	} else if (number(textProvider)) {
	    	int timeoutValue = Integer.parseInt(textProvider.getLastToken());
	    	if (exact("SECONDS",textProvider)) {
	    		timeoutValue *= 1000;
	    	} else if (exact("SECOND",textProvider)) {
	    		timeoutValue *= 1000;        		
	    	} else if (exact("MINUTES",textProvider)) {
	    		timeoutValue *= (1000*60);        		
	    	} else if (exact("MINUTE",textProvider)) {
	    		timeoutValue *= (1000*60);        		
	    	} else if (exact("HOURS",textProvider)) {
	    		timeoutValue *= (1000*60*60);        		        		
	    	} else if (exact("HOUR",textProvider)) {
	    		timeoutValue *= (1000*60*60);        		        		
	    	} else if (exact("DAYS",textProvider)) {
	    		timeoutValue *= (1000*60*60*24);
	    	} else if (exact("DAY",textProvider)) {
	    		timeoutValue *= (1000*60*60*24);
	    	} else if (exact("WEEKS",textProvider)) {
	    		timeoutValue *= (1000*60*60*24*7);
	    	} else if (exact("WEEK",textProvider)) {
	    		timeoutValue *= (1000*60*60*24*7);
	    	}
	    	timeoutProvider = new TimeoutProviderSimple(timeoutValue);
    	}
    	if (timeoutProvider == null) {
    		throw new ParserException("Invalid timeout",textProvider);
    	}
    	exactOrError(")",textProvider);
    	processTaskList(catchProcess,textProvider);
    	task.addTimeoutHandler(timeoutProvider,catchProcess,textProvider);
    	
    }
    private boolean processCatchBlock(TaskTry task, ProcessTextProvider textProvider) {
    	if (!exact("catch",textProvider)) {
    		return false;
    	}
    	ProcessDefinition catchProcess = new ProcessDefinition(task.getOwnerProcess());
        exactOrError("(",textProvider);
        if (exact("timeout",textProvider)) {
        	exactOrError("=",textProvider);
        	processTimeout(task, catchProcess, textProvider);
        } else if (exact("error",textProvider)) {
        	exactOrError(")",textProvider);
        	processTaskList(catchProcess,textProvider);
        	task.addErrorHandler(catchProcess,textProvider);
        } else if (exact("abort",textProvider)) {
        	exactOrError(")",textProvider);
        	processTaskList(catchProcess,textProvider);
        	task.addAbortHandler(catchProcess,textProvider);
        } else {
    		throw new ParserException("Invalid catch value",textProvider);
        }
        return true;
    }

    private void processTaskList(ProcessDefinition processDefinition, ProcessTextProvider textProvider)
    {
        exactOrError("{",textProvider);
        while (true) {
        	
	        if (exact("}",textProvider)) {
	        	TaskEnd task = new TaskEnd(processDefinition);
	        	task.setPosition(textProvider.getPosition());
	        	processDefinition.addTask(task);
	    		commit(textProvider);
	        	return;
	        }
	        
	        if (exact("if",textProvider)) {
	        	// Process an 'if'
	        	TaskIf task = new TaskIf(processDefinition);
	        	processDefinition.addTask(task);
	        	task.setPosition(textProvider.getPosition());
	        	task.setCondition(processCondition(textProvider));
	        	task.setNegate(textProvider.getNegate());
//	        	exactOrError("(",textProvider);
//	            if (!CVariable(textProvider))
//	            {
//	                throw new ParserException("Missing condition name",textProvider);
//	            }
//	            String condition = textProvider.getLastToken();
//	            FieldDescriptor fd = textProvider.getSchemaParser().findOperandInScope(textProvider.getCurrentScope(),condition );
//	            if (fd == null) {
//	            	throw new ParserException("Could not find condition: "+condition,textProvider);
//	            }
//	            task.setCondition(fd);
//	            exact(")",textProvider);
	            ProcessDefinition trueProcess = new ProcessDefinition(processDefinition);
	            processTaskList(trueProcess,textProvider);
	            ProcessDefinition elseProcess = new ProcessDefinition(processDefinition);
	            if (exact("else",textProvider)) {
	                processTaskList(elseProcess,textProvider);
	            }
	            else
	            {
	            	TaskBase taskBase = new TaskStart(elseProcess);
	            	taskBase.setPosition(textProvider.getPosition());
	            	elseProcess.addTask(taskBase);
	            	taskBase = new TaskEnd(elseProcess);
	            	elseProcess.addTask(taskBase);
	            }
	            task.setTrueHandler(trueProcess);
	            task.setElseHandler(elseProcess);
	        	continue;
	        }
	        if (exact("do",textProvider)) {
	        	// Process a 'do' eg do "OrderItemProcess" for orderItems 
	        	ProcessDefinition subProcess = null;
	        	if (quotedString('"',textProvider)) {
	        		// we have an external subprocess
	        		String subprocessName = textProvider.getLastToken();
	        		for (ProcessDefinition pd :textProvider.getProcesses()) {
	        			if (pd.getName().equals(subprocessName)) {
	        				subProcess = pd;
	        			}
	        		}
	        		if (subProcess == null) {
	        			throw new ParserException("Could not find subprocess: "+subprocessName,textProvider);
	        		}
	        	}
	        	else
	        	{
	        		subProcess = new ProcessDefinition(processDefinition);
	        		processTaskList(subProcess,textProvider);
	        	}
	        	if (exact("while",textProvider)) {
	        		// do ... while
	        		FieldDescriptor fd = processCondition(textProvider);
	        		exactOrError(";",textProvider);
	        		TaskBase task = new TaskDoWhile(subProcess,fd, processDefinition);
	        		task.setNegate(textProvider.getNegate());
	        		task.setPosition(textProvider.getPosition());
		        	processDefinition.addTask(task);
	        	} else if (exact("until",textProvider)) {
	        		// do...until
	        		FieldDescriptor fd = processCondition(textProvider);
	        		exactOrError(";",textProvider);
	        		TaskBase task = new TaskDoUntil(subProcess,fd, processDefinition);
	        		task.setNegate(textProvider.getNegate());
	        		task.setPosition(textProvider.getPosition());
		        	processDefinition.addTask(task);
	        	} else {
	        		// for...
		        	exactOrError("for",textProvider);
		            if (!CVariable(textProvider))
		            {
		                throw new ParserException("Missing field name",textProvider);
		            }
		            FieldDescriptor fd = textProvider.getSchemaParser().findOperandInScope(textProvider.getCurrentScope(), textProvider.getLastToken());
		            exactOrError(";",textProvider);
		        	TaskBase task = new TaskDoFor(subProcess,fd, processDefinition);
		        	task.setPosition(textProvider.getPosition());
		        	processDefinition.addTask(task);	        		
	        	}
	            continue;
	        }
	        if (exact("fork",textProvider)) {
	        	// Process a 'fork' eg fork "process1","process2"
	        	List<ProcessDefinition> subprocesses = new ArrayList<ProcessDefinition>();
	        	do {
	        		if (!quotedString('"',textProvider)) {
	        			throw new ParserException("expected subprocess name",textProvider);
	        		}
	        		String subprocessName = textProvider.getLastToken();
	        		ProcessDefinition subProcess = null;
	        		for (ProcessDefinition pd :textProvider.getProcesses()) {
	        			if (pd.getName().equals(subprocessName)) {
	        				subProcess = pd;
	        			}
	        		}
	        		if (subProcess == null) {
	        			throw new ParserException("Could not find subprocess: "+subprocessName,textProvider);
	        		}
	        		subprocesses.add(subProcess);
	        	} while (exact(",",textProvider));

	            exactOrError(";",textProvider);
	        	TaskFork task = new TaskFork(subprocesses,processDefinition);
	        	task.setPosition(textProvider.getPosition());
	        	processDefinition.addTask(task);
	            continue;
	        }
	        if (exact("try",textProvider)) {
	        	// Process a 'try'
	        	TaskTry tryTask = new TaskTry(processDefinition);
	        	tryTask.setPosition(textProvider.getPosition());
	        	processDefinition.addTask(tryTask);
	            ProcessDefinition tryProcess = new ProcessDefinition(processDefinition);
	            processTaskList(tryProcess,textProvider);
	            tryTask.addMainHandler(tryProcess);
	        	while (processCatchBlock(tryTask,textProvider));
	            continue;
	            }
	        if (exact("retry;",textProvider)) {
	        	// Process a 'resume'
	        	TaskRetry task = new TaskRetry(processDefinition);
	        	task.setPosition(textProvider.getPosition());
	        	processDefinition.addTask(task);
	            continue;
	        }
	        if (exact("continue;",textProvider)) {
	        	// Process a 'continue'
	        	TaskContinue task = new TaskContinue(processDefinition);
	        	task.setPosition(textProvider.getPosition());
	        	processDefinition.addTask(task);
	            continue;
	        }
	        if (exact("abort",textProvider)) {
	        	// Process an 'abort'
	        	String comment = "";
	    		if (quotedString('"', textProvider))
	    		{
	    			comment = textProvider.getLastToken();
	    		}
	    		exactOrError(";",textProvider);
	        	TaskAbort task = new TaskAbort(processDefinition,comment);
	        	task.setPosition(textProvider.getPosition());
	        	processDefinition.addTask(task);
	            continue;
	        }
	        if (exact("form",textProvider)) {
	        	// Process a 'form'
	        	exactOrError("=",textProvider);
	            
	            if (!CVariable(textProvider))
	            {
	                throw new ParserException("Missing form name",textProvider);
	            }
	            String formName = textProvider.getLastToken();
	            String queueName = "";
	            if (exact("queue",textProvider)) {
	            	exactOrError("=",textProvider);
		            if (!quotedString('"', textProvider))
		            {
		                throw new ParserException("Missing queue name",textProvider);
		            }
		            queueName = textProvider.getLastToken();
		            if (textProvider.getWorkflowManager().getQueue(queueName)==null) {
		            	throw new ParserException("Missing queue definition for "+queueName,textProvider);		            	
		            }
	            }
	        	TaskForm task = new TaskForm(processDefinition,formName,queueName);
	        	task.setPosition(textProvider.getPosition());
	        	processDefinition.addTask(task);
	            exactOrError(";",textProvider);
	            continue;
	        }
	        if (exact("message",textProvider)) {
	        	// Process a 'message'
	        	TaskMessage task = new TaskMessage(processDefinition);
	        	task.setPosition(textProvider.getPosition());
	        	processDefinition.addTask(task);
	        	exactOrError("=",textProvider);
	            if (!CVariable(textProvider))
	            {
	                throw new ParserException("Missing message name",textProvider);
	            }
	            task.setMessageName(textProvider.getLastToken());
	            @SuppressWarnings("unchecked")
				MessageSender<Object> messageSender = (MessageSender<Object>)processDefinition.getWorkflowManager().getMessage(task.getMessageName());
	            if (messageSender==null)
	            {
	            	throw new ParserException("Missing message bean for "+task.getMessageName(),textProvider);
	            }
	            task.setMessageSender(messageSender);
	            exactOrError(";",textProvider);
	            continue;
	        }
	        if (exact("compute",textProvider)) {
	        	// Process a 'compute'
	        	TaskCompute task = new TaskCompute(processDefinition);
	        	task.setPosition(textProvider.getPosition());
	        	processDefinition.addTask(task);
	        	exactOrError("=",textProvider);
	            if (!CVariable(textProvider))
	            {
	                throw new ParserException("Missing compute name",textProvider);
	            }
	            String computeName = textProvider.getLastToken();
	            ComputeType<?> computeTypeBean = processDefinition.getWorkflowManager().getComputeType(computeName);
	            if (computeTypeBean == null)
	            {
	            	throw new ParserException("Missing compute bean for "+computeName,textProvider);
	            }
	            task.setComputeType(computeTypeBean);
	            task.setArguments(getArgumentList(textProvider));
	            exactOrError(";",textProvider);
	            continue;
	        }
	        throw new ParserException("unrecognised syntax",textProvider);
        }
    }
    
    private FieldDescriptor processCondition(ProcessTextProvider textProvider) {
    	exactOrError("(",textProvider);
    	textProvider.setNegate(false);
    	if (exact("!",textProvider)) {
    		textProvider.setNegate(true);
    	}
        if (!CVariable(textProvider))
        {
            throw new ParserException("Missing condition name",textProvider);
        }
        String condition = textProvider.getLastToken();
        FieldDescriptor fd = textProvider.getSchemaParser().findOperandInScope(textProvider.getCurrentScope(),condition );
        if (fd == null) {
        	throw new ParserException("Could not find condition: "+condition,textProvider);
        }
        exact(")",textProvider);
        return fd;
    }
    
    @SuppressWarnings("unused")
	private Object getClassFromName(String className, Class<?> interfaceClass, ProcessTextProvider textProvider) {
        if (!className.contains(".")) {
        	className = textProvider.getPackageName()+className;
        }
        Class<?> targetClass = null;
        try {
			targetClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new ParserException("Class not found: "+className,textProvider);
		}
        Class<?>[] interfaces = targetClass.getInterfaces();
        boolean found = false;
        for (Class<?> i: interfaces) {
        	if (i == interfaceClass) {
        		found = true;
        		break;
        	}
        }
        if (!found)
        {
        	throw new ParserException("Class "+targetClass.getName()+" does not implement the ComputeType interface",textProvider);
        }
    	try {
			return targetClass.newInstance();
		} catch (Exception e) {
        	throw new ParserException(e.getLocalizedMessage(),textProvider);
		}
    }
    
    /**
     * Look for a Java Class
     * alphanum and underbars and dots are allowed but must start with a letter
     * @return true if matched
     */
    public boolean JavaClass(TextProvider textProvider)
        
    {
        clearLastToken(textProvider);
        clearLeadingSpaces(textProvider);
        mark(textProvider);
        if (m_debug)
			debug("testing",textProvider);
        int count=0;
        StringBuilder sb = new StringBuilder();
        while (true)
        {
            char c = getNextChar(textProvider);
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '.') break;
            if (count == 0 && !Character.isLetter(c)) break;
            mark(textProvider);
            sb.append(c);
            count++;
        }
        reset(textProvider); // removes last char
        String s = sb.toString().trim();
        if (s.length() == 0) return false;
        textProvider.setLastToken(s);
        debug(textProvider);
        return true;
    }

    private Map<String,String> getArgumentList(ProcessTextProvider textProvider)
    {
    	Map<String,String> ret = new HashMap<String,String>();
    	while (!nexact(";", true, textProvider))
    	{
            if (!CVariable(textProvider))
            {
                throw new ParserException("Expected argument name",textProvider);
            }
            String key = textProvider.getLastToken();
            exactOrError("=",textProvider);
    		if (!quotedString('"', textProvider))
    		{
    			throw new ParserException("Expected argument value",textProvider);
    		}
    		ret.put(key, textProvider.getLastToken());
    	}
    	return ret;
    }
	/**
	 * Parse the process.
	 * @return ProcessDefinition
	 */
    private ProcessDefinition processProcess(ProcessTextProvider textProvider)
    {
    	int start = textProvider.getPos();
        if (!JavaClass(textProvider))
        {
            throw new ParserException("Missing class name",textProvider);
        }
        textProvider.accumulate(" ");
        String className = textProvider.getLastToken();

//        if (className.indexOf('.') == -1) {
//        	className = textProvider.getPackageName()+className;
//        }
        textProvider.setCurrentScope(className);
    	String processName = getProcessName(textProvider);
        textProvider.addTOCElement(null, processName, start, textProvider.getPos(), TYPE_PROCESS);
    	String launchForm = null;
    	String queueName = null;
    	String description = "";
		if (quotedString('"',textProvider))
		{
			description = textProvider.getLastToken();
		}

        if (exact("launchForm", textProvider)) {
        	exactOrError("=", textProvider);
            if (!CVariable(textProvider))
            {
                throw new ParserException("Missing launch form name",textProvider);
            }
            launchForm = textProvider.getLastToken();
            if (exact("queue", textProvider)) {
            	exactOrError("=", textProvider);
	            if (!quotedString('"', textProvider))
	            {
	                throw new ParserException("Missing queue name",textProvider);
	            }
	            queueName = textProvider.getLastToken();
	            if (textProvider.getWorkflowManager().getQueue(queueName)==null) {
	            	throw new ParserException("Missing queue definition for "+queueName,textProvider);		            	
	            }
            }
        }

        ProcessDefinition ret = new ProcessDefinition(processName, className, textProvider.getWorkflowManager(), launchForm, queueName, textProvider.getPackageName(), description);
        ret.addTask(new TaskStart(ret));
        
        log.debug(ret.getName());
        textProvider.accumulate("\n");
        processTaskList(ret,textProvider);
		commit(textProvider);

        return ret;
    }
}
