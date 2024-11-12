import java.util.ArrayList;
import java.util.Arrays;
/**
 * Abstract class Chord - write a description of the class here
 *
 * @author (your name here)
 * @version (version number or date here)
 */
public class Triad// implements Cloneable
{
    public int[][] transformationGroup, directedTransformationGroup; // format: [Type][DeltaRoot]
    public int type, root;
    public static final int searchLimit = 100;

    public static int[][] triadDictionary = {
            {0,5,9,12},{0,4,9,12},{0,5,8,9},{0,5,9,13},{0,3,5,9},{0,4,9,13}
        };

    public static boolean ascending;
    private final String[] NOTE_NAMES = {" C", "C#", "D ", "D ", "Eb","E ", "F ", "F#", "F#", "G ", "Ab", "A ", "Bb", "Bb", "B "};
    
    /*
    {"0-C   ","1-C#  ","2-D-  ","3-D+  ","4-Eb  ","5-E   ","6-F   ","7-F#- ","8-F#+ ","9-G   ",
            "10-Ab ","11-A  ","12-Bb-","12-Bb+","14-B  "};
            */
            
    public static final int[] COMPOSITE = new int[]{0,4,5,6,7,11};
    public ArrayList<Triad> myNuclearFamily;
    public static ArrayList<int[][]> transformationMatrix, directedTransformationMatrix;

    public Triad(int tp, int rt){
        type = tp;
        root = rt;
        if(transformationMatrix == null)
            initializeTransformationMatrix();
        transformationGroup = transformationMatrix.get(type);
        directedTransformationGroup = directedTransformationMatrix.get(type);
        myNuclearFamily = null;
    }

    public ArrayList<Integer> notes(){
        ArrayList<Integer> notes = new ArrayList<Integer>();
        for(int n: triadDictionary[type]){
            notes.add((n + root) % 15);
        }
        return notes;
    }

    public Triad(Triad t){
        type = t.type;
        root = t.root;
        transformationGroup = transformationMatrix.get(type);
        directedTransformationGroup = directedTransformationMatrix.get(type);
        myNuclearFamily = null;
    }

