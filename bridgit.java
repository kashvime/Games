import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

class Cell {
  Color color;
  Cell top, bottom, left, right; // Neighbors
  boolean isEdge; // True if the cell is on the edge of the board

  // Constructor
  Cell(Color color, boolean isEdge) {
      this.color = color;
      this.isEdge = isEdge;
      this.top = null;
      this.bottom = null;
      this.left = null;
      this.right = null;
  }

  // Draw the cell
  WorldImage drawCell(int cellSize) {
      return new RectangleImage(cellSize, cellSize, OutlineMode.SOLID, this.color);
  }
}


class BridgItGame extends World {
  ArrayList<ArrayList<Cell>> board; // 2D grid of cells
  int n; // Size of the grid
  int cellSize; // Size of each cell in pixels
  boolean isPlayerOneTurn; // true if Player 1 (Pink) is playing, false if Player 2 (Magenta)
  boolean gameOver; // true if the game is over

  // Modify the constructor
  BridgItGame(int n) {
      if (n < 3 || n % 2 == 0) {
          throw new IllegalArgumentException("Board size must be an odd number >= 3.");
      }

      this.n = n;
      this.cellSize = 40;
      this.board = new ArrayList<>();
      this.isPlayerOneTurn = true; // Player 1 starts
      this.gameOver = false; // Game starts as not over

      this.initializeBoard();
      this.linkCells();
  }



    void initializeBoard() {
      for (int row = 0; row < n; row++) {
          ArrayList<Cell> rowList = new ArrayList<>();

          for (int col = 0; col < n; col++) {
              boolean isEdge = (row == 0 || row == n - 1 || col == 0 || col == n - 1);
              Color cellColor;

              // Pattern logic
              if (row % 2 == 0) { // Even rows (White, Purple)
                  if (col % 2 == 0) {
                      cellColor = Color.WHITE;
                  } else {
                      cellColor = Color.MAGENTA; // Purple
                  }
              } else { // Odd rows (Pink, White)
                  if (col % 2 == 0) {
                      cellColor = Color.PINK;
                  } else {
                      cellColor = Color.WHITE;
                  }
              }

              rowList.add(new Cell(cellColor, isEdge));
          }
          this.board.add(rowList);
      }
  }
    
    @Override
    public void onMouseClicked(Posn pos) {
        if (gameOver) {
            return; // Ignore clicks if the game is over
        }

        // Calculate the row and column of the clicked cell
        int row = pos.y / cellSize;
        int col = pos.x / cellSize;

        // Ensure the click is within bounds
        if (row >= 0 && row < n && col >= 0 && col < n) {
            Cell clickedCell = board.get(row).get(col);

            // Only allow changes to white cells that are not on the edges
            if (!clickedCell.isEdge && clickedCell.color.equals(Color.WHITE)) {
                // Update the cell's color based on the current player
                if (isPlayerOneTurn) {
                    clickedCell.color = Color.PINK;
                } else {
                    clickedCell.color = Color.MAGENTA;
                }

                // Alternate the turn
                isPlayerOneTurn = !isPlayerOneTurn;

                // Check if the game is over
                if (checkWin()) {
                    gameOver = true; // Stop the game
                }
            }
        }
    }


    boolean hasPath(Cell cell, Color color, ArrayList<Cell> visited) {
      // Base case: If the cell is null or already visited, return false
      if (cell == null || visited.contains(cell)) {
          return false;
      }

      // Add the current cell to the visited list
      visited.add(cell);

      // Check if the path is complete:
      // - For Player 1 (Pink): Path reaches the right edge
      if (color.equals(Color.PINK) && cell.right == null) {
          return true; // Reached the right edge
      }

      // - For Player 2 (Magenta): Path reaches the bottom edge
      if (color.equals(Color.MAGENTA) && cell.bottom == null) {
          return true; // Reached the bottom edge
      }

      // Recursively check all neighbors of the same color
      return (cell.top != null && cell.top.color.equals(color) && hasPath(cell.top, color, visited)) ||
             (cell.bottom != null && cell.bottom.color.equals(color) && hasPath(cell.bottom, color, visited)) ||
             (cell.left != null && cell.left.color.equals(color) && hasPath(cell.left, color, visited)) ||
             (cell.right != null && cell.right.color.equals(color) && hasPath(cell.right, color, visited));
  }



