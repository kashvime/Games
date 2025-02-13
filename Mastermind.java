import java.awt.Color;
import java.util.Random;
import javalib.funworld.*;
import javalib.worldimages.*;
import tester.Tester;

// Class representing a color in the game
class MyColor {
  String name;
  Color color;

  MyColor(String name, Color color) {
    this.name = name;
    this.color = color;
  }

  // Get the image of this color
  public WorldImage getImage() {
    return new CircleImage(20, OutlineMode.SOLID, this.color);
  }

  // Check if this color is the same as the given color
  boolean sameColor(MyColor other) {
    return this.name.equals(other.name);
  }
}

// Interface for list structure (IList)
interface IList<T> {
  
  int length();
  
  T getElementAt(int index);
  
  IList<T> removeElementAt(int index);
  
  IList<T> removeLast();
  
  int indexOf(T element);
}

// Class representing an empty list
class MtLoList<T> implements IList<T> {
  public int length() {
    return 0;
  }
  
  public int indexOf(T element) {
    return -1;
  }

  public T getElementAt(int index) {
    throw new IndexOutOfBoundsException("Index out of bounds");
  }

  public IList<T> removeElementAt(int index) {
    return this;
  }
  
  public IList<T> removeLast() {
    return this;
  }
}

// Class representing a non-empty list
class ConsLoList<T> implements IList<T> {
  T first;
  IList<T> rest;

  ConsLoList(T first, IList<T> rest) {
    this.first = first;
    this.rest = rest;
  }

  public int indexOf(T element) {
    if (this.first.equals(element)) {
      return 0;
    } else {
      int indexInRest = this.rest.indexOf(element);
      if (indexInRest == -1) {
        return -1;
      } else {
        return 1 + indexInRest;
      }
    }
  }

  public int length() {
    return 1 + this.rest.length();
  }

  public T getElementAt(int index) {
    if (index == 0) {
      return this.first;
    } else {
      return this.rest.getElementAt(index - 1);
    }
  }

  public IList<T> removeElementAt(int index) {
    if (index == 0) {
      return this.rest;
    } else {
      return new ConsLoList<>(this.first, this.rest.removeElementAt(index - 1));
    }
  }
  
  public IList<T> removeLast() {
    if (this.rest.length() == 0) {
      return new MtLoList<>();
    } else {
      return new ConsLoList<>(this.first, this.rest.removeLast());
    }
  }
}

// Class representing an unfinished guess
class UnfinishedGuess {
  IList<MyColor> colors;
  int requiredLength;

  UnfinishedGuess(IList<MyColor> colors, int requiredLength) {
    this.colors = colors;
    this.requiredLength = requiredLength;
  }

  // Add a color to the guess
  public UnfinishedGuess addColor(MyColor color) {
    if (this.colors.length() < this.requiredLength) {
      return new UnfinishedGuess(
          new ConsLoList<>(color, this.colors), this.requiredLength);
    } else {
      return this;
    }
  }

  // Remove the last color
  public UnfinishedGuess removeLastColor() {
    if (this.colors.length() > 0) {
      return new UnfinishedGuess(
          this.colors.removeElementAt(0), this.requiredLength);
    } else {
      return this;
    }
  }

  // Check if the guess is complete
  public boolean isComplete() {
    return this.colors.length() == this.requiredLength;
  }

  // Draw the guess
  public WorldImage draw(
      int sequenceLength, boolean showColors, Color hiddenColor) {
    return drawHelper(this.colors, sequenceLength, showColors, hiddenColor, 0);
  }

  public WorldImage drawWithFeedback(int sequenceLength, boolean showColors,
      Color hiddenColor, Feedback feedback) {
    // Draw the guess (the sequence of colors)
    WorldImage guessImage = this.draw(sequenceLength, showColors, hiddenColor);

    // Draw the feedback to the right of the guess
    WorldImage feedbackImage = feedback.draw();

    // Combine the guess and feedback side-by-side
    return new BesideImage(guessImage, feedbackImage);
  }

