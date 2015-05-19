package word2vec_readtext;

import org.apache.spark.api.java.JavaSparkContext;
import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests.
 */
public class Word2VecTextReaderTest {
    @Test
    public void testWord2VecTextReader() throws Exception {
        Word2VecTextReader textReader = new Word2VecTextReader();
        JavaSparkContext sc = textReader.connectSpark(1, 200);
        String textFile = new ClassPathResource("raw-sentences.txt").getFile().getAbsolutePath();
        Pair<VocabCache,WeightLookupTable> result = textReader.vectorizeWords(textFile, sc);
        String resultsFile = new File("results.txt").getAbsolutePath();
        textReader.saveVectors(result, resultsFile, false);
        assertEquals(result.getFirst().totalWordOccurrences(), 310);
    }

}
