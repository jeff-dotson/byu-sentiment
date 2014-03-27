package com.mycompany.mavenjava;

import au.com.bytecode.opencsv.CSVReader;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hello world!
 *
 */
public class App 
{
    
    public static StanfordCoreNLP pipeline;
    public static MongoClient mongoClient;
    public static DB db;
    public static DBCollection coll; 
    
    //Main entry point for the application
    public static void main( String[] args ) throws UnknownHostException, IOException
    {
        App.initialize();
        //App.loadTweets();
        List<Tweet> tweets = App.getTweets();
        
        //Loading the annotators and training data
        Properties props = new Properties();
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        //props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        props.put("annotators", "tokenize,ssplit,parse,pos,sentiment");
        props.put("parse.model", "edu\\stanford\\nlp\\models\\lexparser\\englishPCFG.ser.gz");         
        App.pipeline = new StanfordCoreNLP(props);
        
        for(Tweet t : tweets){
            t.calculateSentiment();
            t.saveToDB();
        }
        
        System.out.println("Finished Calculating!");
    }
    
    //Initializing database connections
    public static void initialize() throws UnknownHostException{
        //Byusentiment.mongoClient = new MongoClient( "localhost" , 27017 );    //local
        App.mongoClient = new MongoClient( "ec2-54-186-220-153.us-west-2.compute.amazonaws.com" , 27017 ); //AWS
        App.db = mongoClient.getDB( "byu" );
        App.coll = db.getCollection("tweets");
        //boolean auth = db.authenticate(myUserName, myPassword);
    }
    
    //Getting all the tweets from the database
    public static List<Tweet> getTweets(){
        
        try{
            DBCursor c = App.coll.find();
            List<Tweet> tweets = new ArrayList<Tweet>();
            
            for(DBObject row : c){
                Tweet t = new Tweet((BasicDBObject)row);
                tweets.add(t);
            }
        
            return tweets;
        } catch(Exception ex){
            return null;
        }
        
    }
    
    //Loading a list of tweets into the database
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
            App.coll.insert(toInsert);

        
        } catch (FileNotFoundException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
