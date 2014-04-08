/**
 * Copyright 2013 Twitter, Inc. Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 *
 */
package edu.byu.tweetsentiment;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/*
* This class uses the Twitter hose-bird-client library to stream real-time tweets from twitter
* using the terms specified below. I'm not sure if there are limits to how many terms we can
* track or how many tweets-per-time-period we can receive. Probably something we should look into.
*/
public class TwitterFilterStream {

    public void oauth(String consumerKey, String consumerSecret, String token, String secret) throws InterruptedException {
        
        //Initializing some variables
        BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);
        StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();
        
        // Adding terms to be tracked
        endpoint.trackTerms(Lists.newArrayList("@PorterAirlines","@USAirways","@HawaiianAir","@AirFranceFR","@Delta", "@united", "@jetblue", "@southwestair", "@continental", "@virginamerica", "@alaskaair", "@americanair", "@AirAsia", "@BritishAirways", "@flyPAL", "@klm", "@TAMAirlines"));

        //Authenticating
        Authentication auth = new OAuth1(consumerKey, consumerSecret, token, secret);
        // Authentication auth = new BasicAuth(username, password); //Leaving this basic auth call here just in case

        // Create a new BasicClient. By default gzip is enabled.
        Client client = new ClientBuilder()
                .hosts(Constants.STREAM_HOST)
                .endpoint(endpoint)
                .authentication(auth)
                .processor(new StringDelimitedProcessor(queue))
                .build();

        // Establish a connection
        client.connect();

        // Reading a message from twitter, calculating the sentiment, then inserting it into the database
        for (int msgRead = 0; msgRead < 100000; msgRead++) {
            String msg = queue.take();
            System.out.println(msgRead + ") " + msg);
            insertTweet(msg);   //Analyzes sentiment, sends tweet to the db
        }

        //We're done!
        System.out.println("Just finished collection tweets");
        client.stop();

    }

    //Analyzes sentiment, sends tweet to the db
    public void insertTweet(String tweet) {

        //Adding data to the DbObject        
        BasicDBObject dbObject = (BasicDBObject) JSON.parse(tweet);

        //Using the MyTweet object to calculate sentiment and insert into the database
        MyTweet t = new MyTweet(dbObject);
        t.calculateSentiment();
        t.insertToDB();

    }
}