  // Helper to draw guess
  public WorldImage drawHelper(IList<MyColor> colors, int sequenceLength,
      boolean showColors, Color hiddenColor, int currentIndex) {
    if (sequenceLength == 0) {
      return new EmptyImage();
    } else {
      WorldImage restImage = drawHelper(colors, sequenceLength - 1, showColors,
          hiddenColor, currentIndex + 1);
      WorldImage colorImage;

      if (currentIndex < colors.length()) {
        MyColor color = colors.getElementAt(currentIndex);
        if (showColors) {
          colorImage = color.getImage();
        } else {
          colorImage = new CircleImage(20, OutlineMode.SOLID, hiddenColor);
        }
      } else {
        colorImage = new CircleImage(20, OutlineMode.SOLID, hiddenColor);
      }

      return new BesideImage(colorImage, restImage);
    }
  }
}

// Feedback class to represent the result of a guess (exact and inexact matches)
class Feedback {
  int exactMatches;
  int inexactMatches;

  Feedback(int exactMatches, int inexactMatches) {
    this.exactMatches = exactMatches;
    this.inexactMatches = inexactMatches;
  }

  // Draw feedback as text (or you can use graphical symbols if you want)
  public WorldImage draw() {
    String feedbackText =
        "Exact: " + this.exactMatches + " | Inexact: " + this.inexactMatches;
    return new TextImage(feedbackText, 20, Color.BLACK);
  }
}

// Class for the Mastermind game logic
class MastermindGame extends World {
  IList<MyColor> availableColors;
  IList<MyColor> secretCode;
  int maxGuesses;
  int sequenceLength;
  boolean allowDuplicates;
  IList<UnfinishedGuess> pastGuesses;
  UnfinishedGuess currentGuess;
  int remainingGuesses;
  Random rand;
  boolean gameOver;
  boolean playerWon;

  IList<Feedback> feedbacks = new MtLoList<>();

  public MastermindGame(int sequenceLength, int maxGuesses,
      boolean allowDuplicates, Random rand) {
    this.availableColors = initializeColors();
    this.sequenceLength = sequenceLength;
    this.maxGuesses = maxGuesses;
    this.allowDuplicates = allowDuplicates;
    this.rand = rand;
    this.secretCode = generateSecretCode();
    this.pastGuesses = new MtLoList<>();
    this.currentGuess = new UnfinishedGuess(new MtLoList<>(), sequenceLength);
    this.remainingGuesses = maxGuesses;
    this.gameOver = false;
    this.playerWon = false;

    this.feedbacks = new MtLoList<>();
  }

  // Initialize the available colors
  public IList<MyColor> initializeColors() {
    return new ConsLoList<>(new MyColor("Red", Color.RED),
        new ConsLoList<>(new MyColor("Green", Color.GREEN),
            new ConsLoList<>(new MyColor("Blue", Color.BLUE),
                new ConsLoList<>(
                    new MyColor("Yellow", Color.YELLOW), new MtLoList<>()))));
  }

  // Generate a secret code
  public IList<MyColor> generateSecretCode() {
    return generateSecretCodeHelper(
        this.sequenceLength, this.availableColors, new MtLoList<>());
  }

  public IList<MyColor> generateSecretCodeHelper(
      int n, IList<MyColor> colors, IList<MyColor> acc) {
    if (n == 0) {
      return acc;
    } else {
      int index = this.rand.nextInt(colors.length());
      MyColor selectedColor = colors.getElementAt(index);
      IList<MyColor> newAcc = new ConsLoList<>(selectedColor, acc);
      if (this.allowDuplicates) {
        return this.generateSecretCodeHelper(n - 1, colors, newAcc);
      } else {
        IList<MyColor> newColors = colors.removeElementAt(index);
        return this.generateSecretCodeHelper(n - 1, newColors, newAcc);
      }
    }
  }

  public World onKeyEvent(String key) {
    if (this.gameOver) {
      return this; // Do nothing if the game is over
    }

    if (isNumberKey(key)) {
      return handleNumberKey(key);
    }

    if (key.equals("enter")) {
      return handleEnterKey();
    }

    if (key.equals("backspace")) {
      return handleBackspaceKey();
    }

    return this;
  }

  // Handle number key press and add color to the guess
  public MastermindGame handleNumberKey(String key) {
    int num = Integer.parseInt(key);
    if (num >= 1 && num <= this.availableColors.length()) {
      MyColor selectedColor = this.availableColors.getElementAt(num - 1);
      this.currentGuess = this.currentGuess.addColor(selectedColor);
    }
    this.makeScene(); // Update the scene after the color is added
    return this;
  }

