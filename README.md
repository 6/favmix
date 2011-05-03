Favmix
======
Web app made using [Play Framework](http://www.playframework.org) and [siena](http://www.sienaproject.com).
Tested an deployed on Google App Engine: [favmix.appspot.com](http://favmix.appspot.com)

To use it, edit the contents of `conf/application.conf` to include your own secret key. Also edit `app/utilities/Constants.java` to change the password salt.

Controllers are located in the `app/controllers` directory, and the associated views (HTML) are in folders in the `app/views` directory. Models (where each file represents a table in the database) are located in the `app/models` directory. Static files are located in the `public` directory. Configuration files, such as those used for URL routing and internationalization, are located in the `conf` directory. Google App Engine-specific configuration files are in the `war/WEB-INF` directory.
