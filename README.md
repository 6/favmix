Overview
========
Web app made using [Play Framework](http://www.playframework.org) and [siena](http://www.sienaproject.com).
Tested an deployed on Google App Engine: [favmix.appspot.com](http://favmix.appspot.com)

To use it, edit the contents of `conf/application.conf` to include your own secret key. Also edit `app/utilities/Constants.java` to change the password salt.

Controllers are located in the `app/controllers` directory, and the associated views (HTML) are in folders in the `app/views` directory. Models (where each file represents a table in the database) are located in the `app/models` directory. Static files are located in the `public` directory. Configuration files, such as those used for cURL routing and internationalization, are located in the `conf` directory. Google App Engine-specific configuration files are in the `war/WEB-INF` directory.

Deploying to Google App Engine
==============================
First, if you don't have an App Engine account or application ID yet, head over to [appengine.google.com](http://appengine.google.com) to register and create an application.

Configuration
-------------
Change the `war/WEB-INF/appengine-web.xml` file to match your application ID and version number.

Creating a WAR file
-------------------
Convert your application to a WAR file using the following command in the parent directory of the application folder:
    play war favmix/ -o favmix-war
This will create a new directory `favmix-war` that you can now deploy to App Engine.

Deploy
------
Run the following command to deploy the WAR file to App Engine:
    appcfg update favmix-war
You will be prompted to enter the e-mail and password of the account you used to sign up for App Engine.