    public static void initializeTransformationMatrix(){
        class Initializer{
            int[] getTransformation(int[] pitchClassSet){
                for(int a = 0; a < triadDictionary.length; a++){
                    int[] triad = triadDictionary[a];
                    if(triad.length != pitchClassSet.length)
                        continue;
                    for(int i = 0; i < pitchClassSet.length; i++){
                        int proposedRoot = pitchClassSet[i];
                        boolean areEqual = true;
                        for(int n = i; n < i + pitchClassSet.length; n++){
                            if(
                            triad[n - i] != 
                            (pitchClassSet [n % pitchClassSet.length]
                                + (15 - proposedRoot))
                            % 15){
                                areEqual = false;
                                break;
                            }
                        }
                        if(areEqual)
                            return new int[]{a, proposedRoot};
                    }
                }
                return new int[]{-1, -1};
            }

            int[][] getTransformationGroup(int[] triad, boolean directed){

                ArrayList<int[]> initialTriad = new ArrayList<int[]>();
                initialTriad.add(triad);
                int numberOfMembersToTransform = triad.length / 2;
                if(true  && triad.length % 2 == 0)
                    numberOfMembersToTransform--;
                if(false && !directed)
                    numberOfMembersToTransform = 1;
                System.out.println("MEMBERS TO TRANSFORM: " + numberOfMembersToTransform);

                ArrayList<int[]> inchoateGroup = new ArrayList<int[]>();
                /*
                int[]allowed = new int[]{1,2,3,4,5,6,7,8,9,10,11};
                if(directed){
                allowed = new int[]{1,2};
                if(!ascending)
                allowed = new int[]{11,10};
                }
                 */

                int[][] alloweds = new int[][]{{1,2,3,4,5,6,7,8,9,10,11,12,13,14}};

                /*if(!ascending)
                alloweds = new int[][]{{11,10},{1,2},{3,9,4,8,5,7,6}};
                if(directed){
                alloweds = new int[][]{{1,2}};
                if(!ascending)
                alloweds = new int[][]{{11,10}};
                }
                 */
                for(int[] allowed: alloweds){
                    ArrayList<int[]> potentialTriads = transformMembers(triad, initialTriad, numberOfMembersToTransform, allowed);

                    for(int[] potentialTriad: potentialTriads){
                        int[] transformation = getTransformation(potentialTriad);
                        if(transformation[0] >= 0
                        && !isIdentity(transformation, triad[0])
                        && !containsTransformation(inchoateGroup, transformation))
                            inchoateGroup.add(transformation);
                    }
                }
                int[][] transformationGroup = new int[inchoateGroup.size()][2];
                for(int i = 0; i < inchoateGroup.size(); i++){
                    transformationGroup[i] = inchoateGroup.get(i);
                }

                return transformationGroup;
            }

            boolean isIdentity(int[] transformation, int myType){
                return transformation[0] == myType && transformation[1] == 0;
            }

            boolean containsTransformation(ArrayList<int[]> transformations, int[] t2){
                for(int[] t1: transformations)
                    if(t1[0] == t2[0] && t1[1] == t2[1])
                        return true;
                return false;
            }

            public boolean contains(int[]  arr, int val){
                for(int v: arr)
                    if(v == val)
                        return true;
                return false;
            }

            ArrayList<int[]> transformMembers(int[] originalTriad, ArrayList<int[]> initialTriads, int numberOfMembers, int[] allowedAlterations){
                ArrayList<int[]> transformedTriads = new ArrayList<int[]>();
                transformedTriads.addAll(initialTriads);
                for(int[] triad: initialTriads){
                    for(int variableMember = 0; variableMember < triad.length; variableMember++){
                        if(!contains(originalTriad, triad[variableMember]))//already modified
                            continue;
                        for(int alt: allowedAlterations){//int value = triad[variableMember]; value < triad[variableMember] + 12; value++){
                            int value = triad[variableMember] + alt;
                            if(value % 15 == triad[variableMember])
                                continue;
                            int[] potentialTriad = new int[triad.length];
                            for(int i = 0; i < triad.length; i++)
                                if(i != variableMember)
                                    potentialTriad[i] = triad[i];
                                else
                                    potentialTriad[i] = value % 15;
                            Arrays.sort(potentialTriad);
                            transformedTriads.add(potentialTriad);
                        }
                        int[] potentialTriad = new int[triad.length - 1];
                        int ptInd = 0;
                        for(int i = 0; i < triad.length; i++)
                            if(i != variableMember)
                                potentialTriad[ptInd++] = triad[i];
                        Arrays.sort(potentialTriad);
                        transformedTriads.add(potentialTriad);
                    }
                    for(int val = 0; val < 15; val++){
                        boolean used = false;
                        for(int v: triad)
                            if(v == val){
                                used = true;
                                break;
                            }
                        if(used)
                            continue;
                        int[] potentialTriad = new int[triad.length + 1];
                        for(int i = 0; i < triad.length; i++)
                            potentialTriad[i] = triad[i];
                        potentialTriad[potentialTriad.length  - 1] = val;
                        Arrays.sort(potentialTriad);
                        transformedTriads.add(potentialTriad);
                    }
                }

                if(numberOfMembers == 1)
                    return transformedTriads;
                return transformMembers(originalTriad, transformedTriads, numberOfMembers - 1, allowedAlterations);
            }

            ArrayList<int[][]> generate3DTransformationMatrix(){
                ArrayList<int[][]> inchoateMatrix = new ArrayList<int[][]>();
                for(int[] triad: triadDictionary)
                    inchoateMatrix.add(getTransformationGroup(triad, false));
                return inchoateMatrix;
            }

            ArrayList<int[][]> generate3DDirectedTransformationMatrix(){
                ArrayList<int[][]> inchoateMatrix = new ArrayList<int[][]>();
                for(int[] triad: triadDictionary)
                    inchoateMatrix.add(getTransformationGroup(triad, true));
                return inchoateMatrix;
            }
        }
        Initializer init = new Initializer();
        transformationMatrix = init.generate3DTransformationMatrix();
        directedTransformationMatrix = init.generate3DDirectedTransformationMatrix();
        displayTransformationMatrix();
    }

