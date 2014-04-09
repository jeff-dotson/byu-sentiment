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

1. You'll also need to configure the location of the MongoDB in which you wish to store your data. These values should be placed in the "mongoAddress" & "mongoPort" fields in the **config.json** file as well.

# Run the project

To run the project in a linux environment, you'll need to do the following
1. Install Maven
2. Check the code out from github
3. Download the stanford sentiment data (as explained above)
4. Run the following commands from the root (chmod -R 777 byu-sentiment), (mvn compile), (mvn exec:java  -Dexec.mainClass=edu.byu.tweetsentiment.App)
