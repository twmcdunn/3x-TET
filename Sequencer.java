import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Comparator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
/**
 * Write a description of class Game here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Sequencer
{
    public final int GAME_LENGTH = 45;//24;//24;//192;//174;//198;//29;//max possible = size of triad space * 12 / size of syntagm
    public Board sourceSyntagm;
    public ArrayList<Game> gameTree;
    public int globalMax;
    public double globalMaxAverage, localMaxAverage, tollerableAverage, tollerableMin, minAveRep;
    public Game myGame;
    public ArrayList<Triad> telos;
    public int maxGameLengthSoFar, minRepNotes, maxAllowedRep;
    public Random rand;

    Sequencer(){
        sourceSyntagm = new Board();
        initializeVariables();
        initializeHardCodedSource();
        rand = new Random(123);
        Game sourceGame = new Game(sourceSyntagm);
        myGame = sourceGame;
    }

    public static void main(String[] args){
        new Sequencer().playGames();
    }

    public void initializeVariables(){
        globalMax = 3; // max of minsyntacticdistance in a game 
        globalMaxAverage = 0.0; // max of average distance in a game
        localMaxAverage = 0.0; // max of minLocalAverage in a game
        tollerableAverage = 2.4; // findAGoodGame continues to play
        tollerableMin = 2; // if useMin in findAGoodGame, then only continue if above tollerable min
        maxGameLengthSoFar = 0;// used only to monitor progress, doesn't factor into search calculations
        minRepNotes = 30;//Integer.MAX_VALUE;//smallest num of repeated notes used in a complete game
        minAveRep  = 2.584;// average rep notes per board in best game found
        maxAllowedRep = 1;//limits num of rep notes allowed in any board

        Triad.ascending  = true;

    }

    public void addTriadToSource(int type, int root){
        sourceSyntagm.add(new Triad(type, root));
    }

    public void initializeHardCodedSource(){
        //int[][] hardCodedSource = new int[][]{{0,0},{1,2},{3,1}};
        int[][] hardCodedSource = new int[][]{{0,6},{1,1}};//,{5,2}};
        for(int i = 0; i < hardCodedSource.length; i++)
            sourceSyntagm.add(new Triad(hardCodedSource[i][0], hardCodedSource[i][1]));

        int[][] hardCodedTelos = new int[][]{{0,10},{1,8}};
        //int[][] hardCodedTelos = new int[][]{{1,8}, {3,10}};
        telos = new ArrayList<Triad>();
        for(int i = 0; i < hardCodedTelos.length; i++)
            telos.add(new Triad(hardCodedTelos[i][0], hardCodedTelos[i][1]));

        telos = null;
        System.out.println("Source: " + sourceSyntagm);
        System.out.println("Telos: " + telos);
        System.out.println("minDist = " + sourceSyntagm.getMinSyntacticDistance());
    }

    public static void testTraids(){
        int mdist = 0;
        for(int a = 0; a < Triad.triadDictionary.length; a++)
            for(int b =  0; b < Triad.triadDictionary.length; b++)
                for(int d = 0; d < 15; d++){

                    Board brd = new Board();
                    brd.add(new Triad(a,0));
                    brd.add(new Triad(b,d));
                    if(true && Triad.triadDictionary[a].length != Triad.triadDictionary[b].length)
                        continue;
                    if(brd.getMinSyntacticDistance() >= mdist){
                        mdist = brd.getMinSyntacticDistance();
                        System.out.println(brd);
                    }

                }

        Board brd = new Board();
        brd.add(new Triad(3,0));
        brd.add(new Triad(0,6));
        System.out.println(brd);

    }

    public void playGames(){
        for(int i =   0;  i < 7; i++)
            System.out.println();

        for(int i = 0; i < 100; i++)
            getChords();
    }

    private ArrayList<Board> getAllPossibleMoves(Game currentGame){
        ArrayList<Board> incompleteBoards = new ArrayList<Board>();
        incompleteBoards.add(new Board());
        ArrayList<Board> completeBoards = new ArrayList<Board>();
        Board lastBoard = currentGame.getLastBoard();
        for(Triad t: lastBoard){
            completeBoards = new ArrayList<Board>();
            for(int i = 0; i < t.transformationGroup.length; i++){//directedTransformationGroup
                Triad transformed = t.generateTransformed(i);//generateDirectedTransformed
                if(
                //currentGame.notUsed(transformed) && 
                !currentGame.getLastBoard().contains(transformed) &&
                ((notTelos(transformed) && currentGame.size() < GAME_LENGTH - 1) ||
                    (telos == null || !notTelos(transformed) && currentGame.size() == GAME_LENGTH - 1))){
                    for(Board b: incompleteBoards){
                        if(b.contains(transformed))
                            continue;
                        b = new Board(b);
                        b.add(transformed);
                        //check if board fits in scale
                        if(b.fitsMode())
                            completeBoards.add(b);
                    }
                }
            }
            if(completeBoards.size() == 0)//no possible transformation
                return null;
            incompleteBoards = completeBoards;
        }
        //Collections.shuffle(completeBoards);
        return completeBoards;
    }

    public int[][] getChords(){
        ArrayList<Board> allPossibleMoves = getAllPossibleMoves(myGame);
        ArrayList<Game> allPossibleGames = new ArrayList<Game>();

        //chordPopulation (number of times chords in new boards have already occured in the game
        int bestChordPop = Integer.MAX_VALUE;
        for(Board move: allPossibleMoves){
            Game game = new Game(myGame);
            game.makeMove(move);
            allPossibleGames.add(game);

            int chordPop = 0;
            for(Triad t: game.getLastBoard())
                chordPop += game.grid[t.type][t.root];
            bestChordPop = Math.min(bestChordPop, chordPop);

        }
        
        //of the best chord populations, find the best
        //synatic distances
        int best = 0;
        for(Game game: allPossibleGames){
            int chordPop = 0;
            for(Triad t: game.getLastBoard())
                chordPop += game.grid[t.type][t.root];
            if(chordPop == bestChordPop){
                best = Math.max(best, game.minSyntacticDistance);
            }
        }

        System.out.println("BEST DISTANCE: " + best + " BEST CHORD POP: " + bestChordPop);
        
        //build list of games filtered based on the above
        //(first by chord pop, then by syntactic distance)
        ArrayList<Game> bestGames = new ArrayList<Game>();
        for(Game game: allPossibleGames){
            int chordPop = 0;
            for(Triad t: game.getLastBoard())
                chordPop += game.grid[t.type][t.root];
            if(game.minSyntacticDistance == best && chordPop == bestChordPop)
                bestGames.add(game);
        }
        
        //pick randomly from the filtered game options
        System.out.println("NUM OF OPTIONS: " + bestGames.size());
        myGame = bestGames.get((int)(rand.nextDouble() * bestGames.size()));

        Board lastBoard = myGame.getLastBoard();
        int[][] chords = new int[lastBoard.size()][];
        for(int i = 0; i < lastBoard.size(); i++){
            ArrayList<Integer> notes = lastBoard.get(i).notes();
            chords[i] = new int[notes.size()];
            for(int n = 0; n < notes.size(); n++)
                chords[i][n] = notes.get(n);
        }
        return chords;
    }

    private Game findAGoodGame(Game incompleteGame){
        //System.out.println("CURRENT LENGTH = " + incompleteGame.size());
        if(false && (incompleteGame.minSyntacticDistance < globalMax
            || (incompleteGame.aveSyntacticDistance < tollerableAverage && incompleteGame.size() > 10)))
            return null;
        if(false && aveRepNotes(incompleteGame) > minAveRep){
            return null;
        }
        if(incompleteGame.size() == GAME_LENGTH){
            minRepNotes = Math.min(minRepNotes, repeatedNotes(incompleteGame));
            minAveRep = Math.min(minAveRep, aveRepNotes(incompleteGame));
            //System.out.println("REP NOTES: " + minRepNotes);
        }

        Game game = null;
        ArrayList<Board> allPossibleMoves = getAllPossibleMoves(incompleteGame);
        if(allPossibleMoves == null){
            if(incompleteGame.size() == GAME_LENGTH && incompleteGame.aveSyntacticDistance >= globalMaxAverage){
                // && incompleteGame.aveSyntacticDistance >= globalMaxAverage){
                // this should be guarenteeed by method get optimal moves:
                //&& incompleteGame.minSyntacticDistance > globalMax
                globalMax = incompleteGame.minSyntacticDistance;
                globalMaxAverage = incompleteGame.aveSyntacticDistance;
                tollerableAverage = (globalMaxAverage * 0.5) + (tollerableAverage * 0.5);
                //tollerableAverage = globalMaxAverage;
                game = incompleteGame;
                System.out.println("BETTER GAME FOUND:\n" + game);
                //game.printGame();//common tones at a glance
                System.out.println("GLOBAL MAX = " + globalMax);
                System.out.println("GLOBAL MAX AVERAGE = " + globalMaxAverage);
                System.out.println("TOLLERABLE AVERAGE = " + tollerableAverage);
                //myGame = game;
                return game;
            }
            // System.out.println("DEADEND");
            if(incompleteGame.size() > maxGameLengthSoFar){
                System.out.println("LONGEST GAME: " + incompleteGame.size());
                System.out.println(incompleteGame);
                maxGameLengthSoFar = incompleteGame.size();
            }
            return null;
        }

        int best = 0;
        for(Board move: allPossibleMoves){
            best = Math.max(move.getMinSyntacticDistance(), best);
        }
        int num = 0;
        for(Board move: allPossibleMoves){
            if(move.getMinSyntacticDistance() == best)
                num++;
        }
        System.out.println("BEST DISTANCE: " + best + " NUM OF OPTIONS: " +  num);
        System.out.println(incompleteGame);
        for(Board move: allPossibleMoves){
            if(numOfRepNotes(move) > maxAllowedRep)//false && !lacksRepNotes(move))//true && hasCommonTone(incompleteGame.get(incompleteGame.size() - 1),move))
                continue;
            Game gameToMove = new Game(incompleteGame);
            gameToMove.makeMove(move);
            Game completeGame = findAGoodGame(gameToMove); // depth first search
            if(completeGame != null){
                game = completeGame;
            }
        }
        //System.out.println(completeGames.size());
        return game;
    }

    public static double aveRepNotes(Game g){
        return repeatedNotes(g) / (double)g.size();
    }

    public static int repeatedNotes(Game g){
        int reps  = 0;
        for(Board b: g){
            reps += numOfRepNotes(b);
        }
        return reps;
    }

    public static boolean lacksRepNotes(Board b){
        ArrayList<Integer> notes = new ArrayList<Integer>();
        for(Triad chord: b){
            notes.addAll(chord.notes());
        }
        for(int n1 = 0; n1 < notes.size(); n1++)
            for(int n2 = n1 + 1; n2 <  notes.size(); n2++)
                if(notes.get(n1).equals(notes.get(n2)))
                    return false;
        return true;
    }

    public static int numOfRepNotes(Board b){
        int reps  = 0;
        ArrayList<Integer> notes = new ArrayList<Integer>();
        for(Triad chord: b){
            notes.addAll(chord.notes());
        }
        for(int n1 = 0; n1 < notes.size(); n1++)
            for(int n2 = n1 + 1; n2 <  notes.size(); n2++)
                if(notes.get(n1).equals(notes.get(n2)))
                    reps++;
        return reps;
    }

    public static void test(){
        System.out.println((new Integer(1)).equals(new Integer(1)));
    }

    public boolean hasCommonTone(Board b1, Board b2){
        ArrayList<Triad> chords = new ArrayList<Triad>();
        chords.add(b1.get(b1.size() - 1));
        chords.addAll(b2);
        for(int i = 0; i < chords.size() - 1; i++){
            Triad t1 = chords.get(i);
            Triad t2 = chords.get(i + 1);
            for(int m1: Triad.triadDictionary[t1.type]){
                for(int m2: Triad.triadDictionary[t2.type]){
                    if((m1 + t1.root) % 15 == (m2 + t2.root) % 15)
                        return true;
                }
            }
        }
        return false;
    }

    public boolean notTelos(Triad t){
        if(telos == null)
            return true;
        for(Triad aT: telos)
            if(t.equals(aT))
                return false;
        return true;
    }

}
