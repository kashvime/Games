import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// Represents a single card
class Card {
    String rank; 
    String suit; //"♣", "♦", "♥", "♠"
    int value;   
    boolean isFaceUp; 
    boolean isMatched; 

    // Constructor
    Card(String rank, String suit, int value) {
        this.rank = rank;
        this.suit = suit;
        this.value = value;
        this.isFaceUp = false;
        this.isMatched = false;
    }

 // Draw the card
    WorldImage draw() {
        if (this.isMatched) {
            return new OverlayImage(
                new RectangleImage(50, 70, OutlineMode.SOLID, Color.GRAY),
                new RectangleImage(54, 74, OutlineMode.SOLID, Color.BLACK)
            );
        }
        if (this.isFaceUp) {
            return new OverlayImage(
                new TextImage(this.rank + this.suit, 21, this.suitColor()),
                new OverlayImage(
                    new RectangleImage(50, 70, OutlineMode.SOLID, Color.WHITE), //  background
                    new RectangleImage(54, 74, OutlineMode.OUTLINE, Color.BLACK) //  outline of card
                )
            );
        } else {
            return new OverlayImage(
                new RectangleImage(50, 70, OutlineMode.SOLID, new Color(0, 0, 128)), //  color of cards when they are facing down 
                new RectangleImage(54, 74, OutlineMode.OUTLINE, Color.BLACK) 
            );
        }
    }

    // Determine the color of the suit
    public Color suitColor() {
        if (this.suit.equals("♦") || this.suit.equals("♥")) {
            return Color.RED; // Red for hearts and diamonds
        } else {
            return Color.BLACK; // Black for spades and clubs
        }
    }


    // EFFECT: Flips the card
    void flip() {
        this.isFaceUp = !this.isFaceUp;
    }
}

//Represents the Concentration game
class ConcentrationGame extends World {
 ArrayList<ArrayList<Card>> board; 
 ArrayList<Card> flippedCards;    
 int score;                       // number of remaining pairs 
 int noMatchReset;              

 // Constructor
 ConcentrationGame() {
     this.resetGame();
 }

 // Reset the game
 public void resetGame() {
     this.board = this.initBoard();
     this.flippedCards = new ArrayList<>();
     this.score = 26; //(starting from 26 pairs from 52 cards)
     this.noMatchReset = 0; 
 }


 // Shuffles a deck of cards and creates a 4x13 grid from it
 public ArrayList<ArrayList<Card>> initBoard() {
     ArrayList<Card> deck = this.createDeck();
     this.shuffleDeck(deck);
     ArrayList<ArrayList<Card>> board = new ArrayList<>();
     int index = 0;

     for (int i = 0; i < 4; i++) {
         ArrayList<Card> row = new ArrayList<>();
         for (int j = 0; j < 13; j++) {
             row.add(deck.get(index));
             index++;
         }
         board.add(row);
     }

     return board;
 }


 // create card deck 
 public ArrayList<Card> createDeck() {
     ArrayList<Card> deck = new ArrayList<>();

     // define suits and ranks 
     ArrayList<String> suits = new ArrayList<>();
     suits.add("♣");
     suits.add("♦");
     suits.add("♥");
     suits.add("♠");

     ArrayList<String> ranks = new ArrayList<>();
     ranks.add("A");
     for (int i = 2; i <= 10; i++) {
         ranks.add(String.valueOf(i));
     }
     ranks.add("J");
     ranks.add("Q");
     ranks.add("K");

     // combine suits and ranks
     for (String suit : suits) {
         for (int i = 0; i < ranks.size(); i++) {
             deck.add(new Card(ranks.get(i), suit, i + 1));
         }
     }

     return deck;
 }

