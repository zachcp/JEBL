package jebl.evolution.align;

import jebl.evolution.align.scores.Scores;

/**
 * @author Matt Kearse
 * @version $Id$
 *
 * Represents a single residue in a multiple alignment profile
 */
class ProfileCharacter {
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
            return scores.score [ character1.characters [0]] [ character2.characters [0]];
        }
        for (int i = 0; i < character1.numberOfUniqueCharacters; i++) {
            for (int j = 0; j < character2.numberOfUniqueCharacters; j++) {
                score += scores.score [character1.characters[i]] [ character2.characters[j]]*
                        character1.count[i]*character2.count[j];
            }
        }
        return score/totalCharacters;
    }

    public int print() {
        System.out.print("(");
        for (int i = 0; i < numberOfUniqueCharacters; i++) {
            System.out.printf("%c: %d ",characters[i], count[i]);
        }
        System.out.print(")");
        return numberOfUniqueCharacters;
    }

    public boolean isAllGaps() {
        if(numberOfUniqueCharacters != 1) return false;
        if(characters[0]!='-') return false;
        return true;

    }
}