  public MastermindGame handleEnterKey() {
    if (this.currentGuess.isComplete()) {
      // Evaluate the current guess against the secret code
      Feedback feedback = evaluateGuess(this.currentGuess.colors);

      // Store the guess and its feedback
      this.pastGuesses = new ConsLoList<>(this.currentGuess, this.pastGuesses);
      this.feedbacks = new ConsLoList<>(
          feedback, this.feedbacks); // Store feedback with the guess

      // Update the number of remaining guesses
      this.remainingGuesses -= 1;

      // Check if the player won
      if (feedback.exactMatches == this.sequenceLength) {
        this.gameOver = true;
        this.playerWon = true;
      }

      // End game if no guesses left
      if (this.remainingGuesses == 0 && !this.playerWon) {
        this.gameOver = true;
      }

      // Reset the current guess for the next round
      this.currentGuess =
          new UnfinishedGuess(new MtLoList<>(), this.sequenceLength);
    }

    return this;
  }

  public MastermindGame handleBackspaceKey() {
    this.currentGuess = this.currentGuess.removeLastColor();
    return this;
  }

  // check if the key is a valid number (1-9)
  public boolean isNumberKey(String key) {
    return "123456789".contains(key);
  }

  // Evaluate a guess and return feedback
  public Feedback evaluateGuess(IList<MyColor> guessColors) {
    int exactMatches = countExactMatches(this.secretCode, guessColors);

    IList<MyColor> secretWithoutExactMatches =
        removeExactMatches(this.secretCode, guessColors);
    IList<MyColor> guessWithoutExactMatches =
        removeExactMatches(guessColors, this.secretCode);

    int inexactMatches = countInexactMatches(
        secretWithoutExactMatches, guessWithoutExactMatches);

    return new Feedback(exactMatches, inexactMatches);
  }

  // Remove colors that are exact matches to avoid counting them in inexact
  // matches
  public IList<MyColor> removeExactMatches(
      IList<MyColor> secret, IList<MyColor> guess) {
    if (secret.length() == 0 || guess.length() == 0) {
      return new MtLoList<>();
    } else {
      MyColor secretFirst = secret.getElementAt(0);
      MyColor guessFirst = guess.getElementAt(0);

      if (secretFirst.sameColor(guessFirst)) {
        return removeExactMatches(
            secret.removeElementAt(0), guess.removeElementAt(0));
      } else {
        return new ConsLoList<>(secretFirst,
            removeExactMatches(
                secret.removeElementAt(0), guess.removeElementAt(0)));
      }
    }
  }

  // Count inexact matches
  public int countInexactMatches(IList<MyColor> secret, IList<MyColor> guess) {
    if (guess.length() == 0) {
      return 0;
    } else {
      MyColor guessFirst = guess.getElementAt(0);
      if (containsColor(secret, guessFirst)) {
        return 1
            + countInexactMatches(
                secret.removeElementAt(secret.indexOf(guessFirst)),
                guess.removeElementAt(0));
      } else {
        return countInexactMatches(secret, guess.removeElementAt(0));
      }
    }
  }

  // Count exact matches
  public int countExactMatches(IList<MyColor> secret, IList<MyColor> guess) {
    return countMatches(secret, guess, true);
  }

  // count total matches
  public int countTotalMatches(IList<MyColor> secret, IList<MyColor> guess) {
    return countMatches(secret, guess, false);
  }

  // helper to count matches
  public int countMatches(
      IList<MyColor> secret, IList<MyColor> guess, boolean exactOnly) {
    if (secret.length() == 0 || guess.length() == 0) {
      return 0;
    } else {
      MyColor secretFirst = secret.getElementAt(0);
      MyColor guessFirst = guess.getElementAt(0);
      int match = 0;
      if (secretFirst.sameColor(guessFirst) && exactOnly) {
        match = 1;
      } else {
        if (!exactOnly && containsColor(secret, guessFirst)) {
          match = 1;
        }
      }
      return match
          + countMatches(
              secret.removeElementAt(0), guess.removeElementAt(0), exactOnly);
    }
  }

