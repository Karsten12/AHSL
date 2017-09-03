package com.fonsecakarsten.ahsl.Assignments;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fonsecakarsten.ahsl.Constants;
import com.fonsecakarsten.ahsl.Misc.API;
import com.fonsecakarsten.ahsl.Misc.Utils;
import com.fonsecakarsten.ahsl.R;
import com.fonsecakarsten.ahsl.RefreshTask;
import com.fonsecakarsten.ahsl.RemoteDebug;
import com.fonsecakarsten.ahsl.SortType;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Assignments_Fragment extends ListFragment {

    private CurrentAssignmentsAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrapeAssignmentsTask task = new ScrapeAssignmentsTask();
        task.execute();
    }

    private static final Comparator<CurrentAssignment> DATE_COMPARATOR = new Comparator<CurrentAssignment>() {
        @Override
        public int compare(CurrentAssignment lhs, CurrentAssignment rhs) {
            Date d1 = null;
            Date d2 = null;

            try {
                d1 = Constants.LOOPED_DATE_FORMAT.parse(lhs.getDueDate());
                d2 = Constants.LOOPED_DATE_FORMAT.parse(rhs.getDueDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (d1 != null && d2 != null)
                return d1.compareTo(d2);

            return 0;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.google_card, container, false);
        setHasOptionsMenu(true);

        return rootView;
    }

    private class ScrapeAssignmentsTask extends AsyncTask<String, Void, List<CurrentAssignment>> {
        @Override
        protected List<CurrentAssignment> doInBackground(String... params) {
            try {
                return API.get().getCurrentAssignments();
            } catch (Exception e) {
                RemoteDebug.debugException(e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<CurrentAssignment> result) {
            super.onPostExecute(result);

            CurrentAssignment[] values = result.toArray(new CurrentAssignment[result.size()]);

            if (values.length > 0) {

                CardView spaceshipImage = (CardView) getView().findViewById(R.id.card_view);
                Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_card);
                spaceshipImage.startAnimation(hyperspaceJumpAnimation);

                ListView listView = (ListView) getView().findViewById(android.R.id.list);

                adapter = new CurrentAssignmentsAdapter(getActivity(), values);

                listView.setAdapter(adapter);

//                listView.setOnItemClickListener(new AssignmentItemClickAdapter(adapter, getActivity()));

            } else {
                ListView listView = (ListView) getView().findViewById(android.R.id.list);
                TextView emptyText = Utils.getCenteredTextView(getActivity(), getString(R.string.empty_assignments));

                Utils.showViewOnTop(listView, emptyText);
            }
        }
    }

    private class CurrentAssignmentsAdapter extends ArrayAdapter<CurrentAssignment> {
        private final Context context;
        private final CurrentAssignment[] values;
        private boolean sortDir = true;

        public CurrentAssignmentsAdapter(Context context, CurrentAssignment[] values) {
            super(context, R.layout.assignments_row, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View rootView, ViewGroup parent) {
            View rowView = rootView;

            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.assignments_row, parent, false);
            }

            TextView name = (TextView) rowView.findViewById(R.id.assign_name);
            TextView courseName = (TextView) rowView.findViewById(R.id.assign_course_name);
            TextView dueDate = (TextView) rowView.findViewById(R.id.assign_due_date);

            CurrentAssignment assignment = values[position];

            // Get current date for due today
            DateFormat dateFormat = new SimpleDateFormat("M/d/yy");
            Date date = new Date();

            if (assignment.getDueDate().contains(dateFormat.format(date))) {
                dueDate.setText("Due: Today");
                dueDate.setBackgroundColor(getResources().getColor(R.color.Red));
                dueDate.setTextColor(getResources().getColor(R.color.White));
            } else
                dueDate.setText(assignment.getDueDate());

            name.setText(assignment.getName());
            courseName.setText(assignment.getCourseName());

            return rowView;
        }

        public CurrentAssignment[] getValues() {
            return values;
        }

        public boolean getSortOrder() {
            return sortDir;
        }

        public void toggleSortOrder() {
            sortDir = !sortDir;
        }
    }

    /*
    private class ScrapeAssignmentDetailsTask extends AsyncTask<CurrentAssignment, Void, AssignmentDetail> {
        private View flow;
        private View content;
        private ProgressBar bar;

        public ScrapeAssignmentDetailsTask(View flow, View content, ProgressBar bar) {
            this.flow = flow;
            this.bar = bar;
            this.content = content;
        }

        @Override
        protected AssignmentDetail doInBackground(CurrentAssignment... params) {
            try {
                return API.get().getAssignmentDetails(params[0]);
            } catch (Exception e) {
                RemoteDebug.debugException(e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(AssignmentDetail assignDetail) {
            super.onPostExecute(assignDetail);

            TextView title = (TextView) flow.findViewById(R.id.assigndet_title);
            TextView audience = (TextView) flow.findViewById(R.id.assigndet_audi);
            TextView info = (TextView) flow.findViewById(R.id.assigndet_info);
            WebView explanation = (WebView) flow.findViewById(R.id.assigndet_expl);

            title.setText(assignDetail.getName());
            audience.setText(assignDetail.getTargetAudience());

            String separator = "<br />";
            String explCont = assignDetail.getExplanation() + separator + assignDetail.getAttachments();

            if(!(explCont.replace(separator, "").length() == 0))
                explanation.loadDataWithBaseURL(null, explCont, "text/html", "UTF-8", null);
            else
                ((LinearLayout) explanation.getParent()).removeView(explanation);

            String infoStr = "";
            String colonDelim = ": ";

            for(String detail : assignDetail.getDetails()) {
                if(detail.startsWith("Assigned") && detail.contains("Due") && detail.contains(colonDelim)) {
                    int idx = detail.indexOf("Due");
                    String assigned = detail.substring(0, idx);
                    String due = detail.substring(idx);

                    String parts1[] = assigned.split(colonDelim);
                    String parts2[] = due.split(colonDelim);

                    infoStr += "<b>" + parts1[0] + "</b>: " + parts1[1]
                            + separator + "<b>" + parts2[0] + "</b>: "
                            + parts2[1] + separator;
                } else if(detail.contains(colonDelim)) {
                    String parts[] = detail.split(colonDelim);
                    infoStr += "<b>" + parts[0] + "</b>: " + parts[1] + separator;
                } else {
                    infoStr += detail + separator;
                }
            }

            content.setFocusable(true);
            content.setFocusableInTouchMode(true);

            info.setText(Html.fromHtml(infoStr));
            bar.setVisibility(View.GONE);

            content.setVisibility(View.VISIBLE);
        }
    };

    private class AssignmentItemClickAdapter implements AdapterView.OnItemClickListener {
        CurrentAssignmentsAdapter adapter;
        Activity parent;

        public AssignmentItemClickAdapter(CurrentAssignmentsAdapter adapter, Activity parent) {
            this.adapter = adapter;
            this.parent = parent;
        }

        @Override
        public void onItemClick(AdapterView<?> list, View view, int position, long id) {
            if(Utils.isNetworkOffline(parent)) return;

            LayoutInflater inflater = (LayoutInflater) parent.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout flow = (LinearLayout) inflater.inflate(R.layout.assignment_details_popup, null, false);
            LinearLayout content = (LinearLayout) flow.findViewById(R.id.assigndet_content);
            ProgressBar load = (ProgressBar) flow.findViewById(R.id.popup_prog);

            Display display = parent.getWindowManager().getDefaultDisplay();
            int width = display.getWidth();

            Dialog popup = Utils.createLoopedDialog(parent, flow, width);

            load.setVisibility(View.VISIBLE);
            content.setVisibility(View.GONE);

            popup.show();

            CurrentAssignment selected = (CurrentAssignment) adapter.getItem(position);
            ScrapeAssignmentDetailsTask task = new ScrapeAssignmentDetailsTask(flow, content, load);
            task.execute(selected);
        }
    }
    */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        refresh();
        return true;
    }

    void refresh() {
        if (Utils.isNetworkOffline(getActivity())) return;

        preRefresh();

        //System.out.println("Refreshing Assignments");
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.refresh));

        Runnable firstJob = new Runnable() {
            @Override
            public void run() {
                try {
                    API.get().refreshMainPortal();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Utils.safelyDismissDialog(progressDialog);
                }

                ScrapeAssignmentsTask task = new ScrapeAssignmentsTask();
                task.execute();
            }
        };

        Runnable secondJob = new Runnable() {
            @Override
            public void run() {
                try {
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Utils.safelyDismissDialog(progressDialog);
                }

                //System.out.println("Finished refreshing Assignments");
                postRefresh();
            }
        };

        RefreshTask refreshTask = new RefreshTask(firstJob, secondJob);
        refreshTask.execute();

    }

    public void sort(SortType type) {
        if (type == SortType.DATE) {
            if (adapter != null) {
                adapter.toggleSortOrder();

                Collections.sort(Arrays.asList(adapter.getValues()),
                        adapter.getSortOrder() ? DATE_COMPARATOR : Collections.reverseOrder(DATE_COMPARATOR));
                adapter.notifyDataSetChanged();
            }
        }
    }

    void preRefresh() {
        Utils.lockOrientation(getActivity());
    }


    void postRefresh() {
        Utils.unlockOrientation(getActivity());
    }

}
