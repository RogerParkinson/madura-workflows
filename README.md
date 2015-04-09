MaduraWorkflows
===============

This is yet another workflow package, but it is a little different from its competitors in that it is tightly integrated with [Madura Objects](https://github.com/RogerParkinson/MaduraObjects) and optionally [Madura Rules](https://github.com/RogerParkinson/MaduraRules). It also ignores BPEL formats in favour of an easy-to-learn Java like syntax on the basis that it is always programmers rather than business analysts who end up writing business processes. Consequently there are no efforts made to provide a diagramatic UI to define them in favour of a syntax-aware editor as an Eclipse plugin: [maduraeditors](https://github.com/RogerParkinson/maduraeditors).

Following the general approach in MaduraObjects/MaduraRules there are restrictions on what kind of logic can be implemented in a process definition. You cannot add complex conditions to process definitions because those belong in the rules. The rules set boolean flags and process definitions check those. This approach is consistent with the idea that business rules are always handled by the rules engine rather than scattered throughout the system (workflow, application logic, ui etc).

Other things you would expect from a workflow system are also implemented here:

* Long running structured process environment with process state saved to database for data integrity.
* Multiple servers can service the process queues to ensure good throughput.
* Process tasks can include user operated forms, customised Java code and external calls (eg web services etc accessed via [Spring Integration](http://projects.spring.io/spring-integration/)).
* User operated forms can be auto generated from the data or hand coded. The example UI (sub project madura-workflow-ui) provides a fully functional [Vaadin-based](https://vaadin.com/home) UI including administration functions.
* Audit logs of all processes.
* New processes can be added dynamically (including their data definitions, database connections, external calls and UI).
* Processes monitor themselves for timeouts so that they can escalate tasks that are taking too long.
* Attachments: documents of any kind can be attached to any process. These may be referenced by users (or smart custom code) when working on the tasks in the processes. The UI project supports the upload/download of attachments.
* Security and permissions are managed such that users see processes they are allowed to action, but not other processes. Fields on forms have security defined by Madura Objects and implemented (in the UI) by Spring Security.

# Building

Just run mvn in the top level directory. To run the tests you need to be connected to the internet, but you can turn the tests off, of course. 

There is one issue that sometimes appears in a build. The first build is inevitably fine but subsequent builds sometimes fail with an error reported by Atomikos's 2 phase commit transaction handler. The error reports `no XAResource to rollback - the required resource is probably not yet intialized?`. The solution is to remove the \*.epoch and tmlog\*.log from your /tmp directory. This is an issue with the unit tests that use Atomikos not always shutting down correctly.

# Subprojects

There are several subprojects to this:

## madura-workflow

This is the core library the others depend on. It also hold the documentation. You'll find a pdf in the target directory after you've done the maven build. You can use this library to build your own UI is you don't want to use the one in madura-workflow-ui.

## madura-workflow-ui

### Deploying
This is the UI and it builds a war file you can deploy to an application server such as Tomcat 7. For that to work you do need to configure Tomcat first. Edit the context.xml and add the following (replace PROJECT with your madura-workflow-ui directory)

`<Environment name="WorkflowUIBundlesDir" 
	value="PROJECT/bundles" 
	type="java.lang.String" override="true"/>`

The maven build copies the example processes into the PROJECT/bundles directory. 

### Demo Script
There is a demo script and other documentation on the ui in the pdf in the madura-workflow-ui/target directory.

### From Demo to Production
The intention of this project is to provide a customisable template rather than a production application. Things you would want to review to turn it into production include:
* Database. The in memory database used in this would obviously be replaced in a production system. You may want to review whether you want H2 or some other database product more widely used in enterprise applications. Hibernate and Atomikos are also only options that can be replaced by alternatives you may prefer.
* Tomcat. The application is not particularly dependent on Tomcat because it only uses standard JEE facilities. The one area that might be a little tricky is if you want to use WebLogic because it has no easy way of defining a JNDI name to point to a simple string. That problem is solved by [weblogic-jndi-startup](https://github.com/RogerParkinson/weblogic-jndi-startup) . Your reconfigured application will likely make use of JNDI data sources instead of the simpler ones configured here. There are also decisions to be made around how many instances of the application, particularly how many copies of the scheduler are running.
* Security. The hard coded users in the security configuration must be reworked to use your enterprise security facilities. This usually just means adjusting the security configuration file because Spring Security is very comprehensive.
* The scheduler options configured here are probably about right, but your workload might mean they need to be tuned or tweaked, or you might just have different preferences in your enterprise.
* Locking. You will almost certainly need to move from SimpleLock (which is memory based) to SQLLock or perhaps something else you prefer.
* The CSS definitions. You do not have to keep the defaults. You can change all the fonts, colours and images and completely rebrand this application if you know enough about CSS.
* Language translations. The application is, we believe, fully i18n compliant. You will want to look at src/main/resources/messages.properties and produce a translated version of that. There is already a French one there. You also need to check localmessages.properties in the bundles.
* Writing your own workflow definitions, forms, objects and rules. The whole reason for doing this is to get the workflow you really want, so this step is obvious. It is where, hopefully, most of the work will go to get the application where you want it.

## madura-workflow-vaadin

Holds some definitions shared by the example processes and the UI.

## simple-workflow

An example workflow bundle that does very little. It mostly serves as a dummy for the demo. There is no actual workflow process in it.

## order-workflow

An example workflow bundle that holds four process. The most interesting one is the "Demo" process which includes an external call and a user form.
