MaduraWorkflows
================

This is a UI that goes with the MaduraWorkflow project. It is a web based application that gets its workflow from bundles (Madura Bundles) which means they can be switched at runtime.

There are actually three projects here, all built with maven. Follow this sequence:
1) cd to the project directory
2) mvn
3) cd bundles/Workflow1
4) mvn
5) cd ../tbundle
6) mvn
7) define the JNDI name to point to the bundles directory. On Tomcat you add this to the context.xml (replace PROJECT with your diectory):
    <Environment name="WorkflowUIBundlesDir" 
	value="PROJECT/bundles" 
	type="java.lang.String" override="true"/>