    boolean checkWin() {
      // Check for Player 1 (Pink): Left to Right
      for (int row = 0; row < n; row++) {
          if (board.get(row).get(0).color.equals(Color.PINK)) { // Start on the left edge
              if (hasPath(board.get(row).get(0), Color.PINK, new ArrayList<>())) {
                  System.out.println("Player 1 (Pink) wins!");
                  return true;
              }
          }
      }

      // Check for Player 2 (Magenta): Top to Bottom
      for (int col = 0; col < n; col++) {
          if (board.get(0).get(col).color.equals(Color.MAGENTA)) { // Start on the top edge
              if (hasPath(board.get(0).get(col), Color.MAGENTA, new ArrayList<>())) {
                  System.out.println("Player 2 (Magenta) wins!");
                  return true;
              }
          }
      }

      return false; // No winner yet
  }


    void linkCells() {
      for (int row = 0; row < n; row++) {
          for (int col = 0; col < n; col++) {
              Cell current = board.get(row).get(col);

              // Assign neighbors using if statements
              if (row > 0) {
                  current.top = board.get(row - 1).get(col);
              } else {
                  current.top = null;
              }

              if (row < n - 1) {
                  current.bottom = board.get(row + 1).get(col);
              } else {
                  current.bottom = null;
              }

              if (col > 0) {
                  current.left = board.get(row).get(col - 1);
              } else {
                  current.left = null;
              }

              if (col < n - 1) {
                  current.right = board.get(row).get(col + 1);
              } else {
                  current.right = null;
              }
          }
      }
  }

    // Render the game board
    public WorldScene makeScene() {
        WorldScene scene = new WorldScene(n * cellSize, n * cellSize);

        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                Cell cell = board.get(row).get(col);
                WorldImage cellImage = cell.drawCell(cellSize);

                // Place the cell at the correct position
                scene.placeImageXY(cellImage, col * cellSize + cellSize / 2, row * cellSize + cellSize / 2);
            }
        }

        return scene;
    }
}

