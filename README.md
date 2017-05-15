# Trust Service Reputation

## Introduction
The web application is available [here](http://trustservicereputation.eu/).

The goal of this application is to evaluate [Trust service providers](https://en.wikipedia.org/wiki/Trust_service_provider)
provided by the European Commission via [EU Trusted Lists](https://ec.europa.eu/digital-single-market/en/eu-trusted-lists-trust-service-providers).

A trust service provider is a person or legal entity providing and preserving digital certificates to create and validate electronic signatures and to authenticate
their signatories as well as websites in general.

This application collects statistics by regularly downloading and validating documents provided by trust service providers.
Evaluation is automatically computed based on the presence of specific documents, their availability and validity.

Trust Service Reputation is open source and licensed under the terms of the MIT license. The source code is available
[here](https://github.com/marcplouhinec/trust-service-reputation/).

## Technologies
* [Kotlin language](https://kotlinlang.org/)
* [Spring Boot](https://projects.spring.io/spring-boot/)
* [MySQL](https://www.mysql.com/)
* [JQuery](https://jquery.com/)
* [Font Awesome](http://fontawesome.io/)
* [Apache HTTP Server](https://httpd.apache.org/) as a reverse proxy

## Build and installation
The scripts in this section target the [Debian](https://www.debian.org/) operating system.

### Database
Setup MySQL (version >= 5.5.x):

    sudo apt-get install mysql-server mysql-client
    
Create a database and a user:

    mysql -u root -p
    CREATE DATABASE trust_service_reputation;
    CREATE USER 'tsreputation'@'localhost' IDENTIFIED BY 'password';
    GRANT ALL ON trust_service_reputation.* TO 'tsreputation'@'localhost';
    QUIT;
    
### Download and build the application
Clone the repository from GitHub:

    git clone https://github.com/marcplouhinec/trust-service-reputation.git
    cd trust-service-reputation
    
Configure the datasource, build and test it:

    vi src/main/resources/application.properties
    mvn clean install
    
Create the Debian package:

    chmod +x build-deb-package.sh
    ./build-deb-package.sh $(git rev-parse --short HEAD)
    
The package is named: "./trustservicereputation_1.0.0.GIT_HASH_all.deb"

### Installation
Copy the .deb package on your server and stop the existing version if applicable:

    sudo service trustservicereputation stop
    
Install the package:

    sudo dpkg -i ./trustservicereputation_latest_all.deb
    sudo apt-get -f install
    
Setup MySQL, create a database and user as explained in the previous section.

Adapt the configuration files:

    cd /etc/trustservicereputation
    vi application.properties
    vi logback.xml
    cd /etc/apache2/sites-available/trustservicereputation
    vi trustservicereputation

Start the service:

    sudo service trustservicereputation start
    sudo service apache2 restart

The logs are located in the folder /var/log/trustservicereputation.

After one hour, the scheduled tasks should have populated the database and the application
should be available at http://localhost:9096/ (and at the URL defined in the Apache configuration).
