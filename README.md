Madura Workflows
===============

(More detailed documentation (PDFs) can be found on the [core workflow](http://www.madurasoftware.com/madura-workflow.pdf) and
the [workflow ui](http://www.madurasoftware.com/madura-workflow-impl.pdf)).

This is yet another workflow package, but it is a little different from its competitors in that it is tightly integrated with [Madura Objects](https://github.com/RogerParkinson/madura-objects-parent/tree/master/madura-objects) and optionally [Madura Rules](https://github.com/RogerParkinson/madura-objects-parent/tree/master/madura-rules). It also ignores BPEL formats in favour of an easy-to-learn Java like syntax on the basis that it is always programmers rather than business analysts who end up writing business processes. Consequently there are no efforts made to provide a diagramatic UI to define them in favour of a syntax-aware editor as an Eclipse plugin: [madura-eclipse](https://github.com/RogerParkinson/madura-eclipse).

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

This is the core library the others depend on. It also holds the documentation. You'll find a pdf in the target directory after you've done the maven build. You can use this library to build your own UI if you don't want to use the one in madura-workflow-ui.

## madura-workflow-impl

### Deploying
This is the UI and it builds a war file you can deploy to an application server such as Tomcat 7. There are a number of configuration options to turn this into a production application, especially the location of the bundles. These are all in config.properties.

The maven build copies the example processes into the PROJECT/bundles directory of this project. 

## madura-workflow-headless

This project builds an uber jar file that can be used to run the background processes if you want to separate them from the UI. By default the background runs in the war file from madura-workflow-impl so this project is optional. Instructions for using headless are in the impl documentation.

### Demo Script
There is a demo script and other documentation on the ui [here](http://www.madurasoftware.com/madura-workflow-impl.pdf). See 'Running the Application'.

### From Demo to Production
The intention of this project is to provide a customisable template rather than a production application. Things you would want to review to turn it into production include:

 * Database. The in memory H2 database used in this would obviously be replaced in a production system. The default setting is to auto-create tables in the database which may not be the preference of your DBA.
 * Tomcat. The application is not particularly dependent on Tomcat because it only uses standard JEE facilities. There are also decisions to be made around how many instances of the application, particularly how many copies of the scheduler are running.
 * Security. The hard coded users in the security configuration must be reworked to use your enterprise security facilities. See the documentation for [Madura Login](https://github.com/RogerParkinson/madura-vaadin-support/tree/master/madura-login) for details.
 * The scheduler options configured here are probably about right, but your workload might mean they need to be tuned or tweaked, or you might just have different preferences in your enterprise.
 * Locking. You will need to create the lock table in your workflow database. This is documented in [Madura Utils](https://github.com/RogerParkinson/madura-objects-parent/tree/master/madura-utils)
 * The CSS definitions. You do not have to keep the defaults. You can change all the fonts, colours and images and completely rebrand this application if you know enough about CSS.
 * Language translations. The application is, we believe, fully i18n compliant. You will want to look at src/main/resources/messages.properties and produce a translated version of that. There is already a French one there. You also need to check localmessages.properties in the bundles.
 * Writing your own workflow definitions, forms, objects and rules. The whole reason for doing this is to get the workflow you really want, so this step is obvious. It is where, hopefully, most of the work will go to get the application where you want it.
 
Many of these settings are determined in the config.properties file and every setting there can be overridden by an environment variable, so you should be able to keep code edits to a minimum.

## madura-workflow-vaadin

Holds some definitions shared by the example processes and the Vaadin UI.

## simple-workflow

An example workflow bundle that does very little. It mostly serves as a dummy for the demo. There is no actual workflow process in it.

## order-workflow

An example workflow bundle that holds four process. The most interesting one is the "Demo" process which includes an external call and a user form.
