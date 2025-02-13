import java.util.Random; // Import Random
import java.util.Scanner; // Import Scanner

public class RockPaperScissors {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        while (true) {
            // Define the list of moves as a string array
            String[] rps = {"r", "p", "s"};

            // Generate a random move
            Random random = new Random();
            String computerMove = rps[random.nextInt(rps.length)];

            String playerMove;

            // Validate player's move
            while (true) {
                System.out.println("Please enter your move (r, p, or s):");
                playerMove = scanner.nextLine().toLowerCase();

                if (playerMove.equals("r") || playerMove.equals("p") || playerMove.equals("s")) {
                    break; // Exit the loop if valid move
                }
                System.out.println(playerMove + " is not a valid move");
            }

            System.out.println("Computer played: " + computerMove);

            // Determine the result
            if (playerMove.equals(computerMove)) {
                System.out.println("The game was a tie!");
            } else if (playerMove.equals("r") && computerMove.equals("s") ||
                       playerMove.equals("p") && computerMove.equals("r") ||
                       playerMove.equals("s") && computerMove.equals("p")) {
                System.out.println("You Win!");
            } else {
                System.out.println("You Lose!");
            }

            // Ask if the player wants to play again
            System.out.println("Do you want to play again? (y/n):");
            String playAgain = scanner.nextLine().toLowerCase();
            if (!playAgain.equals("y")) {
                System.out.println("Thanks for playing! Goodbye!");
                break; // Exit the main loop
            }
        }

        scanner.close(); 
    }
}
