import java.util.ArrayList;
/**
 * Write a description of class Tet15 here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Tet15
{
    public static final String[] NOTE_NAMES = new String[]{"C ", "C#", "D ", "Eb", "E ", "F ", "F#", "G ", "Ab", "A ", "Bb", "B "};
    public static int TET = 33;
    public static void main(String[] args){
        //printLimTrans(7);
        //calculateScale();
        //System.out.println(700/57.14285714);
        maximumlyEven();
    }
//0 1 3 5 6 8 10 12 13 15 17 19 20 22 24 26 27 29 31 
//0 2 5 8 10 13 16

//0 1 3 5 7 9 11 12 14 16 18 20 22 23 25 27 29 31
//0 2 5 7 10 12 15
//{0,3,9,12,18,22,27}
//m 3 = 8; p5 = 19

    public static void maximumlyEven(){
        double a = 0;
        while(a < 33){
            System.out.print((int)a + " ");
            a += 33 / 18.0;
        }

        System.out.println();
        System.out.println();
        System.out.println();

        a = 0;
        while(a < 12){
            System.out.print((int)a + " ");
            a += 12 / 5.0;
        }

        System.out.println();

        System.out.println();
        int[] mode = {0, 3, 5, 8, 11, 14, 16, 19, 22, 25, 27, 30};
        
        //mode = new int[]{0,3,7,11,14,18,22,25,29};
        int[] minorTriad = {0,8,19};
        int[] majorTriad = {0,11,19};
        int[] scaleDegrees = new int[]{0,2,4,7,9};
        for(int offset = 0; offset < mode.length; offset++){
            int[] chord = new int[scaleDegrees.length];
            for(int i = 0; i < chord.length; i++){
                chord[i] = mode[(scaleDegrees[i] + offset) % mode.length];
            }
            for(int root = 0; root < chord.length; root++){
                boolean triadContained = true;
                for(int i = 0; i < minorTriad.length; i++){
                    boolean memberContained = false;
                    for(int n = 0; n < chord.length; n++){
                        if(minorTriad[i] == (chord[n] + (33 - chord[root])) % 33){
                        memberContained = true;
                        break;
                        }
                    }
                    if(!memberContained){
                        triadContained = false;
                        break;
                    }
                }
                if(triadContained){
                    System.out.print("ROOT " + root + " MINOR: ");
                    for(int member: chord){
                        System.out.print(((33+member-chord[root])%33) + " ");//((33+member-chord[root])%33)
                    }
                    
                }

                triadContained = true;
                for(int i = 0; i < majorTriad.length; i++){
                    boolean memberContained = false;
                    for(int n = 0; n < chord.length; n++){
                        if(majorTriad[i] == (chord[n] + (33 - chord[root])) % 33){
                        memberContained = true;
                        break;
                        }
                    }
                    if(!memberContained){
                        triadContained = false;
                        break;
                    }
                }
                if(triadContained){
                    System.out.print("ROOT " + root + " MAJOR: ");
                    for(int member: chord){
                        System.out.print(((33+member-chord[root])%33) + " ");
                    }
                }
            }
        }
        
    }

    //1,25             2, 3
    //{25,0,5,11,19} {3,8,14,22,27}

    //{{0,5,11,19,25},{0,8,13,19,27},{0,5,11,19,24},{0,8,14,19,28},{0,6,11,19,25}, {0,8,14,19,27}}
    
/*
 * Modes of limited trans in 21 TET
 * a: 0 1 5 7 8 12 14 15 19   Contains [0,5,12] [7,14,19]
 * b: 0 1 3 7 8 10 14 15 17  Contains [3,8,15]  [10,17,1]
 * 
 * 
 * a':0 1 2 5 7 8 9 12 14 15 16 19
 * 
 * b':0 1 3 4 7 8 10 11 14 15 17 18 
 * b''0 1 2 3 7 8 9 10 14 15 16 17
 * 
 * 
 * a+b:0 1 3 5 7 8 10 12 14 15 17 19
 * 
 */

    public static void calculateScale(){
        for(int i = 0; i < TET; i++){
            double freq = 440 * Math.pow(2, i/(double)TET);
            double hs = 12 * (Math.log(freq / 440.0) / Math.log(2));
            double quantizedHs = Math.rint(hs);
            double residual = hs - quantizedHs;
            //System.out.println(freq + " " + hs + " " + quantizedHs + " " + residual);
            residual *= 32;
            System.out.println(NOTE_NAMES[(int)quantizedHs] + " " + Math.rint(64 + residual) + " (exact cents: " + residual / 32.0 + ")");

        }
    }

    public static void printLimTrans5(){
        printLimTrans(5);
    }

    public static void printLimTrans3(){
        printLimTrans(3);
    }

    public static void printLimTrans(int lim){
        ArrayList<ArrayList<Integer>> scales = limitedTransScales(new ArrayList<Integer>(), lim);
        for(ArrayList<Integer> scale: scales){
            for(int trans = 0; trans < (int)(TET / lim); trans++){
                int note = 0;
                System.out.print((note + trans * lim) + " ");
                for(Integer hs: scale){
                    note += hs;
                    if(note < lim)
                        System.out.print((note + trans * lim) + " ");
                }
            }
            System.out.println();
        }
    }

    public static ArrayList<ArrayList<Integer>> limitedTransScales(ArrayList<Integer> incomp, int lim){
        //ArrayList<ArrayList<Integer>> scales = new ArrayList<ArrayList<Integer>>();

        ArrayList<ArrayList<Integer>> completeScales = new ArrayList<ArrayList<Integer>>();
        int tot = 0;
        for(Integer hs: incomp)
            tot += hs;
        if(tot == lim)
        {
            completeScales.add(incomp);
            return completeScales;
        }

        for(int i = 1; i < lim + 1 - tot; i++){
            ArrayList<Integer> incompCopy = new ArrayList<Integer>();
            incompCopy.addAll(incomp);
            incompCopy.add(i);
            ArrayList<ArrayList<Integer>> compChildren = limitedTransScales(incompCopy, lim);

            for(ArrayList<Integer> novel: compChildren){
                boolean match = false;
                for(ArrayList<Integer> alreadyIncluded: completeScales){
                    if(alreadyIncluded.size() != novel.size())
                        continue;
                    for(int trans = 0; trans < novel.size(); trans++){
                        match = true;
                        for(int n = 0; n < novel.size(); n++){
                            if(alreadyIncluded.get(n) != novel.get((n + trans) % novel.size())){
                                match = false;
                                break;
                            }
                        }
                        if(match)
                            break;
                    }
                    if(match)
                        break;
                }
                if(!match)//if there's no match, add the novel set
                    completeScales.add(novel);
            }
        }

        return completeScales;
    }
}
