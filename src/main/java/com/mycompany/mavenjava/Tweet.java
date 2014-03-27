/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mycompany.mavenjava;

import com.mongodb.BasicDBObject;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import java.util.List;

/**
 *
 * @author moncur
 */
public class Tweet {
    public BasicDBObject dbrow;
    
    public Tweet(BasicDBObject _dbrow){
        this.dbrow = _dbrow;
    }
    
    public void calculateSentiment(){
        String text = this.dbrow.get("text").toString();
        Annotation document = new Annotation(text);
        App.pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        int avgSentimentScore = 0;
        int its = 0;
        for(CoreMap sentence: sentences) {
            its++;
            Tree t = sentence.get(edu.stanford.nlp.sentiment.SentimentCoreAnnotations.AnnotatedTree.class);
            int sentimentScore = RNNCoreAnnotations.getPredictedClass(t);
            avgSentimentScore += sentimentScore;
        }
        avgSentimentScore = avgSentimentScore/its;
        
        this.dbrow.append("sentiment", avgSentimentScore);
        
        System.out.println(text);
        
    }
    
    public void saveToDB(){
        App.coll.save(dbrow);        
    }
}

