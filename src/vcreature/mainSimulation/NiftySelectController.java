package vcreature.mainSimulation;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.*;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sean on 10/17/15.
 */
public class NiftySelectController implements ScreenController
{
  private final MainSim app;
  private ArrayList<Integer> thread_count_selections = new ArrayList<>(Arrays.asList(1, 2, 4, 8, 16, 32));

  private DropDown thread_count_box;
  private DropDown thread_view_box;

  private int viewing_creature = -1;

  public NiftySelectController(MainSim app) {
    this.app = app;
  }

  @Override
  public void bind(Nifty nifty, Screen screen)
  {
    thread_count_box = screen.findNiftyControl("threadCountSelectionBox", DropDown.class);
    for(int t : thread_count_selections) {
      thread_count_box.addItem(t);
    }
    thread_view_box = screen.findNiftyControl("threadViewSelectionBox", DropDown.class);
    for(int i = 1; i <= app.thread_count; i++) {
      thread_view_box.addItem(i);
    }
    Slider speed_slider = screen.findNiftyControl("speedSlider", Slider.class);
    speed_slider.setMin(1);
    speed_slider.setMax(50);
    speed_slider.setButtonStepSize(5);

    TextField max_num_blocks_field = screen.findNiftyControl("maxNumBlocksField", TextField.class);
    max_num_blocks_field.setText(app.max_num_blocks + "");

    TextField max_population_field = screen.findNiftyControl("maxPopulationField", TextField.class);
    max_population_field.setText(app.starting_population_count + "");

    TextField creature_view_field = screen.findNiftyControl("creatureViewField", TextField.class);
    creature_view_field.setText("");
  }

  @NiftyEventSubscriber(id = "threadCountSelectionBox")
  public void onThreadCountSelectionBoxChanged(final String id, final DropDownSelectionChangedEvent<Integer> event) {
    int selection = event.getSelection();
    if (app.debug) {
      System.out.println("Thread Count Selection: " + selection);
    }
    app.setThreadCount(selection);

    int thread_view_selection = thread_view_box.getSelectedIndex();
    thread_view_box.clear();
    for(int i = 1; i <= app.thread_count; i++) {
      thread_view_box.addItem(i);
    }
    if (thread_view_selection < app.thread_count) {
      thread_view_box.selectItemByIndex(thread_view_selection);
    }
  }

  @NiftyEventSubscriber(id = "threadViewSelectionBox")
  public void onThreadViewSelectionBoxChanged(final String id, final DropDownSelectionChangedEvent<Integer> event) {
    int selection = event.getSelection();
    if (app.debug) {
      System.out.println("Thread View Selection: " + selection);
    }
    app.setViewingThread(selection);
  }

  @NiftyEventSubscriber(id = "speedSlider")
  public void onSpeedSliderChanged(final String id, final SliderChangedEvent event) {
    if (app.debug) {
      System.out.println("speed selection: " + event.getValue());
    }
    app.setSpeed((int) event.getValue());
  }

  @NiftyEventSubscriber(id = "maxNumBlocksField")
  public void onMaxNumBlocksFieldChanged(final String id, final TextFieldChangedEvent event) {
    int selection = Integer.parseInt(event.getText());
    if (app.debug) {
      System.out.println("max number of blocks: " + selection);
    }
    app.setMaxNumBlocks(selection);
  }

  @NiftyEventSubscriber(id = "maxPopulationField")
  public void onMaxPopulationFieldChanged(final String id, final TextFieldChangedEvent event) {
    int selection = Integer.parseInt(event.getText());
    if (app.debug) {
      System.out.println("max population: " + selection);
    }
    app.setMaxPopulation(selection);
  }

  @NiftyEventSubscriber(id = "creatureViewField")
  public void onCreatureViewFieldChanged(final String id, final TextFieldChangedEvent event) {
    int selection = Integer.parseInt(event.getText());
    if (app.debug) {
      System.out.println("creature view: " + selection);
    }
    this.viewing_creature = selection;
  }

  @NiftyEventSubscriber(id = "submitButton")
  public void onDoneButtonClicked(final String id, final ButtonClickedEvent event) {
    app.setViewingCreature(viewing_creature);
  }

  @Override
  public void onStartScreen()
  {

  }

  @Override
  public void onEndScreen()
  {

  }
}
