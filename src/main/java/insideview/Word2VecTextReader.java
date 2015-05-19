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
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.CmdLineException;

/**
 * Read in a text file
 * Convert text to word vectors
 * Save vectors into a file
 */

public class Word2VecTextReader
{

    @Option(name="-input", usage="Input file path and name", required=true)
    private String inputFilePath;

    @Option(name="-output", usage="Output file path and name", required=true)
    String outputFilePath;

    @Option(name="-serialize", usage="Compress the file. If not included a standard text file will be saved." )
    Boolean serialize = false;

    @Option(name="-min-words", usage="Enter minimum number of words to tokenize")
    int minWords = 1;

    @Option(name="-vector-length", usage="Enter number of features per word to capture.")
    int vectorLength = 300;

    private static final Logger log = LoggerFactory.getLogger(Word2VecTextReader.class);

    private JavaSparkContext connectSpark(int minWords, int vectorLength){

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[8]").set(SparkDl4jMultiLayer.AVERAGE_EACH_ITERATION, "false")
                .set(Word2VecPerformer.NUM_WORDS, String.valueOf(minWords)).set(Word2VecPerformer.NEGATIVE, String.valueOf("0"))
                .set("spark.akka.frameSize", "100").set(Word2VecPerformer.VECTOR_LENGTH,String.valueOf(vectorLength))
                .setAppName("mnist");

        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        return sc;
    }

    private Pair<VocabCache,WeightLookupTable> vectorizeWords(String fileName, JavaSparkContext sc) throws Exception {
        JavaRDD<String> rdd = sc.textFile(new File(fileName).toURI().toString());
        Word2Vec vec = new Word2Vec();
        return vec.train(rdd);
    }

    private void saveVectors( Pair result, String outputFileName, Boolean serialize ) throws Exception {
        if (serialize) {
            SerializationUtils.saveObject(result, new File(outputFileName));
            return;
        }
        InMemoryLookupCache vecCache = (InMemoryLookupCache) result.getFirst();
        InMemoryLookupTable vecTable = (InMemoryLookupTable) result.getSecond();
        WordVectorSerializer.writeWordVectors(vecTable, vecCache, outputFileName);

    }

    public void exec( String[] args) throws Exception{
        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);

        } catch (CmdLineException e) {
            // handling of wrong arguments
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
        }

        log.info("Setting up Spark Context...");
        JavaSparkContext sc = connectSpark(minWords, vectorLength);

        log.info("Vectorizing words...");
        Pair<VocabCache,WeightLookupTable> result = vectorizeWords(inputFilePath, sc);

        saveVectors(result, outputFilePath, serialize);
        log.info("Model saved");

    }

    public static void main( String[] args ) throws Exception
    {
        new Word2VecTextReader().exec(args);

    }
}