// Example Tester
class ExamplesBridgIt {
    void testGame(Tester t) {
        BridgItGame game = new BridgItGame(7); // Create a 7x7 board
        game.bigBang(280, 280, 0.5); // Start the game with a 40-pixel cell size
    }
    void testCellConnections(Tester t) {
      BridgItGame game = new BridgItGame(11); // Create an 11x11 board

      // Loop through all cells to check their neighbors
      for (int row = 0; row < game.n; row++) {
          for (int col = 0; col < game.n; col++) {
              Cell cell = game.board.get(row).get(col);

              // Check the top neighbor
              if (row > 0) {
                  t.checkExpect(cell.top, game.board.get(row - 1).get(col));
              } else {
                  t.checkExpect(cell.top, null); // Top row should have no top neighbor
              }

              // Check the bottom neighbor
              if (row < game.n - 1) {
                  t.checkExpect(cell.bottom, game.board.get(row + 1).get(col));
              } else {
                  t.checkExpect(cell.bottom, null); // Bottom row should have no bottom neighbor
              }

              // Check the left neighbor
              if (col > 0) {
                  t.checkExpect(cell.left, game.board.get(row).get(col - 1));
              } else {
                  t.checkExpect(cell.left, null); // Leftmost column should have no left neighbor
              }

              // Check the right neighbor
              if (col < game.n - 1) {
                  t.checkExpect(cell.right, game.board.get(row).get(col + 1));
              } else {
                  t.checkExpect(cell.right, null); // Rightmost column should have no right neighbor
              }
          }
      }
  }
    void testMakeScene(Tester t) {
      // Create a 3x3 board for simplicity
      BridgItGame game = new BridgItGame(3);

      // Create the expected scene
      WorldScene expectedScene = new WorldScene(game.n * game.cellSize, game.n * game.cellSize);

      // Add expected images to the scene
      for (int row = 0; row < game.n; row++) {
          for (int col = 0; col < game.n; col++) {
              Cell cell = game.board.get(row).get(col);
              WorldImage cellImage = cell.drawCell(game.cellSize);

              // Place the cell in the expected position
              expectedScene.placeImageXY(cellImage, col * game.cellSize + game.cellSize / 2, row * game.cellSize + game.cellSize / 2);
          }
      }

      // Check if the rendered scene matches the expected scene
      t.checkExpect(game.makeScene(), expectedScene);
  }
    void testInitializeBoard(Tester t) {
      BridgItGame game = new BridgItGame(5); // Create a 5x5 board for testing

      // Check the general properties of the board
      t.checkExpect(game.board.size(), 5); // The board should have 5 rows
      for (ArrayList<Cell> row : game.board) {
          t.checkExpect(row.size(), 5); // Each row should have 5 cells
      }

      // Verify colors and edge statuses
      for (int row = 0; row < 5; row++) {
          for (int col = 0; col < 5; col++) {
              Cell cell = game.board.get(row).get(col);
              boolean isEdge = (row == 0 || row == 4 || col == 0 || col == 4);

              // Check the edge status
              t.checkExpect(cell.isEdge, isEdge);

              // Check the color logic
              if (row % 2 == 0) { // Even rows (White, Purple)
                  if (col % 2 == 0) {
                      t.checkExpect(cell.color, Color.WHITE); // White cell
                  } else {
                      t.checkExpect(cell.color, Color.MAGENTA); // Purple cell
                  }
              } else { // Odd rows (Pink, White)
                  if (col % 2 == 0) {
                      t.checkExpect(cell.color, Color.PINK); // Pink cell
                  } else {
                      t.checkExpect(cell.color, Color.WHITE); // White cell
                  }
              }
          }
      }
  }
    void testCornerCells(Tester t) {
      BridgItGame game = new BridgItGame(5); // Create a 5x5 board

      // Top-left corner
      Cell topLeft = game.board.get(0).get(0);
      t.checkExpect(topLeft.isEdge, true);
      t.checkExpect(topLeft.color, Color.WHITE);

      // Top-right corner
      Cell topRight = game.board.get(0).get(4);
      t.checkExpect(topRight.isEdge, true);
      t.checkExpect(topRight.color, Color.WHITE);

      // Bottom-left corner
      Cell bottomLeft = game.board.get(4).get(0);
      t.checkExpect(bottomLeft.isEdge, true);
      t.checkExpect(bottomLeft.color, Color.WHITE);

      // Bottom-right corner
      Cell bottomRight = game.board.get(4).get(4);
      t.checkExpect(bottomRight.isEdge, true);
      t.checkExpect(bottomRight.color, Color.WHITE);
  }
    void testSpecificRows(Tester t) {
      BridgItGame game = new BridgItGame(5); // Create a 5x5 board

      // Test an even row (Row 0)
      ArrayList<Cell> row0 = game.board.get(0);
      t.checkExpect(row0.get(0).color, Color.WHITE);
      t.checkExpect(row0.get(1).color, Color.MAGENTA);
      t.checkExpect(row0.get(2).color, Color.WHITE);
      t.checkExpect(row0.get(3).color, Color.MAGENTA);
      t.checkExpect(row0.get(4).color, Color.WHITE);

      // Test an odd row (Row 1)
      ArrayList<Cell> row1 = game.board.get(1);
      t.checkExpect(row1.get(0).color, Color.PINK);
      t.checkExpect(row1.get(1).color, Color.WHITE);
      t.checkExpect(row1.get(2).color, Color.PINK);
      t.checkExpect(row1.get(3).color, Color.WHITE);
      t.checkExpect(row1.get(4).color, Color.PINK);
  }
    void testEdgeCells(Tester t) {
      BridgItGame game = new BridgItGame(5); // Create a 5x5 board

      // Check the top and bottom edges
      for (int col = 0; col < 5; col++) {
          t.checkExpect(game.board.get(0).get(col).isEdge, true); // Top edge
          t.checkExpect(game.board.get(4).get(col).isEdge, true); // Bottom edge
      }

      // Check the left and right edges
      for (int row = 0; row < 5; row++) {
          t.checkExpect(game.board.get(row).get(0).isEdge, true); // Left edge
          t.checkExpect(game.board.get(row).get(4).isEdge, true); // Right edge
      }

      // Check non-edge cells
      for (int row = 1; row < 4; row++) {
          for (int col = 1; col < 4; col++) {
              t.checkExpect(game.board.get(row).get(col).isEdge, false); // Non-edge cells
          }
      }
  }
    void testSmallBoard(Tester t) {
      BridgItGame game = new BridgItGame(3);

      // Check rows and edge logic as before
      t.checkExpect(game.board.size(), 3);
      for (ArrayList<Cell> row : game.board) {
          t.checkExpect(row.size(), 3);
      }
  }
    void testHasPath(Tester t) {
      BridgItGame game = new BridgItGame(5);

      // Manually create a winning path for Player 1 (Pink)
      game.board.get(0).get(0).color = Color.PINK;
      game.board.get(0).get(1).color = Color.PINK;
      game.board.get(0).get(2).color = Color.PINK;
      game.board.get(0).get(3).color = Color.PINK;
      game.board.get(0).get(4).color = Color.PINK;

      t.checkExpect(game.checkWin(), true); // Player 1 wins

      // Reset the board and create a winning path for Player 2 (Magenta)
      game = new BridgItGame(5);
      game.board.get(0).get(0).color = Color.MAGENTA;
      game.board.get(1).get(0).color = Color.MAGENTA;
      game.board.get(2).get(0).color = Color.MAGENTA;
      game.board.get(3).get(0).color = Color.MAGENTA;
      game.board.get(4).get(0).color = Color.MAGENTA;

      t.checkExpect(game.checkWin(), true); // Player 2 wins
  }


}
