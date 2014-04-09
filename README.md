byu-sentiment
=============

## Introduction 

This is a utility that streams tweets from twitter, assigns each a sentimnet score, then saves the tweets to a MongoDB database. The software was designed by students at Brigham Young University for research purposes. Go team!

**Very important!!! To get this project to work you'll need to perform the following initial setup.**

## Download Stanford NLP Training Data

1. Download the Stanford CoreNLP software from http://nlp.stanford.edu/software/corenlp.shtml
2. Unzip the file stanford-corenlp-3.3.1-models.jar using 7zip or similar. 
3. Place the extracted **/edu** directory (and all nested contents) in the root of this project. This includes the training data needed to run this project.

## Get Twitter Keys

To run the twitter stream you'll need to create keys for twitter's OAuth Authentication. 

1. Go to https://apps.twitter.com/
2. Create a new app, and generate the following keys: API Key, API Secret, Access Token, Access Token Secret
3. Insert these generated keys in the file **config.json**
 
<!-- language lang-json -->

    {
        "consumerKey": "[API Key Goes Here]",
        "consumerSecret":"[API Secret Goes Here]",
        "accessToken":"[Access Token Goes Here]",
        "accessTokenSecret":"[Access Token Secret Goes Here]",
        "mongoAddress":"localhost",
        "mongoPort":27017
    }
    
## Configure Database

You'll also need to configure the location of the MongoDB in which you wish to store your data. These values should be placed in the "mongoAddress" & "mongoPort" fields in the **config.json** file as well.

# Run the project

To run the project in a linux environment, you'll need to do the following

**Mount an ebs volume to /data/db & install mongodb**

- Learn how to mount a drive here: http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ebs-using-volumes.html
- Install MongoDB: http://docs.mongodb.org/manual/tutorial/install-mongodb-on-red-hat-centos-or-fedora-linux/

**Install Maven**

<!-- language shell -->
    sudo wget http://apache.mesi.com.ar/maven/maven-3/3.2.1/binaries/apache-maven-3.2.1-bin.tar.gzcd
    sudo tar xvzf apache-maven-3.2.1-bin.tar.gz
    sudo mkdir /usr/local/apache-maven
    cd /usr/local/apache-maven
    sudo cp -rv ~/apache-maven-3.2.1 .

**Configure maven's variables (so you can do an mvn command)**

<!-- Doobie doo -->
    export M2_HOME=/usr/local/apache-maven/apache-maven-3.2.1
    export M2=$M2_HOME/bin
    export MAVEN_OPTS="-Xms256m -Xmx512m"
    export PATH=$M2:$PATH
    JAVA_HOME=/usr
    mvn --version

**Install git**

<!-- language shell -->

    sudo yum install git

**Check the code out from github**

<!-- language shell -->
    cd ~/
    git clone https://github.com/rmoncur/byu-sentiment.git

**Download the stanford sentiment data**
 
<!-- language shell -->

    cd ~/byu-sentiment
    sudo wget http://nlp.stanford.edu/software/stanford-corenlp-full-2014-01-04.zip
    sudo unzip stanford-corenlp-full-2014-01-04.zip
    cd stanford-corenlp-full-2014-01-04
    sudo unzip stanford-corenlp-3.3.1-models.jar
    sudo mv edu ../

**Run the following commands from the root **

<!-- language shell -->

    # change the permissions of the root directory
    chmod -R 777 byu-sentiment
    
    # compile the code using maven
    mvn compile
    
    # run the program
    mvn exec:java  -Dexec.mainClass=edu.byu.tweetsentiment.App
    
    # run the program as a background process (run this instead of the line above)
    nohup mvn exec:java  -Dexec.mainClass=edu.byu.tweetsentiment.App &

### Stop the process

By running the nohup version of the command above, the app will run in the background. You may want to stop the process later on to update it, etc. To do that, follow these steps.

<!-- language shell -->
    ps aux

Note the process id from the above command. Then issue the following command to kill it:

<!-- language shell -->
    kill [the process id]

