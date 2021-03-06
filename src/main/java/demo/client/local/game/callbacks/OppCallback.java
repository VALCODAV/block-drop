package demo.client.local.game.callbacks;

import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;

import demo.client.local.Client;
import demo.client.local.game.controllers.OppController;
import demo.client.local.game.gui.ControllableBoardDisplay;
import demo.client.shared.message.Command;
import demo.client.shared.message.MoveEvent;
import demo.client.shared.message.ScoreEvent;
import demo.client.shared.meta.GameRoom;
import demo.client.shared.meta.Player;

/**
 * A MessageCallback implementation for receiving state updates of opponents in Block Drop.
 * 
 * @author mbarkley <mbarkley@redhat.com>
 * 
 */
public class OppCallback implements MessageCallback {

  private Map<Player, OppController> oppControllers;
  private ControllableBoardDisplay boardDisplay;
  private Client client;
  public OppCallback(ControllableBoardDisplay boardDisplay, Client client) {
    this.client = client;
    this.oppControllers = new HashMap<Player, OppController>();
    this.boardDisplay = boardDisplay;

    GameRoom game = client.getGameRoom();
    for (Player player : game.getPlayers().values()) {
      this.oppControllers.put(player, new OppController(boardDisplay, client));
    }
    this.oppControllers.get(client.getPlayer()).setActive(true);
    this.oppControllers.get(client.getPlayer()).startGame();
  }

  @Override
  public void callback(Message message) {
    Command command = Command.valueOf(message.getCommandType());
    switch (command) {

    case MOVE_UPDATE:
      MoveEvent moveEvent = message.getValue(MoveEvent.class);
      OppController movedController = oppControllers.get(moveEvent.getPlayer());
      movedController.setPaused(false);
      movedController.addState(moveEvent.getState());
      break;

    case UPDATE_SCORE:
      ScoreEvent scoreEvent = message.getValue(ScoreEvent.class);
      if (!oppControllers.containsKey(scoreEvent.getScoreTracker().getPlayer())) {
        oppControllers.put(scoreEvent.getScoreTracker().getPlayer(), new OppController(boardDisplay, client));
      }
      break;

    case GAME_KEEP_ALIVE:
      Player pausePlayer = message.getValue(Player.class);
      OppController pausedController = oppControllers.get(pausePlayer);
      pausedController.setPaused(true);
      break;

    case SWITCH_OPPONENT:
      Player player = message.getValue(Player.class);
      for (OppController controller : oppControllers.values()) {
        if (controller.isActive()) {
          controller.setActive(false);
        }
      }
      oppControllers.get(player).setActive(true);
      oppControllers.get(player).startGame();
      break;

    default:
      break;
    }
  }

  public void destroy() {
    for (OppController controller : oppControllers.values()) {
      controller.stop();
    }
  }

}
