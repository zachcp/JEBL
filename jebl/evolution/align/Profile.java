package jebl.evolution.align;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Matt Kearse
 * @version $Id$
 *
 * Represents a profile of a number of sequences to be used in a
 * multiple sequence alignment.
 */
class Profile {
    ProfileCharacter[] profile;
    int alphabetSize;
//    int length;
    int sequenceCount;
    Map<Integer, String> paddedSequences=new HashMap<Integer, String>();

    public Profile(int alphabetSize) {
        this.alphabetSize = alphabetSize;
    }
    public Profile(int sequenceNumber,String sequence) {
        this(calculateAlphabetSize(new String[] {sequence}));
        addSequence(sequenceNumber,sequence);
    }
    public int length () {
        return profile.length;
    }

    public static ProfileCharacter[] createProfile(String sequence, int alphabetSize) {
        int length = sequence.length();
        ProfileCharacter results[] = new ProfileCharacter[length];
        for (int i = 0; i < length; i++) {
            ProfileCharacter profile = new ProfileCharacter(alphabetSize);
            profile.addCharacter(sequence.charAt(i), 1);
            results[i] = profile;
        }
        return results;
    }
    public void addSequence(int sequenceNumber,String sequence) {
        sequenceCount++;
        profile = createProfile(sequence, alphabetSize);
        paddedSequences.put(sequenceNumber, sequence);
//        length = profile.length;
    }

    public void remove(Profile remove) {
        int size = length();
        assert(size == remove.length());
        for (int i = 0; i < size; i++) {
            profile[i].removeProfileCharacter(remove.profile[i]);
        }
        sequenceCount-= remove.sequenceCount;
        for (Integer sequenceNumber : remove.paddedSequences.keySet()) {
            paddedSequences.remove(sequenceNumber);
        }

        trim();

    }

    /* used after a sequence has been removed from a profile to remove profile characters
    that are all gap characters in the remaining sequences profiled.
    */
    private void trim() {
        int gapCount = 0;
        int count = 0;
        for (ProfileCharacter character : profile) {
            if(character.isAllGaps ())
                gapCount ++;
            else
                count ++;
        }
//        System.out.println("gaps =" + gapCount+ "," + count);
        if(gapCount== 0) return;
        ProfileCharacter characters[]=new ProfileCharacter[count];
        char [][] sequences = new char[sequenceCount][];
        char[][] newSequences = new char[sequenceCount][];
        int[] sequenceNumbers =new int[sequenceCount];
        for (int i = 0; i < sequenceCount; i++) {
            newSequences[i]=new char[count];
        }
        int i = 0;
        for (Map.Entry<Integer, String> entry : paddedSequences.entrySet()) {
            sequenceNumbers[i]= entry.getKey();
            sequences[i++] = entry.getValue ().toCharArray();
        }
        assert(i== sequenceCount);
        int index = 0;
        int sourceIndex = 0;
        for (ProfileCharacter character : profile) {
            if (character.isAllGaps()) {
                sourceIndex++;
                continue;
            }
            characters [ index  ] = character;
            for (int j = 0; j < sequenceCount; j++) {
                newSequences[j][ index ] = sequences [j][ sourceIndex ];
            }
            sourceIndex++;
            index ++;
        }
        for (int j = 0; j < sequenceCount; j++) {
            String sequence = new String(newSequences[j]);
            assert(sequence.length()== count);
            paddedSequences.put(sequenceNumbers[j], sequence);
        }
        profile= characters;
    }

    public static Profile combine(Profile profile1, Profile profile2, AlignmentResult result1, AlignmentResult result2) {
        int size = result1.size;
        int alphabetSize = profile1.alphabetSize;
        Profile result =new Profile(alphabetSize);
        result.profile =new ProfileCharacter[size];
        int index1= 0;
        int index2= 0;
        for (int i = 0; i < size; i++) {
            ProfileCharacter character =new ProfileCharacter(alphabetSize);
            if(result1.values[i]) {
                character.addProfileCharacter(profile1.profile[index1++]);
            }
            else {
                character.addGaps(profile1.sequenceCount);
            }
            if(result2.values[i]) {
                character.addProfileCharacter(profile2.profile[index2++]);
            }
            else {
                character.addGaps(profile2.sequenceCount);
            }
            result.profile[i]= character;
        }
        for (Map.Entry<Integer, String> entry : profile1.paddedSequences.entrySet()) {
            String sequence = entry.getValue();
            sequence =buildAlignmentString(sequence, result1);
            result.paddedSequences.put(entry.getKey(), sequence);
        }
        for (Map.Entry<Integer, String> entry : profile2.paddedSequences.entrySet()) {
            String sequence = entry.getValue();
            sequence =buildAlignmentString(sequence, result2);
            result.paddedSequences.put(entry.getKey(), sequence);
        }
        result.sequenceCount= profile1.sequenceCount + profile2.sequenceCount;
        assert(result.sequenceCount == result.paddedSequences.size());
//        result.length = size;
        return result;
    }

    public static int calculateAlphabetSize(String[] sequences) {
        int total = 0;
        boolean found[] =new boolean[127];
        for (String sequence : sequences) {
            for (char character : sequence.toCharArray()) {
                if(! found [ character ]) total ++;
                found [ character ] = true;
            }
        }
        return total;
    }

    public static String buildAlignmentString(String sequence, AlignmentResult result) {

        StringBuilder builder =new StringBuilder();
        int index = 0;
        for (int i = 0; i < result.size; i++) {
            if(result.values[i]) {
                builder.append(sequence.charAt(index ++));
            }
            else {
                builder.append('-');
            }
        }
        assert(index == sequence.length());
        return builder.toString();
    }

    public void print(boolean displaySequences) {
        if(displaySequences) {
int maximum = 0;
            for (int k = 0; k < paddedSequences.size(); k++) {
                String sequence = paddedSequences.get(k);
                maximum = Math.max(maximum, sequence.length ());
                System.out.println(sequence);
            }
            for (int i = 0; i < maximum; i++) {
                System.out.print(i % 10);
            }
        }
        System.out.println ();
        int count = 0;
        int index = 0;
        for (ProfileCharacter character : profile) {
            System.out.print(" " +(index ++) + ":");
            count +=character.print ();
            if(count> 800000) {
                count = 0;
                System.out.println ();
            }
        }
        System.out.println();
    }
}
