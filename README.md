## General information

The project was built and stored in [Sharepoint](https://oasissmartsim.sharepoint.com/Operations/Forms/AllItems.aspx?newTargetListUrl=%2FOperations&viewpath=%2FOperations%2FForms%2FAllItems%2Easpx&id=%2FOperations%2F5%2D%20Infrastructure%5FFR%2F4%2DTools%2FTaskadapter&viewid=168d5414%2D421f%2D4898%2Db8f9%2Da5be0acb4da5)

[Redmine documentation](https://tickets.oasis-smartsim.com/projects/devops/wiki/Redmine-JIRA_integration_using_Taskadapter)

# TaskAdapter
TaskAdapter is a stand-alone application to transfer tasks between various bug tracking and project management systems, 
e.g. between Atlassian Jira, Redmine, Microsoft Project, etc.
The app is implemented in Java language and requires a JVM (Java Virtual Machine) installed to run. 
It has a web UI module to support editing synchronization configs and start export process.

App website: https://taskadapter.com
Binary releases: https://taskadapter.com/download

# Local development

This chapter explains how to build and run the application from the source code, if you choose to do so. This may be
useful if you want some customizations or if you want to submit a bugfix.

## Local development - pre-requisites

* Have Java Development Kit (JDK) version 14+ installed.
* have npm installed (`sudo apt install npm`)
* run the following command to disable Vaadin Gradle usage reporting (to avoid a failing NPM "integrity" check):
```
    npm explore @vaadin/vaadin-usage-statistics -- npm run disable
```

##  Local development - run the application in an IDE

1. Run `./gradlew vaadinPrepareFrontend` command in the project folder, to configure Vaadin for npm mode.
2. Import the project into your IDE (say, Intellij IDEA). You can open "build.gradle" file in the project root folder.
3. Run/Debug the `TADevLauncher` class as an application (run the `main()` method).
   The app will use `npm` to download all javascript libraries (will take a long time)
   and will start in development mode.
4. Optionally, add the following parameter to `TADevLauncher` run configuration arguments list:

* `--openTaskAdapterPageInWebBrowser` 

this will open a web browser on app start.

## Build and run from command line

Run this in the project root folder:
```
    ./gradlew clean build -Pvaadin.productionMode -ProllbarApiTokenProperty=<token_here>
```    
Note - the rollbar Api property is optional. If not provided, the app will skip Rollbar error reporting.

This command will create a ZIP file in `webui/build/distributions` folder. Unpack the ZIP anywhere,
run `bin/taskadapter` script. It will automatically open TA URL in browser when the web app is ready.


Command without the Prollbar:
```
    ./gradlew clean build -Pvaadin.productionMode 
```    
