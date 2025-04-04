import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import org.json.JSONException;
import org.json.JSONObject;


public class FeatureFactory {
    /** Add any necessary initialization steps for your features here.
     *  Using this constructor is optional. Depending on your
     *  features, you may not need to intialize anything.
     */
    public FeatureFactory() {}
    /**
     * Words is a list of the words in the entire corpus, previousLabel is the label
     * for position-1 (or O if it's the start of a new sentence), and position
     * is the word you are adding features for. PreviousLabel must be the
     * only label that is visible to this method.
     */
    private static final Set<String> popularNames = new HashSet<>();

    static {
        try {
            Files.lines(Paths.get("names.txt")).map(String::trim).filter(line -> !line.isEmpty()).forEach(popularNames::add);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<String> computeFeatures(List<String> words, String previousLabel, int position) {

        List<String> features = new ArrayList<String>();

        String currentWord = words.get(position);
        
        //TODO: The baseline performance:
        // precision = 0.5693950177935944
        // recall = 0.18912529550827423
        // F1 = 0.28393966282165045

        // TODO: New features here

        // Fe-1: Normalize letter variants (e.g. مدرسة -> مدرسه)
        String normalizedWord = currentWord.replaceAll("[إأآ]", "ا").replaceAll("ى", "ي").replaceAll("ة", "ه");
        features.add("normalizedWord=" + normalizedWord);

        // Fe-2: Composite morphological feature — combines normalization status, plural suffix, and feminine ending (e.g. كتاب, المسؤولين, الفنانات)
        features.add("normalized=" + normalizedWord.equals(currentWord) + "|plural=" + (currentWord.endsWith("ون") || currentWord.endsWith("ين")) + "|feminine=" + (currentWord.endsWith("ه") || currentWord.endsWith("ات")));

        // Fe-3: Extract adjacent context words (one before & one after)
        if (position > 0) features.add("previousWord=" + words.get(position - 1));
        if (position < words.size() - 1) features.add("nextWord=" + words.get(position + 1));

        // Fe-4: Detect preceding title prefix in prior one or two words (e.g. الرئيس)
        if (position > 1) {
            if ((words.get(position - 1).startsWith("ال") && words.get(position - 1).length() > 3 && words.get(position - 1).split(" ").length == 1) ||
                (words.get(position - 2).startsWith("ال") && words.get(position - 2).length() > 3 && words.get(position - 2).split(" ").length == 1)) {
                features.add("precededTitle=true");
            } else {
                features.add("precededTitle=false");
            }
        }

        // Fe-5: Extract prefix and suffix segments (e.g. ليد ,جو)
        if (currentWord.length() > 2) features.add("prefix=" + currentWord.substring(0, 2));
        if (currentWord.length() > 2) features.add("suffix=" + currentWord.substring(currentWord.length() - 3));

        // Fe-6: Identify numeric token (e.g. 2021)
        if (currentWord.matches("[0-9]+")) features.add("number=true");
        else features.add("number=false");

        // Fe-7: Detect preceding institutional or nationality in prior one or two words (e.g. السعودي)
        if (position > 1 && (words.get(position - 1).matches("^ال.*[ةهي]$") || words.get(position - 2).matches("^ال.*[ةهي]$"))) features.add("precededInsOrNat=true");
        else features.add("precededInsOrNat=false");

        // Fe-8: Detect preceding institutional in prior one or two words without the definite article (e.g. حكومة)
        if (position > 1 && ((words.get(position - 1).matches("^[^ال].*[ةه]$")) || (words.get(position - 2).matches("^[^ال].*[ةه]$")))) features.add("precededInstitutional=true");
        else features.add("precededInstitutional=false");

        // Fe-9: Detect preceding 4-letter verb candidate (e.g. يقول, وقال)
        if (position > 0 && (words.get(position - 1).length() == 4)) features.add("precededVerb=true");
        else features.add("precededVerb=false");

        // Fe-10: Detect if current word or previous word is a known popular name (e.g. بشار ,بشار الأسد) "انا مش مقتنع بهاي الفيشتر رغم انها خلت السكور يصير عالي"
        if (popularNames.contains(currentWord)) features.add("popularName=true");
        else features.add("popularName=false");

        if (position > 0 && popularNames.contains(words.get(position - 1))) features.add("precededByPopularName=true");
        else features.add("precededByPopularName=false");

        //TODO: The best performance I have achieved so far:
        // precision = 0.622895622895623
        // recall = 0.4373522458628842
        // F1 = 0.513888888888889

        return features;
    }

    public List<Datum> readData(String filename) throws IOException {

        List<Datum> data = new ArrayList<Datum>();
        BufferedReader in = new BufferedReader(new FileReader(filename));

        for (String line = in.readLine(); line != null; line = in.readLine()) {
            if (line.trim().length() == 0) {
                continue;
            }
            String[] bits = line.split("\\s+");
            String word = bits[0];
            String label = bits[1];

            Datum datum = new Datum(word, label);
            data.add(datum);
        }

        return data;
    }

    /**
     * Do not modify this method
     **/
    public List<Datum> readTestData(String ch_aux) throws IOException {

        List<Datum> data = new ArrayList<Datum>();

        for (String line : ch_aux.split("\n")) {
            if (line.trim().length() == 0) {
                continue;
            }
            String[] bits = line.split("\\s+");
            String word = bits[0];
            String label = bits[1];

            Datum datum = new Datum(word, label);
            data.add(datum);
        }

        return data;
    }

    /**
     * Do not modify this method
     **/
    public List<Datum> setFeaturesTrain(List<Datum> data) {
        // this is so that the feature factory code doesn't accidentally use the
        // true label info
        List<Datum> newData = new ArrayList<Datum>();
        List<String> words = new ArrayList<String>();

        for (Datum datum : data) {
            words.add(datum.word);
        }

        String previousLabel = "O";
        for (int i = 0; i < data.size(); i++) {
            Datum datum = data.get(i);

            Datum newDatum = new Datum(datum.word, datum.label);
            newDatum.features = computeFeatures(words, previousLabel, i);
            newDatum.previousLabel = previousLabel;
            newData.add(newDatum);

            previousLabel = datum.label;
        }

        return newData;
    }

    /**
     * Do not modify this method
     **/
    public List<Datum> setFeaturesTest(List<Datum> data) {
        // this is so that the feature factory code doesn't accidentally use the
        // true label info
        List<Datum> newData = new ArrayList<Datum>();
        List<String> words = new ArrayList<String>();
        List<String> labels = new ArrayList<String>();
        Map<String, Integer> labelIndex = new HashMap<String, Integer>();

        for (Datum datum : data) {
            words.add(datum.word);
            if (labelIndex.containsKey(datum.label) == false) {
                labelIndex.put(datum.label, labels.size());
                labels.add(datum.label);
            }
        }

        // compute features for all possible previous labels in advance for
        // Viterbi algorithm
        for (int i = 0; i < data.size(); i++) {
            Datum datum = data.get(i);

            if (i == 0) {
                String previousLabel = "O";
                datum.features = computeFeatures(words, previousLabel, i);

                Datum newDatum = new Datum(datum.word, datum.label);
                newDatum.features = computeFeatures(words, previousLabel, i);
                newDatum.previousLabel = previousLabel;
                newData.add(newDatum);

            } else {
                for (String previousLabel : labels) {
                    datum.features = computeFeatures(words, previousLabel, i);

                    Datum newDatum = new Datum(datum.word, datum.label);
                    newDatum.features = computeFeatures(words, previousLabel, i);
                    newDatum.previousLabel = previousLabel;
                    newData.add(newDatum);
                }
            }

        }

        return newData;
    }

    /**
     * Do not modify this method
     **/
    public void writeData(List<Datum> data, String filename)
            throws IOException {


        FileWriter file = new FileWriter(filename + ".json", false);


        for (int i = 0; i < data.size(); i++) {
            try {
                JSONObject obj = new JSONObject();
                Datum datum = data.get(i);
                obj.put("_label", datum.label);
                obj.put("_word", base64encode(datum.word));
                obj.put("_prevLabel", datum.previousLabel);

                JSONObject featureObj = new JSONObject();

                List<String> features = datum.features;
                for (int j = 0; j < features.size(); j++) {
                    String feature = features.get(j).toString();
                    featureObj.put("_" + feature, feature);
                }
                obj.put("_features", featureObj);
                obj.write(file);
                file.append("\n");
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        file.close();
    }

    /**
     * Do not modify this method
     **/
    private String base64encode(String str) {
        Base64 base = new Base64();
        byte[] strBytes = str.getBytes();
        byte[] encBytes = base.encode(strBytes);
        String encoded = new String(encBytes);
        return encoded;

    }

}
