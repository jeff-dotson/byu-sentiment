/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.byu.tweetsentiment;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import java.util.Date;
import java.util.List;

/**
 *
 * @author moncur
 * This class manages the sentiment analysis and database insertion of tweets
 */
public class MyTweet {

    public BasicDBObject dbrow;

    //Constructor
    public MyTweet(BasicDBObject _dbrow) {
        this.dbrow = _dbrow;
    }
    
    //Constructor
    public MyTweet(DBObject _dbrow) {
        this.dbrow = (BasicDBObject)_dbrow;
    }

    //Calculates the sentiment of a tweet using the Stanford NLP tools
    public void calculateSentiment() {

        try {
            String text = this.dbrow.get("text").toString();
            Annotation document = new Annotation(text);
            App.pipeline.annotate(document);
            List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
            int avgSentimentScore = 0;
            int its = 0;
            for (CoreMap sentence : sentences) {
                its++;
                Tree t = sentence.get(edu.stanford.nlp.sentiment.SentimentCoreAnnotations.AnnotatedTree.class);
                int sentimentScore = RNNCoreAnnotations.getPredictedClass(t);
                avgSentimentScore += sentimentScore;
            }
            avgSentimentScore = avgSentimentScore / its;
            this.dbrow.append("sentiment", avgSentimentScore);
            
            System.out.println(text);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

    }
    
    public void parseDate(){
        try{
            String created_at = this.dbrow.getString("created_at");
            Date parse = new Date(created_at);
            this.dbrow.append("date",parse);
        } catch(Exception ex){
            
        }
    }
    
    //Physically inserting the data into the MongoDatabase
    public void insertToDB(){
        try {
            App.tweets.insert(dbrow);
        } catch (Exception ex) {

        }
        
    }
    
    //Physically saves the data to the database
    public void saveToDB() {
        App.tweets.save(dbrow);
    }
}
