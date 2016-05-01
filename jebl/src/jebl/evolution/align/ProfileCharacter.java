package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;

/**
 * @author Matt Kearse
 *
 * Represents a single residue in a multiple alignment profile
 * It can represent character frequencies inside an alignment column
 * as a fraction of the total number of characters in that column.
 */
public class ProfileCharacter {
    /*
    'characters' contains the actual residue character, and a parallel array called
    'count' contains the number of times that character occurs. NumberOfUniqueCharacters
    contains the length of these to parallel arrays. 'totalCharacters' is the sum of
    all entries in the array 'count'
    */
    private final char characters[];
    private final int count[];
    private int numberOfUniqueCharacters;
    private int totalCharacters;
    private boolean calculatedGapFraction=false;
    private  float gapFraction;
    private boolean isImmutable = false;

    /**
     *
     * @param alphabetSize the number of distinct characters in the alphabet (e.g. 20 for amino acids)
     * @return an estimate of the amount of memory used per character in a profile created during alignment
     */
    public static int getMemoryUsagePerProfileCharacter(int alphabetSize) {
        int minimumObjectSize = 16;
        return
                (2+4)*alphabetSize + // the contents of the characters and count arrays
                minimumObjectSize*3+ // the character, count array objects and this object
                20;
    }

    public ProfileCharacter(int alphabetSize) {
        if (alphabetSize < 0) {
            throw new IllegalArgumentException("Expected a nonnegative alphabet size, got " + alphabetSize);
        }
        characters = new char[alphabetSize +1];
        count = new int[alphabetSize +1];
    }

    private static int MAX_CHAR = 128;
    private static ProfileCharacter immutableCharacters[] = new ProfileCharacter[MAX_CHAR];
    static {
        for (char c = 0; c < MAX_CHAR; c++) {
            immutableCharacters[c]=createImmutableProfileCharacter(c);
        }
    }

    private static ProfileCharacter createImmutableProfileCharacter(char c) {
        ProfileCharacter pc = new ProfileCharacter(0);
        pc.totalCharacters = 1;
        pc.characters[0]=c;
        pc.count[0]=1;
        pc.totalCharacters = 1;
        pc.numberOfUniqueCharacters = 1;
        pc.gapFraction(); // Force it to be calculated now to avoid threading issues later
        return pc;
    }

    /**
     * An immutable ProfileCharacter that uses less memory than a mutable profile character.
     * The same immtuable ProfileCharacter may also be returned from other calls to this method.
     * @param character the single character to wrap in a profile.
     * @return an immutable ProfileCharacter
     */
    public static ProfileCharacter getImmutableProfileCharacter(char character) {
        if (character>=0 && character<MAX_CHAR) {
            return immutableCharacters[character];
        }
        else {
            return createImmutableProfileCharacter(character);
        }
    }


    private void assertMutable() {
        if (isImmutable) {
            throw new IllegalArgumentException("This profile is immutable");
        }
    }

    public void addCharacter(char character, int increment) {
        assertMutable();
        calculatedGapFraction = false;
        totalCharacters += increment;
        for (int i = 0; i < numberOfUniqueCharacters; i++) {
            if(characters[i]== character) {
                count[i]+= increment;
                return;
            }
        }
        characters [ numberOfUniqueCharacters ] = character;
        count [ numberOfUniqueCharacters ++ ] = increment;
    }

    private void removeCharacter(char character, int increment) {
        assertMutable();
        calculatedGapFraction = false;
        totalCharacters -= increment;
        for (int i = 0; i < numberOfUniqueCharacters; i++) {
            if (characters[i] == character) {
                count[i] -= increment;
                if(count[i]== 0) {
                    count[i]= count [ numberOfUniqueCharacters -1];
                    characters[i]= characters [ numberOfUniqueCharacters - 1];
                    numberOfUniqueCharacters --;
                }
                return;
            }
        }
        assert(false);
    }

    public void addProfileCharacter(ProfileCharacter character) {
        assertMutable();
        for (int j = 0; j < character.numberOfUniqueCharacters; j++) {
            addCharacter(character.characters[j], character.count[j]);
        }
    }

