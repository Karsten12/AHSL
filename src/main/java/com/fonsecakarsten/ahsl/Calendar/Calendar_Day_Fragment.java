package com.fonsecakarsten.ahsl.Calendar;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fonsecakarsten.ahsl.Misc.API;
import com.fonsecakarsten.ahsl.Misc.Utils;
import com.fonsecakarsten.ahsl.R;
import com.fonsecakarsten.ahsl.RemoteDebug;

import java.util.List;

public class Calendar_Day_Fragment extends ListFragment {

    public CalendarAdapter adapter;

    public Calendar_Day_Fragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrapeCalendarTask task = new ScrapeCalendarTask();
        task.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.google_card, container, false);
    }

    private class ScrapeCalendarTask extends AsyncTask<String, Void, List<calendar>> {
        @Override
        protected List<calendar> doInBackground(String... params) {
            try {
                return API.get().getCalendar();
            } catch (Exception e) {
                RemoteDebug.debugException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<calendar> result) {
            super.onPostExecute(result);

            calendar[] values = result.toArray(new calendar[result.size()]);

            if (values.length > 0) {
                adapter = new CalendarAdapter(getActivity(), values);

                setListAdapter(adapter);
            } else {
                ListView listView = (ListView) getView().findViewById(android.R.id.list);
                TextView emptyText = Utils.getCenteredTextView(getActivity(), getString(R.string.empty_courses));

                Utils.showViewOnTop(listView, emptyText);
            }
        }

    }

    private class CalendarAdapter extends ArrayAdapter<calendar> {
        private Context context;
        private calendar[] values;
        private static final int list = 0;
        private static final int header = 1;


        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public int getItemViewType(int position) {
//            if (position % 2 == 0 || position == 0) {
//
//                return header;
//            }
            return list;
        }

        public CalendarAdapter(Context context, calendar[] values) {
            super(context, 0, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View ConvertView, ViewGroup parent) {
            View rowView;
            calendar calendar = values[position];

            if (getItemViewType(position) == header) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.sub_header, parent, false);

                TextView subheader = (TextView) rowView.findViewById(R.id.header_name);
                subheader.setText(calendar.getWeekDay());
            } else {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.assignments_row, parent, false);

                TextView name = (TextView) rowView.findViewById(R.id.assign_name);
                TextView courseName = (TextView) rowView.findViewById(R.id.assign_course_name);
                TextView assignment_type = (TextView) rowView.findViewById(R.id.assign_due_date);

                name.setText(calendar.getAssignment());
                courseName.setText(calendar.getCourseName());
                assignment_type.setText(calendar.getAssignmentType());
            }
            return rowView;
        }
    }

}
