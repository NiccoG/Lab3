import java.io.Serializable;
import java.util.Arrays;
//this class serves as a way to create a shallow copy of a Stat object in order to avoid caching problems client-side
public class StatSnap implements Serializable {
    private final int numberPlayed;
    private final double winRate;
    private final int lastStreak;
    private final int longestStreak;
    private final int[] guessD;
    public StatSnap(Stat stat){ //initializer, copies every field of stat object passed as argument
        this.numberPlayed = stat.getNumberPlayed();
        this.winRate = stat.getWinRate();
        this.lastStreak = stat.getLastStreak();
        this.longestStreak = stat.getLongestStreak();
        this.guessD = Arrays.copyOf(stat.getGuessD(),stat.getGuessD().length); //copy to avoid same caching issues
    }
    //these methods are intuitive, and the same as in the Stat class basically
    public int getNumberPlayed() {
        return numberPlayed;
    }

    public double getWinRate() {
        return winRate;
    }

    public int getLastStreak() {
        return lastStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public int[] getGuessD() {
        return guessD;
    }
}
