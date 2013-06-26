package demo.client.local.game;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageHidden;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.DomEvent.Type;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;

import demo.client.local.lobby.Client;
import demo.client.local.lobby.Lobby;
import demo.client.shared.Command;
import demo.client.shared.ExitMessage;
import demo.client.shared.ScoreTracker;

/*
 * An Errai Navigation Page providing the UI for a Block Drop game.
 */
@Page
@Templated("Board.html")
public class BoardPage extends Composite {

  public static final String CANVAS_WRAPPER_ID = "mainCanvas-wrapper";

  /* The background colour of the Block Drop board. */
  public static final String BOARD_COLOUR = "rgb(255,255,255)";

  /* A mainCanvas for drawing a Block Drop game. */
  @DataField("canvas")
  private Canvas mainCanvas = Canvas.createIfSupported();
  /* A mainCanvas for drawing the next piece in the Block Drop game. */
  @DataField("next-piece")
  private Canvas nextPieceCanvas = Canvas.createIfSupported();
  @Inject
  @DataField("score-list")
  private ListWidget<ScoreTracker, ScorePanel> scoreDisplay;
  private ScorePanel panel;

  @Inject
  private TransitionTo<Lobby> lobbyTransition;

  ScoreTracker getScoreModel() {
    return panel.getModel();
  }

  /* A controller for this view. */
  private BoardController controller;

  @Inject
  private RequestDispatcher dispatcher;

  /*
   * Create a BoardPage for displaying a Block Drop game.
   */
  public BoardPage() {
    System.out.println("Initiating BoardModel");

    // Initialize canvases.
    mainCanvas.setCoordinateSpaceHeight(Size.MAIN_COORD_HEIGHT);
    mainCanvas.setCoordinateSpaceWidth(Size.MAIN_COORD_WIDTH);
    nextPieceCanvas.setCoordinateSpaceHeight(Size.NEXT_COORD_HEIGHT);
    nextPieceCanvas.setCoordinateSpaceWidth(Size.NEXT_COORD_WIDTH);

    // Initialize controller.
    controller = new BoardController();
  }

  /*
   * Perform additional setup for the Board UI after this object has been constructed.
   */
  @PostConstruct
  private void constructUI() {
    // Check that mainCanvas was supported.
    if (mainCanvas != null) {
      System.out.println("Canvas successfully created.");
      controller.setPage(this);

      try {
        controller.startGame();
      } catch (NullPointerException e) {
        // Null pointer likely means the user needs to register a Player object in the lobby.
        lobbyTransition.go();
      }
    }
    else {
      // TODO: Display message to user that HTML5 Canvas is required.
    }
  }

  @PageHidden
  private void leaveGame() {
    ExitMessage exitMessage = new ExitMessage();
    exitMessage.setPlayer(Client.getInstance().getPlayer());
    exitMessage.setGame(Client.getInstance().getGameRoom());
    Client.getInstance().setGameRoom(null);
    MessageBuilder.createMessage("Relay").command(Command.LEAVE_GAME).withValue(exitMessage).noErrorHandling()
            .sendNowWith(dispatcher);
  }

  void initScoreList(List<ScoreTracker> scoreList) {
    scoreDisplay.setItems(scoreList);
  }

  /*
   * Fill the current path on this page's mainCanvas with the board background colour.
   */
  private void drawBackground() {
    mainCanvas.getContext2d().setFillStyle(BOARD_COLOUR);
    mainCanvas.getContext2d().fill();
  }

  /*
   * Undraw the given block from this page's mainCanvas. Note: Any path on the mainCanvas will be
   * lost after invoking this method.
   * 
   * @param x The x coordinate of the position of the block.
   * 
   * @param y The y coordinate of the position of the block.
   * 
   * @param activeBlock The block to undraw.
   */
  void undrawBlock(int x, int y, Block activeBlock) {
    mainCanvas.getContext2d().beginPath();
    activeBlock.getPath(x, y, mainCanvas.getContext2d());
    mainCanvas.getContext2d().closePath();
    drawBackground();
  }

  /*
   * Draw a block on this page's mainCanvas.
   * 
   * @param x The x coordinate of the position of the block.
   * 
   * @param y The y coordinate of the position of the block.
   * 
   * @param activeBlock The block to draw.
   */
  void drawBlock(int x, int y, Block activeBlock) {
    activeBlock.draw(x, y, mainCanvas.getContext2d());
  }

  /*
   * Add a key press handler to this page's mainCanvas.
   * 
   * @param handler A key press handler for the mainCanvas.
   */
  <H extends EventHandler> void addHandlerToMainCanvas(H handler, Type<H> type) {
    Assert.notNull("Could not get game-wrapper root panel.", RootPanel.get()).addDomHandler(handler,
            type);
  }

  void drawBlockToNextCanvas(Block nextBlock) {
    // Clear everything.
    nextPieceCanvas.getContext2d().setFillStyle("lightgrey");
    nextPieceCanvas.getContext2d().fillRect(0, 0, Size.NEXT_COORD_WIDTH, Size.MAIN_COORD_HEIGHT);

    // Draw title.
    nextPieceCanvas.getContext2d().setFillStyle("black");
    nextPieceCanvas.getContext2d().setFont("bold 20px sans-serif");
    nextPieceCanvas.getContext2d().fillText("Next Block", 10, 20);

    nextBlock.draw(2 * Size.BLOCK_SIZE + nextBlock.getCentreColDiff() * Size.BLOCK_SIZE, 2 * Size.BLOCK_SIZE
            + nextBlock.getCentreRowDiff() * Size.BLOCK_SIZE, nextPieceCanvas.getContext2d());
  }

  List<ScoreTracker> getScoreList() {
    return scoreDisplay.getValue();
  }

  static void pause() {
    DivElement element = ((DivElement) Document.get().getElementById("pause-overlay"));
    element.setAttribute("style", "visibility: visible");
  }

  static void unpause() {
    DivElement element = ((DivElement) Document.get().getElementById("pause-overlay"));
    element.removeAttribute("style");
  }
}