 // EFFECT: Shuffles the deck 
    public void shuffleDeck(ArrayList<Card> deck) {
        for (int i = 0; i < deck.size(); i++) {
            int randomIndex = (int) (Math.random() * deck.size());
            Card temp = deck.get(i);
            deck.set(i, deck.get(randomIndex));
            deck.set(randomIndex, temp);
        }
    }
    // Draw the game scene w/ cards and scoreDisplay 
    public WorldScene makeScene() {
        WorldScene scene = new WorldScene(800, 320);

        scene.placeImageXY(new RectangleImage(800, 320, OutlineMode.SOLID, Color.WHITE), 400, 160);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 13; j++) {
                Card card = this.board.get(i).get(j);
                WorldImage cardImage = card.draw();
                scene.placeImageXY(cardImage, 60 * j + 30, 80 * i + 40);
            }
        }

        WorldImage scoreDisplay = new TextImage("Score: " + this.score, 24, Color.BLACK);
           
        scene.placeImageXY(scoreDisplay, 400, 340); 

        
        return scene;
    }

    // EFFECT: flips a card on the board and adds it to the list of flipped cards
    public void onMouseClicked(Posn pos) {
        int row = pos.y / 80;
        int col = pos.x / 60;

        if (row < 4 && col < 13 && this.noMatchReset == 0) {
            Card card = this.board.get(row).get(col);
            if (!card.isFaceUp && !card.isMatched && this.flippedCards.size() < 2) {
                card.flip();
                this.flippedCards.add(card);

                if (this.flippedCards.size() == 2) {
                    this.checkMatch();
                }
            }
        }
    }

 // EFFECT: Check if the flipped cards match
    public void checkMatch() {
        Card card1 = this.flippedCards.get(0);
        Card card2 = this.flippedCards.get(1);

        if (card1.value == card2.value) {
            card1.isMatched = true;
            card2.isMatched = true;
            this.score -= 1;
            this.flippedCards.clear();

            if (this.score == 0) {
                this.endOfWorld("You Win!");
            }
        } else {
            this.noMatchReset = 20;
        }
    }

    //EFFECT:  resets the board by hiding unmatched cards (facing them down again) after a short delay
    public void onTick() {
        if (this.noMatchReset > 0) {
            this.noMatchReset -= 1;
            if (this.noMatchReset == 0) {
                for (Card card : this.flippedCards) {
                    card.flip();
                }
                this.flippedCards.clear();
            }
        }
    }


    // EFFECT: Resets the game state when the "r" key is pressed
    public void onKeyEvent(String key) {
        if (key.equals("r")) {
            this.resetGame();
        }
    }

 // EFFECT: Ends the game with a "You Win" message.
    public WorldScene lastScene(String msg) {
      WorldScene scene = this.makeScene();
      WorldImage winMessage = new OverlayImage(
          new TextImage(msg, 48, new Color(34, 139, 34)),
          new RectangleImage(400, 100, OutlineMode.SOLID, Color.WHITE)
      );
      scene.placeImageXY(winMessage, 400, 160);
      return scene;
  }
}

//Example tests for the Concentration Game
class ExamplesConcentrationGame {
 // Run the game
 void testRunGame(Tester t) {
     ConcentrationGame game = new ConcentrationGame();
     game.bigBang(800, 360, 0.1);
 }

 // Test for the Card class
 void testCard(Tester t) {
     Card card = new Card("A", "♠", 1);

     // Test constructor
     t.checkExpect(card.rank, "A");
     t.checkExpect(card.suit, "♠");
     t.checkExpect(card.value, 1);
     t.checkExpect(card.isFaceUp, false);
     t.checkExpect(card.isMatched, false);

     // Test draw method (face-down)
     WorldImage faceDown = new OverlayImage(
         new RectangleImage(50, 70, OutlineMode.SOLID, new Color(0, 0, 128)),
         new RectangleImage(54, 74, OutlineMode.OUTLINE, Color.BLACK)
     );
     t.checkExpect(card.draw(), faceDown);

     // Test flip method
     card.flip();
     t.checkExpect(card.isFaceUp, true);
     WorldImage faceUp = new OverlayImage(
         new TextImage("A♠", 21, Color.BLACK), // Match font size
         new OverlayImage(
             new RectangleImage(50, 70, OutlineMode.SOLID, Color.WHITE),
             new RectangleImage(54, 74, OutlineMode.OUTLINE, Color.BLACK)
         )
     );
     t.checkExpect(card.draw(), faceUp);

     // Test flip back
     card.flip();
     t.checkExpect(card.isFaceUp, false);

     // Test matched card
     card.isMatched = true;
     WorldImage matched = new OverlayImage(
         new RectangleImage(50, 70, OutlineMode.SOLID, Color.GRAY), // Match color
         new RectangleImage(54, 74, OutlineMode.SOLID, Color.BLACK)
     );
     t.checkExpect(card.draw(), matched);
 }

