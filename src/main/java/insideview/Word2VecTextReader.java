package insideview;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.InMemoryLookupCache;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.spark.impl.multilayer.SparkDl4jMultiLayer;
import org.deeplearning4j.spark.models.embeddings.word2vec.Word2Vec;
import org.deeplearning4j.spark.models.embeddings.word2vec.Word2VecPerformer;
import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;

import org.deeplearning4j.util.SerializationUtils;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reading in text file
 * Converting to word vectors
 * Saving vectors as text
 */

public class Word2VecTextReader
{

    private static final Logger log = LoggerFactory.getLogger(Word2VecTextReader.class);

    private static JavaSparkContext connectSpark(int minWords, int vectorLength){

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[8]").set(SparkDl4jMultiLayer.AVERAGE_EACH_ITERATION, "false").set(Word2VecPerformer.NUM_WORDS, String.valueOf(minWords)).set(Word2VecPerformer.NEGATIVE, String.valueOf("0"))
                .set("spark.akka.frameSize", "100").set(Word2VecPerformer.VECTOR_LENGTH,String.valueOf(vectorLength))
                .setAppName("mnist");

        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        return sc;
    }

    private static Pair<VocabCache,WeightLookupTable> vectorizeWords(String fileName, JavaSparkContext sc) throws Exception {
        JavaRDD<String> rdd = sc.textFile(new File(fileName).toURI().toString());
        Word2Vec vec = new Word2Vec();
        return vec.train(rdd);
    }

    private static void saveVectors( Pair result, String outputFileName, Boolean serialize ) throws Exception {
        if (serialize) {
            SerializationUtils.saveObject(result, new File(outputFileName));
            return;
        }
        InMemoryLookupCache vecCache = (InMemoryLookupCache) result.getFirst();
        InMemoryLookupTable vecTable = (InMemoryLookupTable) result.getSecond();
        WordVectorSerializer.writeWordVectors(vecTable, vecCache, outputFileName);

    }

    public static void main( String[] args ) throws Exception
    {
        // Get filename
        String inputFilePath = "";
        String outputFileName = "";
        Boolean serialize = false;
        int minWords = 1;
        int vectorLength = 300;

        if(args.length == 2) {
            inputFilePath = args[0];
            outputFileName = args[1];
        } else {
            log.error("Please enter directory plus filename for the text file you want to convert, and enter the file name for vector output.");
        }

        log.info("Setting up Spark Context...");
        JavaSparkContext sc = connectSpark(minWords, vectorLength);

        log.info("Vectorizing words...");
        Pair<VocabCache,WeightLookupTable> result = vectorizeWords(inputFilePath, sc);

        saveVectors(result, outputFileName, serialize);
        log.info("Model saved");


    }
}
