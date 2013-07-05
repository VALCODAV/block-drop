package demo.client.local.game;

import javax.inject.Inject;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.client.widget.HasModel;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

import demo.client.local.Style;
import demo.client.shared.ScoreTracker;

@Templated
public class ScorePanel extends Composite implements HasModel<ScoreTracker> {

  @AutoBound
  @Inject
  private DataBinder<ScoreTracker> scoreBinder;

  @DataField
  @Bound(property="player.name")
  @Inject
  private Label name;

  @DataField
  @Bound
  @Inject
  private Label score;
  
  public void setSelected(boolean value) {
    setStyleName(Style.SELECTED, value);
  }

  public void setScore(long score) {
    this.score.setText((new Long(score)).toString());
  }

  public long getScore() {
    return new Long(score.getText());
  }

  public void setName(String string) {
    name.setText(string);
  }

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