    public void removeProfileCharacter(ProfileCharacter character) {
        assertMutable();
        for (int j = 0; j < character.numberOfUniqueCharacters; j++) {
            removeCharacter(character.characters[j], character.count[j]);
        }
    }

    public void addGaps(int count) {
        assertMutable();
        addCharacter('-', count);
    }

    public static float score(ProfileCharacter character1, ProfileCharacter character2, Scores scores) {
        double score = 0;
        long totalCharacters = ((long)character1.totalCharacters)*character2.totalCharacters;
        if(totalCharacters == 1) {
            return scores.score
                    [ character1.characters [0]]
                    [ character2.characters [0]];
        }
        if (totalCharacters==0)
            throw new IllegalStateException("totalCharacters shouldn't be 0 for "+character1 +" and "+character2);
        for (int i = 0; i < character1.numberOfUniqueCharacters; i++) {
            for (int j = 0; j < character2.numberOfUniqueCharacters; j++) {
                // one of these can be an empty array for some reason (bug 3472) - spreading over multiple lines to see which one, next time the bug occurs.
                char char1 = character1.characters[i];
                char char2 = character2.characters[j];
                long count = ((long)character1.count[i]) * character2.count[j];
                // TT: score is a 128x128 array, so we assume the character is ASCII - is this safe?
                score += ((double)scores.score[char1][char2]) * count;
            }
        }
        float result = (float) (score / totalCharacters);
        if (Float.isNaN(result) || Float.isInfinite(result))
            throw new IllegalStateException("Got "+result+" for "+character1+" and "+character2+" from "+score+" and "+totalCharacters);
        return result;
    }

    public static float scoreSelf(ProfileCharacter character, Scores scores) {
        float score = 0;
        long totalCharacters = ((long)character.totalCharacters) * character.totalCharacters;
        if (totalCharacters == 1) {
            return scores.score[character.characters[0]][character.characters[0]];
        }
        for (int i = 0; i < character.numberOfUniqueCharacters; i++) {
            for (int j = 0; j < character.numberOfUniqueCharacters; j++) {
                score += scores.score[character.characters[i]][character.characters[j]] *
                        character.count[i] * character.count[j];
            }
        }

        //reduce counts of identical characters being compared by one
        // for example, if comparing A:1 and B:1, score should be minimum
        // but if comparing A:2 B:1,  score should be higher
        for (int i = 0; i < character.numberOfUniqueCharacters; i++) {
            score -= scores.score[character.characters[i]][character.characters[i]];
            totalCharacters --;
        }

        return score / totalCharacters;
    }


    public int print() {
        System.out.print(toString());
        return numberOfUniqueCharacters;
    }

    public String toString() {
        if(numberOfUniqueCharacters==1) {
            return "" +characters[0];
        }
        StringBuilder result =new StringBuilder();
        result.append("(");
        for (int i = 0; i < numberOfUniqueCharacters; i++) {
            result.append(String.format("%c: %d ", characters[i], count[i]));
        }
        result.append(")");
        return result.toString();
    }

    public boolean isAllGaps() {
        if(numberOfUniqueCharacters > 2) return false;
        if(characters[0]!='-' && characters[0]!='_') return false;
        if (numberOfUniqueCharacters==1) return true;
        if (characters[1] != '-' && characters[1] != '_') return false;
        return true;

    }

    public void clear() {
        assertMutable();
        numberOfUniqueCharacters= 0;
        totalCharacters= 0;
    }

    /**
     *
     * @return the fraction of characters that are gap Characters in this profile
     */
    public float gapFraction () {
        if (totalCharacters==0) return 0;
        if (calculatedGapFraction) return gapFraction;
        int gapCount=0;
        for (int i = 0; i < numberOfUniqueCharacters; i++) {
            if(characters[i]=='-' || characters[i]=='_') {
                gapCount+=count[i];
            }
        }
        gapFraction = ((float) gapCount) / totalCharacters;
        assert gapFraction >= 0;
        return gapFraction;
    }
}
