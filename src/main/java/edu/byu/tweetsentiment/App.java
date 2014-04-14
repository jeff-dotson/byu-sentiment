package edu.byu.tweetsentiment;

import au.com.bytecode.opencsv.CSVReader;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.event.Event;
import com.google.common.collect.Lists;
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
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hello world!
 *
 */
public class App 
{
    
    //Some global static variables we use
    public static StanfordCoreNLP pipeline;
    public static MongoClient mongoClient;
    public static DB db;
    public static DBCollection tweets; 
    public static DBCollection searchTerms; 
    
    //Keys & tokens for twitter
    public static String consumerKey;
    public static String consumerSecret;
    public static String accessToken;
    public static String accessTokenSecret;
    
    //Database address
    public static String mongoAddress;
    public static int mongoPort;
    
    //Main entry point for the application
    public static void main( String[] args ) throws UnknownHostException, IOException, InterruptedException
    {
            
        //Initializing database connections
        App.initialize();
   
        //Calculating sentiment for a list of tweets
        //App.loadTweets();
        //List<MyTweet> mtweets = App.getTweets();
        //for(MyTweet t : mtweets){
        //    t.parseDate();
        //    t.saveToDB();
        //}
        
        //Loading the annotators and training data for the stanford NLP tools
        Properties props = new Properties();
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        //props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        props.put("annotators", "tokenize,ssplit,parse,pos,sentiment");
        props.put("parse.model", "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");         
        App.pipeline = new StanfordCoreNLP(props);
        
        //Listening for tweets
        TwitterFilterStream f = new TwitterFilterStream();
        f.oauth(App.consumerKey, App.consumerSecret, App.accessToken, App.accessTokenSecret);
        
        System.out.println("Finished Calculating!");
    }
    
    //Initializing database connections
    public static void initialize() throws UnknownHostException, FileNotFoundException{
        
        //Reading config values from config file
        String content = new Scanner(new File("config.json")).useDelimiter("\\Z").next();
        //System.out.println(content);
        BasicDBObject o = (BasicDBObject) JSON.parse(content);
        App.consumerKey = o.getString("consumerKey");
        App.consumerSecret = o.getString("consumerSecret");
        App.accessToken = o.getString("accessToken");
        App.accessTokenSecret = o.getString("accessTokenSecret");
        App.mongoAddress = o.getString("mongoAddress");
        App.mongoPort = o.getInt("mongoPort");
        
        //Connecting to the database
        App.mongoClient = new MongoClient( App.mongoAddress , App.mongoPort );    //local
        App.db = mongoClient.getDB( "byu" );
        App.tweets = db.getCollection("tweets");          
        App.searchTerms = db.getCollection("searchTerms"); 
        
    }
    
    //Loading all tweets from the database into memory
    public static List<MyTweet> getTweets(){
        
        try{
            BasicDBObject query = new BasicDBObject("date", new BasicDBObject("$exists", false)); 
            DBCursor c = App.tweets.find(query).limit(12000);
            List<MyTweet> tweetss = new ArrayList<MyTweet>();
            
            for(DBObject row : c){
                MyTweet t = new MyTweet((BasicDBObject)row);
                tweetss.add(t);
            }
        
            return tweetss;
        } catch(Exception ex){
            return null;
        }
        
    }
    
    //Loading a list of tweets into the database from a csv file
    public static void loadTweets() throws IOException{
        try {
            CSVReader reader = new CSVReader(new FileReader("tweets-2.csv"));
            //reader.readNext();
            List myEntries = reader.readAll();
            String[] headers = (String[]) myEntries.get(0);
            
            //Adding data to the DbObject
            List<DBObject> toInsert = new ArrayList<DBObject>();
            for(int i = 1; i < myEntries.size(); i++){
                BasicDBObject doc = new BasicDBObject();
                String[] row = (String[]) myEntries.get(i);
                
                for(int j = 0; j < row.length; j++){
                    doc.append(headers[j], row[j]);
                }
                toInsert.add(doc);
            }
        
            //Inserting data into the database             
            App.tweets.insert(toInsert);

        
        } catch (FileNotFoundException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
