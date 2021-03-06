package demo.client.local.lobby;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageHidden;
import org.jboss.errai.ui.nav.client.local.PageShowing;
import org.jboss.errai.ui.nav.client.local.TransitionTo;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;

import demo.client.local.Client;
import demo.client.local.game.gui.BoardPage;
import demo.client.local.game.tools.Style;
import demo.client.shared.lobby.Invitation;
import demo.client.shared.lobby.LobbyUpdate;
import demo.client.shared.lobby.LobbyUpdateRequest;
import demo.client.shared.lobby.RegisterRequest;
import demo.client.shared.message.Command;
import demo.client.shared.meta.GameRoom;
import demo.client.shared.meta.Player;

/**
 * A class displaying a page for a game lobby.
 */
@Page(role = DefaultPage.class)
@Templated
@ApplicationScoped
public class Lobby extends Composite {

  /* For the Errai NavigationUI. */
  @Inject
  private TransitionTo<BoardPage> boardTransition;
  /* For requesting lobby updates from the server. */
  @Inject
  private Event<LobbyUpdateRequest> lobbyUpdateRequest;
  /* For registering this client with the server. */
  @Inject
  private Event<RegisterRequest> registerRequest;
  /* For inviting another user to play a game. */
  @Inject
  private Event<Invitation> gameInvitation;
  /* For receiving messages from the server. */
  private MessageBus messageBus = ErraiBus.get();

  @Inject
  @DataField("player-button-panel")
  private HorizontalPanel playerButtonPanel;
  @Inject
  @DataField("player-list")
  private ListWidget<Player, PlayerPanel> playerList;
  @Inject
  @DataField("game-list")
  private ListWidget<GameRoom, GamePanel> gameList;
  
  @Inject
  private Client client;

  private Set<Player> selectedPlayers = new HashSet<Player>();
  private GameRoom selectedGame = null;

  private LobbyHeartBeat heartBeat;

  /**
   * Construct the UI elements for the lobby.
   */
  @PostConstruct
  private void postConstruct() {
    Button newGameButton = new Button("New Game");
    newGameButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        // This player should always join a new game that he or she starts.
        Invitation invite = new Invitation();
        invite.setHost(client.getPlayer());
        invite.setGuests(selectedPlayers);
        gameInvitation.fire(invite);
      }
    });

    playerButtonPanel.add(newGameButton);
  }

  private void resetHeartBeat() {
    if (heartBeat != null)
      heartBeat.cancel();

    heartBeat = new LobbyHeartBeat(client);
    heartBeat.scheduleRepeating(5000);
  }

  @PageHidden
  private void leaveLobby() {
    heartBeat.cancel();
  }

  /**
   * Request an update of the current clients in the lobby from the server.
   */
  public void requestLobbyUpdate() {
    lobbyUpdateRequest.fire(new LobbyUpdateRequest());
  }
  
  /**
   * Join a game if one has been selected by the user.
   */
  public void joinSelectedGame() {
    if (selectedGame != null && gameList.getValue().contains(selectedGame)) {
      Invitation invite = new Invitation();
      // The target and gameId are the only information that the server will need.
      invite.setGameId(selectedGame.getId());
      invite.setTarget(client.getPlayer());
      MessageBuilder.createMessage("Relay").command(Command.JOIN_GAME).withValue(invite).noErrorHandling()
              .sendNowWith(messageBus);
    }
  }

  /**
   * Update the lobby list model and display with newest lobby update from the server.
   */
  public void updateLobby(@Observes LobbyUpdate update) {
    List<Player> players = update.getPlayers();
    players.remove(client.getPlayer());
    playerList.setItems(players);
    gameList.setItems(update.getGames());
    
    if (players.isEmpty()) {
      Document.get().getElementById("empty-player-list").getStyle().clearDisplay();
    }
    else {
      Document.get().getElementById("empty-player-list").getStyle().setDisplay(Display.NONE);
    }
    
    if (update.getGames().isEmpty()) {
      Document.get().getElementById("empty-game-list").getStyle().clearDisplay();
    }
    else {
      Document.get().getElementById("empty-game-list").getStyle().setDisplay(Display.NONE);
    }
  }

  /**
   * Register this user with the lobby.
   */
  @PageShowing
  public void joinLobby() {
    // If the user is joining for the first time, make a new player object.
    Player player = client.getPlayer() != null ? client.getPlayer() : new Player(client.getNickname());
    RegisterRequest request = new RegisterRequest(player);
    registerRequest.fire(request);
  }

  /**
   * Accept a player object from the server as the canonical representation of this user.
   */
  public void loadPlayer(@Observes Player player) {

    // If this user has not yet been registered, subscribe to server relay
    if (!client.hasRegisteredPlayer()) {

      messageBus.subscribe("Client" + player.getId(), new LobbyMessageCallback(client, this));
    }

    client.setPlayer(player);

    // Reset or start LobbyHeartBeat
    resetHeartBeat();

    requestLobbyUpdate();
  }

  /**
   * Toggle whether the given player is selected in the list of available players.
   * 
   * @param model
   *          The player to be toggled.
   */
  void togglePlayerSelection(Player model) {
    if (selectedPlayers.contains(model)) {
      selectedPlayers.remove(model);
      playerList.getWidget(model).removeStyleName(Style.SELECTED);
    }
    else {
      selectedPlayers.add(model);
      playerList.getWidget(model).addStyleName(Style.SELECTED);
    }
  }

  /**
   * Transition to the {@link BoardPage board page}.
   */
  void goToBoard() {
    boardTransition.go();
  }

  /**
   * Toggle whether the given GameRoom is selected in the list of available games. Any previously
   * selected games will be unselected.
   * 
   * @param model
   *          The GameRoom to be toggled.
   */
  void toggleGameSelection(GameRoom model) {
    if (model.equals(selectedGame)) {
      gameList.getWidget(model).removeStyleName(Style.SELECTED);
      selectedGame = null;
    }
    else {
      if (selectedGame != null)
        gameList.getWidget(selectedGame).removeStyleName(Style.SELECTED);
      gameList.getWidget(model).addStyleName(Style.SELECTED);
      selectedGame = model;
    }
  }
}
