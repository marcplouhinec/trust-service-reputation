#!/usr/bin/env bash

# Go to the folder containing the package
cd ..
cd ./target

# Stop the server if it is running
if ps aux | grep "[t]rustservicereputation" > /dev/null
then
    sudo service trustservicereputation stop
fi

# Install the Debian package
sudo dpkg -i ./trustservicereputation_latest_all.deb
sudo apt-get -f install

# Start the server
sudo service trustservicereputation start
sudo service apache2 restart