 // Test for the ConcentrationGame class
 void testConcentrationGame(Tester t) {
     ConcentrationGame game = new ConcentrationGame();

     // Test board initialization
     t.checkExpect(game.board.size(), 4);
     for (ArrayList<Card> row : game.board) {
         t.checkExpect(row.size(), 13);
     }

     // Test flipping cards via onMouseClicked
     Card card1 = game.board.get(0).get(0);
     game.onMouseClicked(new Posn(30, 40));
     t.checkExpect(card1.isFaceUp, true); // Expect card1 to be flipped
     t.checkExpect(game.flippedCards.size(), 1); // Ensure it's added to flippedCards

     // Test reset functionality
     game.onKeyEvent("r");
     t.checkExpect(game.score, 26);
     t.checkExpect(game.flippedCards.size(), 0);
 }

 // Test card matching logic
 void testCardFlipAndMatch(Tester t) {
     ConcentrationGame game = new ConcentrationGame();

     // Flip two matching cards
     Card card1 = game.board.get(0).get(0);
     Card card2 = new Card(card1.rank, card1.suit, card1.value); // Duplicate card
     game.board.get(1).set(0, card2); // Place it on the board

     game.onMouseClicked(new Posn(30, 40)); // Flip card1
     game.onMouseClicked(new Posn(30, 120)); // Flip card2
     t.checkExpect(card1.isFaceUp, true);
     t.checkExpect(card2.isFaceUp, true);
     t.checkExpect(card1.isMatched, true);
     t.checkExpect(card2.isMatched, true);

     // Score should decrease by 1
     t.checkExpect(game.score, 25);
 }

 // Test mismatch logic
 void testMismatchLogic(Tester t) {
     ConcentrationGame game = new ConcentrationGame();

     // Flip two non-matching cards
     Card card1 = game.board.get(0).get(0);
     Card card2 = game.board.get(0).get(1);
     game.onMouseClicked(new Posn(30, 40)); // Flip card1
     game.onMouseClicked(new Posn(90, 40)); // Flip card2
     t.checkExpect(card1.isFaceUp, true);
     t.checkExpect(card2.isFaceUp, true);

     // Simulate 20 ticks for delay
     for (int i = 0; i < 20; i++) {
         game.onTick();
     }

     // Cards should flip back
     t.checkExpect(card1.isFaceUp, false);
     t.checkExpect(card2.isFaceUp, false);
     t.checkExpect(game.flippedCards.size(), 0);
 }

 // Test makeScene method
 void testMakeScene(Tester t) {
     ConcentrationGame game = new ConcentrationGame();
     WorldScene scene = new WorldScene(800, 320);

     // Add background
     scene.placeImageXY(new RectangleImage(800, 320, OutlineMode.SOLID, Color.WHITE), 400, 160); // Match color

     // Add cards
     for (int i = 0; i < 4; i++) {
         for (int j = 0; j < 13; j++) {
             Card card = game.board.get(i).get(j);
             scene.placeImageXY(card.draw(), 60 * j + 30, 80 * i + 40);
         }
     }

     // Add score display
     WorldImage scoreBox = new TextImage("Score: " + game.score, 24, Color.BLACK); // Match font size
     scene.placeImageXY(scoreBox, 400, 340); // Match position

     t.checkExpect(game.makeScene(), scene);
 }

 // Test lastScene method
 void testLastScene(Tester t) {
   ConcentrationGame game = new ConcentrationGame();
   WorldScene winScene = game.makeScene();
   // Add win message
   WorldImage winMessage = new OverlayImage(
       new TextImage("You Win!", 48, new Color(34, 139, 34)), 
       new RectangleImage(400, 100, OutlineMode.SOLID, Color.WHITE)
   );
   winScene.placeImageXY(winMessage, 400, 160);
   t.checkExpect(game.lastScene("You Win!"), winScene);
}
}