  // Helper to check if a color exists in a list
  public boolean containsColor(IList<MyColor> colors, MyColor color) {
    if (colors.length() == 0) {
      return false;
    } else {
      MyColor first = colors.getElementAt(0);
      if (first.sameColor(color)) {
        return true;
      } else {
        return containsColor(colors.removeElementAt(0), color);
      }
    }
  }

  // Create the game scene with past and current guesses
  public WorldScene makeScene() {
    int width = 600;
    int height = 800;
    WorldScene scene = new WorldScene(width, height);

    // Draw the secret code (hidden unless the game is over)
    WorldImage secretCodeImage = this.drawSecretCode();
    scene = scene.placeImageXY(secretCodeImage, width / 2, 50);

    // Draw past guesses with feedback
    WorldImage pastGuessesImage =
        this.drawPastGuesses(this.pastGuesses, this.feedbacks);
    scene = scene.placeImageXY(pastGuessesImage, width / 2, height / 2 - 100);

    // Draw the current guess in progress
    WorldImage currentGuessImage = this.drawCurrentGuess();
    scene = scene.placeImageXY(currentGuessImage, width / 2, height / 2 + 100);

    // Draw available colors for the player to choose from
    WorldImage availableColorsImage = this.drawAvailableColors();
    scene = scene.placeImageXY(availableColorsImage, width / 2, height - 50);

    // Display win/lose message if the game is over
    if (this.gameOver) {
      WorldImage message;
      if (this.playerWon) {
        message = new TextImage("You won!", 30, Color.GREEN);
      } else {
        message = new TextImage("You lost!", 30, Color.RED);
      }
      scene = scene.placeImageXY(message, width / 2, height / 2 + 150);
    }

    return scene;
  }

  //  draw the past guesses
  public WorldImage drawPastGuesses(
      IList<UnfinishedGuess> guesses, IList<Feedback> feedbacks) {
    if (guesses.length() == 0 || feedbacks.length() == 0) {
      return new EmptyImage();
    } else {
      UnfinishedGuess guess = guesses.getElementAt(0);
      Feedback feedback = feedbacks.getElementAt(0);

      // Draw the guess with feedback
      WorldImage guessWithFeedbackImage = guess.drawWithFeedback(
          this.sequenceLength, true, Color.GRAY, feedback);

      // Recursively draw the rest of the guesses with feedback
      return new AboveImage(guessWithFeedbackImage,
          drawPastGuesses(
              guesses.removeElementAt(0), feedbacks.removeElementAt(0)));
    }
  }

  // Draw the current guess in progress
  public WorldImage drawCurrentGuess() {
    return this.currentGuess.draw(this.sequenceLength, true, Color.LIGHT_GRAY);
  }

  // Draw the secret code (revealed when the game is over)
  public WorldImage drawSecretCode() {
    return this.currentGuess.draw(
        this.sequenceLength, this.gameOver, Color.GRAY);
  }

  // Draw available colors
  public WorldImage drawAvailableColors() {
    return drawColorsHelper(this.availableColors, 1);
  }

  // Helper to draw available colors
  public WorldImage drawColorsHelper(IList<MyColor> colors, int index) {
    if (colors.length() == 0) {
      return new EmptyImage();
    } else {
      MyColor color = colors.getElementAt(0);
      WorldImage restImage =
          drawColorsHelper(colors.removeElementAt(0), index + 1);
      String label = Integer.toString(index);
      WorldImage colorImage = new OverlayImage(
          color.getImage(), new TextImage(label, 15, Color.BLACK));
      return new BesideImage(colorImage, restImage);
    }
  }
}

class MastermindWorld extends MastermindGame {
  MastermindWorld() {
    super(4, 10, true, new Random());
    this.bigBang(600, 800, 0.1);
  }
}

class ExamplesMastermind {
  // Examples of colors
  MyColor red = new MyColor("Red", Color.RED);
  MyColor anotherRed = new MyColor("Red", Color.RED);
  MyColor green = new MyColor("Green", Color.GREEN);
  MyColor blue = new MyColor("Blue", Color.BLUE);
  MyColor yellow = new MyColor("Yellow", Color.YELLOW);

