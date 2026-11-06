import java.util.*;

class Team {

    String name;
    int points;
    int goalDifference;

    Team(String name, int points, int goalDifference) {
        this.name = name;
        this.points = points;
        this.goalDifference = goalDifference;
    }

    public String toString() {
        return name + " - Points: " + points
                + ", Goal Difference: " + goalDifference;
    }
}

public class Task5 {

    public static void main(String[] args) {

        ArrayList<Team> teams = new ArrayList<>();

        teams.add(new Team("Panthers", 9, 4));
        teams.add(new Team("Eagles", 9, 6));
        teams.add(new Team("Cobras", 6, 1));
        teams.add(new Team("Bisons", 9, 4));

        Comparator<Team> ranking =
                Comparator.comparingInt((Team t) -> t.points)
                        .reversed()
                        .thenComparing(
                                Comparator.comparingInt(
                                        (Team t) -> t.goalDifference
                                ).reversed()
                        )
                        .thenComparing(t -> t.name);

        teams.sort(ranking);

        System.out.println("Team Leaderboard:");

        int rank = 1;

        for (Team t : teams) {
            System.out.println(rank + ". " + t);
            rank++;
        }
    }
}