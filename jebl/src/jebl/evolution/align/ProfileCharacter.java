package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;

/**
 * @author Matt Kearse
 * @version $Id$
 *
 * Represents a single residue in a multiple alignment profile
 */
public class ProfileCharacter {
    /*
    'characters' contains the actual residue character, and a parallel array called
    'count' contains the number of times that character occurs. NumberOfUniqueCharacters
    contains the length of these to parallel arrays. 'totalCharacters' is the sum of
    all entries in the array 'count'
    */
    public char characters[];
    public int count[];
    public int numberOfUniqueCharacters;
    public int totalCharacters;

    public ProfileCharacter(int alphabetSize) {
        characters =new char[alphabetSize +1];
        count =new int[alphabetSize +1];
    }

    public void addCharacter(char character, int increment) {
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

    public void removeCharacter(char character, int increment) {
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
        for (int j = 0; j < character.numberOfUniqueCharacters; j++) {
            addCharacter(character.characters[j], character.count[j]);
        }
    }

    public void removeProfileCharacter(ProfileCharacter character) {
        for (int j = 0; j < character.numberOfUniqueCharacters; j++) {
            removeCharacter(character.characters[j], character.count[j]);
        }

    }

    public void addGaps(int count) {
        addCharacter('-', count);
    }

    public static float score(ProfileCharacter character1, ProfileCharacter character2, Scores scores) {
        float score = 0;
        int totalCharacters = character1.totalCharacters*character2.totalCharacters;
        if(totalCharacters == 1) {
            return scores.score
                    [ character1.characters [0]]
                    [ character2.characters [0]];
        }
        for (int i = 0; i < character1.numberOfUniqueCharacters; i++) {
            for (int j = 0; j < character2.numberOfUniqueCharacters; j++) {
                score += scores.score [character1.characters[i]] [ character2.characters[j]]*
                        character1.count[i]*character2.count[j];
            }
        }
        return score/totalCharacters;
    }

    public static float scoreSelf(ProfileCharacter character, Scores scores) {
        float score = 0;
        int totalCharacters = character.totalCharacters * character.totalCharacters;
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
        if(numberOfUniqueCharacters != 1) return false;
        if(characters[0]!='-') return false;
        return true;

    }

    public void clear() {
        numberOfUniqueCharacters= 0;
        totalCharacters= 0;
    }

    /**
     *
     * @return the fraction of characters that are gap Characters in this profile
     */
    public float gapFraction () {
        for (int i = 0; i < numberOfUniqueCharacters; i++) {
            if(characters[i]=='-') {
                float result = ((float) count[i]) / totalCharacters;
                assert result>= 0;
                assert result < 1;//should not be calling this function on a profile that contains all gap Characters at one location.

                return result;
            }
        }
        return 0;
    }
}