    public static void displayTransformationMatrix(){
        System.out.println();
        System.out.println("PRINTING TRANSFORMATION MATRIX FOR THIS HARMONIC SPACE...");//transformationMatrix
        System.out.println("FORMAT: 'OriginalType:");//transformationMatrix
        System.out.println("        [transformedType][deltaRoot]");
        System.out.println();
        System.out.println();
        System.out.println("MATRIX {");
        for(int i = 0; i < transformationMatrix.size(); i++){
            System.out.println(i + ":");
            for(int[] transformation: transformationMatrix.get(i))
                System.out.println("        ["  + transformation[0] + "][" + transformation[1] + "]");
        }
        System.out.println("} END MATRIX");
    }

    public int findShortestPath(Triad relative){
        if(equals(relative))
            return 0;
        Triad[][] grid = new Triad[triadDictionary.length][15];
        grid[type][root] = this;
        int distance = 1;
        for(distance = 1; !search(relative, grid) && distance < searchLimit; distance++);
        myNuclearFamily = null;
        return distance;
    }

    public boolean search(Triad relative, Triad[][] grid){
        if(myNuclearFamily != null){
            for(Triad sibling: myNuclearFamily)
                if(sibling.search(relative, grid))
                    return true;
            return false;
        }
        myNuclearFamily = new ArrayList<Triad>();
        for(int i = 0; i < transformationGroup.length; i++){
            Triad unbornSibling = new Triad(transformationGroup[i][0],
                    (root + transformationGroup[i][1]) % 15);
            if(unbornSibling.isBorn(grid))
                myNuclearFamily.add(unbornSibling);
            if(unbornSibling.equals(relative))
                return true;
        }
        return false;
    }

    public boolean isBorn(Triad[][] grd){
        if(grd[type][root] != null)
            return false;
        grd[type][root] = this;
        return true;
    }

    public Triad generateTransformed(int i){
        int[] transformation = transformationGroup[i];
        return new Triad(transformation[0], (root + transformation[1]) % 15);
    }

    public Triad generateDirectedTransformed(int i){
        int[] transformation = transformationGroup[i];//directedTransformationGroup
        return new Triad(transformation[0], (root + transformation[1]) % 15);
    }

    public void printOutTransformations(){
        for(int i = 0; i < transformationGroup.length; i++)
            System.out.println(generateTransformed(i));
    }

    @Override
    public boolean equals(Object c){
        Triad contender = (Triad)c;
        return type == contender.type && root == contender.root;
    }

    @Override
    public String toString(){
        String myName = "[";
        for(int i = 0; i < triadDictionary[type].length; i++)
            myName += ((root + triadDictionary[type][i]) % 5) + NOTE_NAMES[(root + triadDictionary[type][i]) % 15] + ", ";
        myName = myName.substring(0, myName.length() - 2) + "] (" + type + "," + root + ")";
        return myName;
    }

    public static void test(){
        Triad[][] grid = new Triad[4][15];
        Triad a = new Triad(0, 0);
        Triad b = new Triad(1, 3);
        System.out.println(a.findShortestPath(b));
    }

    public String print(){

        String seq = "[";
        boolean[] notes = new boolean[15];
        for(int i = 0; i < triadDictionary[type].length; i++){
            notes[(root + triadDictionary[type][i]) % 15] = true;
        }
        for(int i = 0; i < 15; i++){
            if(notes[i])
                seq += NOTE_NAMES[i] + ", ";
            else
                seq += "  , ";
        }
        seq = seq.substring(0, seq.length() - 2) + "]";
        return seq;
    }
}
