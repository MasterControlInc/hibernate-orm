![hibernateLogo](http://static.jboss.org/hibernate/images/hibernate_logo_whitebkg_200px.png)

# MasterControl Fork (Version 3.5)

### Why:
Lucee depends on Hibernate 3.5 in order to perform it's ColdFusion ORM functions. This version was released in 2010 and is missing many of the security updates and features that we would like to take advantage of in our Java code. This fork equips us to not have to make the extremely risky change of upgrading Lucee's hibernate version and instead put it in it's own package where it will not interfere with our Java code.

### How:
This fork is a direct fork of the 3.5 branch of the hibernate-orm project with one major difference, all packages have been changed from `org.hibernate` to `org.luceehibernate`. This allows it to coexist on the classpath with modern versions of Hibernate without issue. 

## How to build:
- Clone this repository.
- Make sure to have [maven](https://maven.apache.org/install.html) installed.
- Import the whole repo into IntelliJ as a maven project. 
- From the maven tool window inside the `core` subproject run the `package` task.
- Your hibernate-core jar will be in the `core/target` folder

*Note: The only subprojects we care about our the `core` and `parent` subprojects. Many of the others are broken.*

## How to publish:
- Change the version in `core/pom.xml`
- Change the version to match in `parent/pom.xml`
- Change the version to match in `core/build.gradle`
- Change the version to match in `parent/build.gradle`
- Put credentials to artifactory that have rights to deploy in the `gradle.properties` file in `core` and `parent`
- Run `gradle artifactoryPublish` from the `core` and `parent` folders. Now you can pull in these dependencies like any other dependency.

In order to publish a snapshot build of the library make sure the repo-key in core/build.gradle and parent/build.gradle are set to libs-snapshot-local, set version in publishVersion() from FINAL to SNAPSHOT in parent/pom.xml and core/pom.xml, and change the artifact() task in core/build.gradle to use the SNAPSHOT jar. We left commented SNAPSHOT examples in the files referenced.

##### Please direct any questions to the System Architecture team.