  // examples of lists of colors
  IList<MyColor> emptyList = new MtLoList<>();
  IList<MyColor> oneElementList = new ConsLoList<>(red, emptyList);
  IList<MyColor> twoElementList = new ConsLoList<>(green, oneElementList);
  IList<MyColor> exampleSecretCode = new ConsLoList<>(red,
      new ConsLoList<>(green,
          new ConsLoList<>(blue, new ConsLoList<>(yellow, new MtLoList<>()))));
  IList<MyColor> correctGuess = new ConsLoList<>(red,
      new ConsLoList<>(green,
          new ConsLoList<>(blue, new ConsLoList<>(yellow, new MtLoList<>()))));

  // examples of unfinished guesses
  UnfinishedGuess oneColorGuess =
      new UnfinishedGuess(new ConsLoList<>(blue, new MtLoList<>()), 4);
  UnfinishedGuess fullGuess = new UnfinishedGuess(
      new ConsLoList<>(green,
          new ConsLoList<>(red, new ConsLoList<>(blue, new MtLoList<>()))),
      3);

  // getting initial game setup for tests
  Random fixedRand = new Random(12345); // fixed seed for predictable results
  //  setting the secret code in the testGame
  MastermindGame testGame = new MastermindGame(4, 10, true, new Random()) {
    {
      // Set secret code manually for testing
      this.secretCode = exampleSecretCode;
    }
  };

  // Test to check if the guess is correct
  boolean testCorrectGuess(Tester t) {
    // get feeback
    Feedback feedback = testGame.evaluateGuess(correctGuess);

    // all positions correct matches
    boolean isCorrect = feedback.exactMatches == 4;

    // result of test
    return t.checkExpect(isCorrect, true);
  }

  // test getImage method returns the correct WorldImage
  boolean testGetImage(Tester t) {
    return t.checkExpect(red.getImage(),
               new CircleImage(20, OutlineMode.SOLID, Color.RED))
        && t.checkExpect(blue.getImage(),
            new CircleImage(20, OutlineMode.SOLID, Color.BLUE));
  }

  // test sameColor checks if colors are equal correctly
  boolean testSameColor(Tester t) {
    return t.checkExpect(red.sameColor(anotherRed), true)
        && t.checkExpect(red.sameColor(green), false);
  }

  boolean testGameStarts(Tester t) {
    new MastermindWorld();
    return true;
  }

  // test length returns # of elements
  boolean testLength(Tester t) {
    return t.checkExpect(emptyList.length(), 0)
        && t.checkExpect(oneElementList.length(), 1)
        && t.checkExpect(twoElementList.length(), 2);
  }

  //  getElementAt gets elements or throws exception
  boolean testGetElementAt(Tester t) {
    return t.checkExpect(oneElementList.getElementAt(0), red)
        && t.checkException(
            new IndexOutOfBoundsException("Index out of bounds"), emptyList,
            "getElementAt", 0);
  }

  // removeElementAt
  boolean testRemoveElementAt(Tester t) {
    IList<MyColor> emptyList = new MtLoList<>();
    IList<MyColor> result = emptyList.removeElementAt(0);
    return t.checkExpect(result, emptyList);
  }

  // addColor adds a color
  boolean testAddColor(Tester t) {
    return t.checkExpect(oneColorGuess.addColor(red).colors.length(), 2)
        && t.checkExpect(fullGuess.addColor(blue).colors.length(),
            3); // cant add more colors
  }

  // removeLastColor removes the last color
  boolean testRemoveLastColor(Tester t) {
    return t.checkExpect(fullGuess.removeLastColor().colors.length(), 2)
        && t.checkExpect(oneColorGuess.removeLastColor().colors.length(), 0);
  }

  // isComplete checks if the guess length matches the required length
  boolean testIsComplete(Tester t) {
    return t.checkExpect(fullGuess.isComplete(), true)
        && t.checkExpect(oneColorGuess.isComplete(), false);
  }

  // this tests the key number that was inputed
  boolean testNumberKeyInput(Tester t) {
    MastermindGame game = new MastermindGame(4, 10, true, new Random());
    game.onKeyEvent("1"); //  "1" corresponds to the first color list
    boolean test1 = t.checkExpect(game.currentGuess.colors.length(), 1);

    game.onKeyEvent("2"); // "2" corresponds to the second color
    boolean test2 = t.checkExpect(game.currentGuess.colors.length(), 2);

    game.onKeyEvent("9");
    boolean test3 = t.checkExpect(
        game.currentGuess.colors.length(), 2); // no change expected

    return test1 && test2 && test3;
  }

