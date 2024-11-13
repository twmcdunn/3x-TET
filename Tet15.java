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
    public static int TET = 21;
    public static void main(String[] args){
        printLimTrans(7);
        //calculateScale();
        System.out.println(700/57.14285714);
    }
    
/*
 * Modes of limited trans in 21 TET
 * 0 1 5 7 8 12 14 15 19   Contains [0,5,12] [7,14,19]
 * 0 1 3 7 8 10 14 15 17  Contains [3,8,15]  [10,17,1]
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
