package com.jotto.unitime;

/**
 * Created by johanrovala on 18/06/15.
 */

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.jotto.unitime.models.Event;
import com.jotto.unitime.sessionhandler.SessionHandler;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class FragmentB extends Fragment {


    // Comment this list of fields later and declare all of their uses
    private FragmentActivity myContext;
    private Calendar cal;
    private Date date1;
    private Date clickedDate;
    private PopupWindow popupWindow;
    ArrayList<Event> events;
    ArrayAdapter<Event> adapter;
    CaldroidFragment calDroid;

    // i dont know how to describe what this does yet
    @Override
    public void onAttach(Activity activity) {
        myContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }


    /*
    PopupWindow to show information of events.
     */
    public void onShowPopup(View v) {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // inflate the custom popup layout
        final View inflatedView = layoutInflater.inflate(R.layout.calendar_popup, null,false);

        // find the ListView in the popup layout
        TextView textView = (TextView)inflatedView.findViewById(R.id.popup_textview);

        // set temporary text for popup
        textView.setText("Sadly this event has no content yet, but we can find the date which " +
                "I know realized is completly useless. " +clickedDate.toString());

        // get device size
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);


        // set height depends on the device size
        popupWindow = new PopupWindow(inflatedView, size.x - 100 ,size.y / 4, true );
        // set a background drawable with rounders corners
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_background));
        // make it focusable to show the keyboard to enter in `EditText`
        popupWindow.setFocusable(true);
        // make it outside touchable to dismiss the popup window
        popupWindow.setOutsideTouchable(false);

        // show the popup at bottom of the screen and set some margin at bottom ie,
        popupWindow.showAtLocation(v, Gravity.CENTER, 0, 100);

    }


    // If there is a database, fetch all of the events stored in event objects and add to arraylist
    private void getEventsFromDatabase() {
        if (doesDatabaseExist(myContext, "unitime.db")) {
            List<Event> retrievedEvents = Event.listAll(Event.class);
            events = new ArrayList<>(retrievedEvents);
            if(events.size() != 0){
                for (int i = 0; i < events.size(); i++) {
                    DateTime dateTime = new DateTime(events.get(i).getStartdate());
                    calDroid.setBackgroundResourceForDate(R.color.blue, dateTime.toDate());
                }
            }
        }
    }

    // Check if database exists
    private static boolean doesDatabaseExist(ContextWrapper context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_b, container, false);

        return inflater.inflate(R.layout.fragment_b,container,false);
    }

    private void getDate(Date date){
        clickedDate = date;
    }

    private class GetCourseInfoTask extends AsyncTask {
        protected void onPostExecute() {
            getEventsFromDatabase();
            adapter.notifyDataSetChanged();
        }
        @Override
        protected Object doInBackground(Object[] params) {
            SessionHandler sessionHandler = new SessionHandler();
            sessionHandler.getEventsFromCourse(params[0].toString());
            return null;
        }
    }


    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        calDroid = new CaldroidFragment();
        Bundle args = new Bundle();
        cal = Calendar.getInstance();
        date1 = new Date(System.currentTimeMillis());
        calDroid.setMinDate(date1);
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        calDroid.setArguments(args);


        /*
        Testing different colored cells
         */

        /*
        // first date
        DateTime dateTime = new DateTime(System.currentTimeMillis());
        dateTime = dateTime.plusDays(3);

        // second date
        DateTime secondDateTime = new DateTime(System.currentTimeMillis());
        secondDateTime = secondDateTime.plusDays(5);

        // third date
        DateTime thirdDateTime = new DateTime(System.currentTimeMillis());
        thirdDateTime = thirdDateTime.plusDays(6);

        // assigning new backgroundresorces for three cells (dates)
        calDroid.setBackgroundResourceForDate(R.color.blue, dateTime.toDate());
        calDroid.setBackgroundResourceForDate(R.color.darkgreen, secondDateTime.toDate());
        calDroid.setBackgroundResourceForDate(R.color.blue, thirdDateTime.toDate());
        */


        // testing cell touch
        final CaldroidListener caldroidListener = new CaldroidListener() {
            @Override
            public void onSelectDate(Date date, View view) {
                getDate(date);
                onShowPopup(view);
            }
        };
        calDroid.setCaldroidListener(caldroidListener);

        android.support.v4.app.FragmentTransaction t = myContext
                .getSupportFragmentManager().beginTransaction();
        t.replace(R.id.llCalendar, calDroid);
        t.commit();

        if (doesDatabaseExist(myContext, "unitime.db")) {
            getEventsFromDatabase();
        }
        else {
            new GetCourseInfoTask().execute("1BD105");
        }
    }
}