  boolean testEnterKey(Tester t) {
    MastermindGame game = new MastermindGame(4, 10, true, new Random());
    game.onKeyEvent("1");
    game.onKeyEvent("2");
    game.onKeyEvent("3");
    game.onKeyEvent("4");
    game.onKeyEvent("enter");
    return t.checkExpect(game.remainingGuesses, 9) && // One guess is made
        t.checkExpect(game.pastGuesses.length(), 1)
        && t.checkExpect(game.currentGuess.colors.length(),
            0); // Current guess should be reset
  }

  // backspace key testing
  boolean testBackspaceKey(Tester t) {
    MastermindGame game = new MastermindGame(4, 10, true, new Random());
    game.currentGuess =
        new UnfinishedGuess(new ConsLoList<>(this.blue,
                                new ConsLoList<>(this.green, new MtLoList<>())),
            4);

    game.onKeyEvent("backspace");
    boolean test1 = t.checkExpect(
        game.currentGuess.colors.length(), 1); // Should be 1 color now

    game.onKeyEvent("backspace");
    boolean test2 = t.checkExpect(
        game.currentGuess.colors.length(), 0); // Should be empty now

    game.onKeyEvent("backspace");
    boolean test3 = t.checkExpect(
        game.currentGuess.colors.length(), 0); // Still should be empty

    return test1 && test2 && test3;
  }

  boolean testInvalidKeys(Tester t) {
    MastermindGame game = new MastermindGame(4, 10, true, new Random());
    game.onKeyEvent("x"); // random key
    return t.checkExpect(game.currentGuess.colors.length(), 0);
  }

  // isNumberKey checks if the key is a valid number for color selection
  boolean testIsNumberKey(Tester t) {
    return t.checkExpect(testGame.isNumberKey("5"), true)
        && t.checkExpect(testGame.isNumberKey("x"), false);
  }

  // generateSecretCode creates a secret code of correct length
  boolean testGenerateSecretCode(Tester t) {
    IList<MyColor> code = testGame.generateSecretCode();
    return t.checkExpect(code.length(), 4);
  }

  // evaluateGuess provides correct feedback for a guess
  boolean testEvaluateGuess(Tester t) {
    IList<MyColor> guess = new ConsLoList<>(this.red,
        new ConsLoList<>(green,
            new ConsLoList<>(
                this.blue, new ConsLoList<>(this.red, new MtLoList<>()))));
    Feedback feedback = testGame.evaluateGuess(guess);
    return t.checkExpect(feedback.exactMatches >= 0, true)
        && t.checkExpect(feedback.inexactMatches >= 0, true);
  }
  
  // Test cases for evaluateGuess method
  boolean testExactMatches(Tester t) {
    testGame.secretCode = new ConsLoList<>(this.red,
        new ConsLoList<>(this.green,
            new ConsLoList<>(
                blue, new ConsLoList<>(yellow, new MtLoList<>()))));
    IList<MyColor> guess = new ConsLoList<>(this.red,
        new ConsLoList<>(this.green,
            new ConsLoList<>(
                blue, new ConsLoList<>(yellow, new MtLoList<>()))));
    Feedback feedback = testGame.evaluateGuess(guess);
    return t.checkExpect(feedback.exactMatches, 4)
        && t.checkExpect(feedback.inexactMatches, 0);
  }

  boolean testNoMatches(Tester t) {
    testGame.secretCode = new ConsLoList<>(red,
        new ConsLoList<>(red,
            new ConsLoList<>(red, new ConsLoList<>(red, new MtLoList<>()))));
    IList<MyColor> guess = new ConsLoList<>(blue,
        new ConsLoList<>(blue,
            new ConsLoList<>(blue, new ConsLoList<>(blue, new MtLoList<>()))));
    Feedback feedback = testGame.evaluateGuess(guess);
    return t.checkExpect(feedback.exactMatches, 0)
        && t.checkExpect(feedback.inexactMatches, 0);
  }

