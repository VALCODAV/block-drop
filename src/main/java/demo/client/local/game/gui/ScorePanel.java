package demo.client.local.game.gui;

import javax.inject.Inject;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

import demo.client.local.game.tools.Style;
import demo.client.shared.ScoreTracker;

/**
 * A display for a single player's name and score.
 * 
 * @author mbarkley <mbarkley@redhat.com>
 * 
 */
@Templated
public class ScorePanel extends Composite implements HasModel<ScoreTracker> {

  @AutoBound
  @Inject
  private DataBinder<ScoreTracker> scoreBinder;

  @DataField
  @Bound(property = "player.name")
  @Inject
  private Label name;

  @DataField
  @Bound
  @Inject
  private Label score;

  /**
   * Select this panel as the current target.
   * 
   * @param event
   *          The click event that triggered this call.
   */
  @EventHandler
  public void onClick(ClickEvent event) {
    @SuppressWarnings("unchecked")
    ListWidget<ScoreTracker, ScorePanel> scoreList = (ListWidget<ScoreTracker, ScorePanel>) getParent().getParent();
    ScoreTracker model = getModel();
    int index = scoreList.getValue().indexOf(model);

    if (index != -1)
      BoardPage.getInstance().getController().getSecondaryController().selectPlayerByIndex(index);
  }

  /**
   * Display this panel with the {@link Style#SELECTED SELECTED} style.
   * 
   * @param value
   *          True if this element should be displayed with the {@link Style#SELECTED SELECTED}
   *          style. False if not.
   */
  public void setSelected(boolean value) {
    setStyleName(Style.SELECTED, value);
  }

  /**
   * Set the displayed score of this panel.
   * 
   * @param score
   *          The new score to be displayed.
   */
  public void setScore(long score) {
    this.score.setText((new Long(score)).toString());
  }

  /**
   * Get the displayed score of this panel.
   * 
   * @return The displayed score.
   */
  public long getScore() {
    return new Long(score.getText());
  }

  /**
   * Set the name displayed on this panel.
   * 
   * @param string
   *          The name to be displayed.
   */
  public void setName(String string) {
    name.setText(string);
  }

  /**
   * Get the name displayed on this panel.
   * 
   * @return The name displayed.
   */
  public String getName() {
    return name.getText();
  }

  @Override
  public ScoreTracker getModel() {
    return scoreBinder.getModel();
  }

  @Override
  public void setModel(ScoreTracker model) {
    scoreBinder.setModel(model);
  }
}
