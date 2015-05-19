# Word2Vec Text Input Example

This repo provides code that takes in a text file, converts the file to word vectors using Word2Vec and saves a file of the word vectors.

### Installation

In order to run this example you will need to configure your computer based on the information at this [link](http://nd4j.org/getstarted.html). If you have Java and Maven already installed do the following:

	$ git clone https://github.com/SkymindIO/word2vec-readtext-example.git
	$ cd word2vec-readtext-example && mvn clean install 

### How it Works
At the command line, run the jar file and provide the following arguments:

	$ java -cp <jar file> <class path> -input <input file path & name> -output <output file path and name>
	
	OR

	$ java -cp <jar file> <class path> -input <input file path & name> -output <output file path and name> -serialize -minWords <int> -vectorLength <int>

A specific command example:

	$ java -cp target/Word2VecExample-1.0-SNAPSHOT.jar insideview.Word2VecTextReader src/main/resources/raw-sentences.txt output.txt

	OR

	$ java -cp target/Word2VecExample-1.0-SNAPSHOT.jar insideview.Word2VecTextReader src/main/resources/raw-sentences.txt output.txt -serialize -minWords 2 -vectorLenght 200


Arguments you can pass in to adapt the results are as follows:

- **input** (*required*) = path and name of the text file to vectorize
- **output** (*required*)= path and name of where to store the vectors
- **serialize** = [*boolean, default = false*] enter true if you want a compressed (serialized file) otherwise it will output to a text file
- **minWords** = [*int, default=1*] number of tokens and is based on the tokenizer. In this example its 1 word per token
- **vectorLength** = [*int, default=300*] length of the feature vector token (in this example word)

The vectors per token will be saved to a file based on the path and name provided. Note, the DefaultTolkenizer is what is applied for word tokenization which is standard bag-of-words approach.

### Run UI Server
To see how the vectors function in a k Nearest Neighbors visualization, perform these steps:

	$ git clone https://github.com/deeplearning4j/deeplearning4j.git

- Open deeplearning4j in Intellij 
- Navigate to the deeplearning4j-ui module
- Select UiServer.java under src/main/java/org.deeplearning4j.ui
- Right click and choose Run
- Open a browser and enter the following in the address bar:

	http://localhost:8080/word2vec

- Follow the directions on the screen to load your word vector output file.