  // Test for evaluating guesses with mixed exact and inexact matches
  boolean testMixedMatches(Tester t) {
    testGame.secretCode = new ConsLoList<>(this.red,
        new ConsLoList<>(this.blue,
            new ConsLoList<>(
                this.green, new ConsLoList<>(this.yellow, new MtLoList<>()))));
    IList<MyColor> guess = new ConsLoList<>(this.red,
        new ConsLoList<>(this.green,
            new ConsLoList<>(
                this.blue, new ConsLoList<>(this.yellow, new MtLoList<>()))));
    Feedback feedback = testGame.evaluateGuess(guess);
    return t.checkExpect(feedback.exactMatches, 2)
        && t.checkExpect(feedback.inexactMatches, 2);
  }

  // Test for inexact matches where guessed colors are in the wrong positions
  boolean testInexactMatches(Tester t) {
    testGame.secretCode = new ConsLoList<>(this.red,
        new ConsLoList<>(this.green,
            new ConsLoList<>(
                this.blue, new ConsLoList<>(this.yellow, new MtLoList<>()))));
    IList<MyColor> guess = new ConsLoList<>(this.green,
        new ConsLoList<>(this.yellow,
            new ConsLoList<>(
                this.red, new ConsLoList<>(this.blue, new MtLoList<>()))));
    Feedback feedback = testGame.evaluateGuess(guess);
    return t.checkExpect(feedback.exactMatches, 0)
        && t.checkExpect(feedback.inexactMatches, 4);
  }

  boolean testDuplicateColorsInGuess(Tester t) {
    testGame.secretCode = new ConsLoList<>(red,
        new ConsLoList<>(red,
            new ConsLoList<>(
                blue, new ConsLoList<>(yellow, new MtLoList<>()))));
    IList<MyColor> guess = new ConsLoList<>(red,
        new ConsLoList<>(red,
            new ConsLoList<>(red, new ConsLoList<>(red, new MtLoList<>()))));
    Feedback feedback = testGame.evaluateGuess(guess);
    return t.checkExpect(feedback.exactMatches, 2)
        && t.checkExpect(feedback.inexactMatches, 0);
  }

  boolean testNoPossibleMatches(Tester t) {
    testGame.secretCode = new ConsLoList<>(red,
        new ConsLoList<>(red,
            new ConsLoList<>(
                green, new ConsLoList<>(green, new MtLoList<>()))));
    IList<MyColor> guess = new ConsLoList<>(blue,
        new ConsLoList<>(blue,
            new ConsLoList<>(
                yellow, new ConsLoList<>(yellow, new MtLoList<>()))));
    Feedback feedback = testGame.evaluateGuess(guess);
    return t.checkExpect(feedback.exactMatches, 0)
        && t.checkExpect(feedback.inexactMatches, 0);
  }

  boolean testGameOverLogic(Tester t) {
    Random rand = new Random(1);
    MastermindGame game = new MastermindGame(4, 2, false, rand);
    game.secretCode = new ConsLoList<>(new MyColor("Red", Color.RED),
        new ConsLoList<>(new MyColor("Green", Color.GREEN),
            new ConsLoList<>(new MyColor("Blue", Color.BLUE),
                new ConsLoList<>(
                    new MyColor("Yellow", Color.YELLOW), new MtLoList<>()))));

    // First incorrect guess (game continues)
    game.currentGuess = new UnfinishedGuess(
        new ConsLoList<>(new MyColor("Red", Color.RED),
            new ConsLoList<>(new MyColor("Green", Color.GREEN),
                new ConsLoList<>(new MyColor("Blue", Color.BLUE),
                    new ConsLoList<>(new MyColor("Purple", Color.MAGENTA),
                        new MtLoList<>())))),
        4);
    game.onKeyEvent("enter");
    t.checkExpect(game.gameOver, false);
    t.checkExpect(game.remainingGuesses, 1);

    // Second incorrect guess (game ends with loss)
    game.currentGuess = new UnfinishedGuess(
        new ConsLoList<>(new MyColor("Red", Color.RED),
            new ConsLoList<>(new MyColor("Blue", Color.BLUE),
                new ConsLoList<>(new MyColor("Green", Color.GREEN),
                    new ConsLoList<>(new MyColor("Purple", Color.MAGENTA),
                        new MtLoList<>())))),
        4);
    game.onKeyEvent("enter");
    t.checkExpect(game.gameOver, true);
    t.checkExpect(game.playerWon, false);

    return true;
  }
}
