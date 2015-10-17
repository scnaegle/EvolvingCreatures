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

  @Override
  public void onStartScreen()
  {

  }

  @Override
  public void onEndScreen()
  {

  }
}
