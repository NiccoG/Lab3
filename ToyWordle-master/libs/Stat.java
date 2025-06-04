import java.io.Serializable;
//this class is the structure which keeps track of stats, one exists per user registered, methods to update are
//also here
public class Stat implements Serializable {
    private int numberPlayed;
    private int numberWon;
    private int lastStreak;
    private int longestStreak;
    private final int[] guessD;

    public Stat(){ //first initialization, no games played
        numberPlayed=0;
        numberWon=0;
        lastStreak=0;
        longestStreak=0;
        guessD=new int[6];
    }

    public int getNumberPlayed(){ //getter for number of games played
        return numberPlayed;
    }

    public double getWinRate(){ //getter for winRate
        if(numberPlayed==0) //avoid division by zero
            return 0;
        return Math.round((double)numberWon/(double)numberPlayed*100)/100.0;
    }

    public int getLastStreak(){ //getter for current streak
        return lastStreak;
    }

    public int getLongestStreak(){ //getter for longest streak
        return longestStreak;
    }

    public int[] getGuessD(){ //getter for guess distribution
        return guessD;
    }

    public void win(int row){ //update according to win
        numberPlayed++;
        numberWon++;
        lastStreak++;
        if(lastStreak>longestStreak)
            longestStreak=lastStreak;
        guessD[row]++;
    }

    public void lose(){ //update according to lost
        numberPlayed++;
        lastStreak=0;
    }
}
