package vcreature.mainSimulation;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;

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

  public NiftySelectController(MainSim app) {
    this.app = app;
  }

  @Override
  public void bind(Nifty nifty, Screen screen)
  {
    DropDown thread_count_box = screen.findNiftyControl("threadCountSelectionBox", DropDown.class);
    for(int t : thread_count_selections) {
      thread_count_box.addItem(t);
    }
    DropDown thread_view_box = screen.findNiftyControl("threadViewSelectionBox", DropDown.class);
    for(int i = 1; i <= app.thread_count; i++) {
      thread_view_box.addItem(i);
    }
  }

  @NiftyEventSubscriber(id = "threadCountSelectionBox")
  public void onThreadCountSelectionBoxChanged(final String id, final ListBoxSelectionChangedEvent<Integer> event) {
    List<Integer> selection = event.getSelection();
    if (app.debug) {
      System.out.println("Thread Count Selection: " + selection.get(0));
      System.out.println("Thread Count Selection class: " + selection.get(0).getClass());
    }
    app.setThreadCount(selection.get(0));
  }

  @NiftyEventSubscriber(id = "threadViewSelectionBox")
  public void onThreadViewSelectionBoxChanged(final String id, final ListBoxSelectionChangedEvent<Integer> event) {
    List<Integer> selection = event.getSelection();
    if (app.debug) {
      System.out.println("Thread View Selection: " + selection.get(0));
      System.out.println("Thread Count Selection class: " + selection.get(0).getClass());
    }
    app.setViewingThread(selection.get(0));
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
