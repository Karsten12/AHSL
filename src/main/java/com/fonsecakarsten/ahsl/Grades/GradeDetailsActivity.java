package com.fonsecakarsten.ahsl.Grades;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fonsecakarsten.ahsl.Constants;
import com.fonsecakarsten.ahsl.Misc.API;
import com.fonsecakarsten.ahsl.Misc.Utils;
import com.fonsecakarsten.ahsl.R;
import com.fonsecakarsten.ahsl.RefreshTask;
import com.fonsecakarsten.ahsl.Refreshable;
import com.fonsecakarsten.ahsl.RemoteDebug;
import com.fonsecakarsten.ahsl.SortType;
import com.fonsecakarsten.ahsl.Sortable;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;


public class GradeDetailsActivity extends AppCompatActivity implements Refreshable, Sortable {

    private GradeDetailsAdapter adapter;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;

    private static final Comparator<GradeDetail> DATE_COMPARATOR = new Comparator<GradeDetail>() {
        @Override
        public int compare(GradeDetail lhs, GradeDetail rhs) {
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            Slide slide = new Slide();
            slide.setDuration(1000);
            getWindow().setEnterTransition(slide);
            getWindow().setReenterTransition(TransitionInflater.from(this).inflateTransition(R.transition.transition));
        }

        setContentView(R.layout.grade_details_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        Course currCourse = (Course) getIntent().getSerializableExtra(Grades_Fragment.COURSE_SELECTED);

        getSupportActionBar().setTitle(currCourse.getName());
        getSupportActionBar().setSubtitle(currCourse.getPercentGrade() + " " + currCourse.getLetterGrade());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        ScrapeGradeDetailsTask task = new ScrapeGradeDetailsTask(false);
        task.execute(currCourse);

        ScrapeBreakdown task2 = new ScrapeBreakdown();
        task2.execute(currCourse);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout2);
        mDrawerList = (ListView) findViewById(R.id.right_drawer_child);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.grade_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                this.refresh();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.getBreakdown:
                if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                    mDrawerLayout.closeDrawer(mDrawerList);
                } else mDrawerLayout.openDrawer(mDrawerList);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
        } else super.onBackPressed();
    }

    public class ScrapeGradeDetailsTask extends AsyncTask<Course, Void, ArrayList<GradeDetail>> {
        private ProgressDialog load;
        private boolean fromRefresh;


        public ScrapeGradeDetailsTask(boolean fromRefresh) {
            this.fromRefresh = fromRefresh;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (!fromRefresh)
                load = ProgressDialog.show(GradeDetailsActivity.this, "", "Retrieving grades");
        }

        @Override
        protected ArrayList<GradeDetail> doInBackground(Course... params) {
            try {
                return (ArrayList<GradeDetail>) API.get().getGradeDetails(params[0]);
            } catch (Exception e) {
                RemoteDebug.debugException(e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<GradeDetail> result) {
            super.onPostExecute(result);

            if (!fromRefresh)
                Utils.safelyDismissDialog(load);

            GradeDetail[] values = result.toArray(new GradeDetail[result.size()]);

            if (values.length > 0) {

                ListView listView = (ListView) findViewById(android.R.id.list);

                adapter = new GradeDetailsAdapter(GradeDetailsActivity.this, values);

                listView.setAdapter(adapter);


            } else {
                ListView listView = (ListView) findViewById(android.R.id.list);
                TextView emptyText = Utils.getCenteredTextView(GradeDetailsActivity.this, "No assignments listed");

                Utils.showViewOnTop(listView, emptyText);
            }
        }
    }

    private class GradeDetailsAdapter extends ArrayAdapter<GradeDetail> {
        private final Context context;
        private final GradeDetail[] values;
        private boolean sortDir;
        private static final int list = 0;
        private static final int header = 1;

        public GradeDetailsAdapter(Context context, GradeDetail[] values) {
            super(context, R.layout.grade_detail_row, values);
            this.context = context;
            this.values = values;

            Collections.sort(Arrays.asList(values), Collections.reverseOrder(DATE_COMPARATOR));

        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return header;
            }
            return list;
        }

        //private int lastPosition = -1;

        @Override
        public View getView(int position, View rowView, ViewGroup parent) {

            GradeDetail detail = values[position];

            if (getItemViewType(position) == header) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.sub_header, parent, false);

                TextView header_Text = (TextView) rowView.findViewById(R.id.header_name);
                header_Text.setText("Last published " + detail.getLastPublishDate());

            } else {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.grade_detail_row, parent, false);

                TextView name = (TextView) rowView.findViewById(R.id.gradedet_name);
                TextView category = (TextView) rowView.findViewById(R.id.gradedet_categ);
                TextView date = (TextView) rowView.findViewById(R.id.gradedet_date);
                TextView percent = (TextView) rowView.findViewById(R.id.gradedet_pct);
                TextView score = (TextView) rowView.findViewById(R.id.gradedet_score);

                name.setText(detail.getDetailName());
                category.setText(detail.getCategory());
                date.setText(detail.getDueDate());
                score.setText(detail.getDisplayScore());
                percent.setText(detail.getDisplayPercent());

                if (detail.getDisplayPercent().equals("0.00%") || detail.getDisplayPercent().isEmpty()) {
                    name.setTextColor(ContextCompat.getColor(context, R.color.Red));
                    percent.setTextColor(ContextCompat.getColor(context, R.color.Red));
                } else {
                    name.setTextColor(ContextCompat.getColor(context, R.color.Black));
                    percent.setTextColor(ContextCompat.getColor(context, R.color.Black));
                }

                if (detail.getDisplayScore().length() == 0) {
                    score.setText("--");
                }

                if (detail.getDisplayPercent().isEmpty()) {
                    percent.setText("--");
                }

//                Animation animation = AnimationUtils.loadAnimation(getContext(), (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
//                rowView.startAnimation(animation);
//                lastPosition = position;

            }
            return rowView;
        }

        public GradeDetail[] getValues() {
            return values;
        }

        public boolean getSortOrder() {
            return sortDir;
        }

        public void toggleSortOrder() {
            sortDir = !sortDir;
        }
    }

    public void refresh() {
        if (Utils.isNetworkOffline(this)) return;

        preRefresh();

        //System.out.println("Refreshing Grade Details");
        final ProgressDialog progressDialog = ProgressDialog.show(this, "", "Refreshing...");

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

                ScrapeGradeDetailsTask task = new ScrapeGradeDetailsTask(true);
                task.execute((Course) getIntent().getSerializableExtra(Grades_Fragment.COURSE_SELECTED));
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

                //System.out.println("Finished refreshing Grade Details");
                postRefresh();
            }
        };

        RefreshTask refreshTask = new RefreshTask(firstJob, secondJob);
        refreshTask.execute();
    }

    @Override
    public void sort(SortType type) {
        if (adapter != null) {
            adapter.toggleSortOrder();

            Collections.sort(Arrays.asList(adapter.getValues()),
                    adapter.getSortOrder() ? DATE_COMPARATOR : Collections.reverseOrder(DATE_COMPARATOR));
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void preRefresh() {
        Utils.lockOrientation(this);
    }

    @Override
    public void postRefresh() {
        Utils.unlockOrientation(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.gc();
    }

    private class ScrapeBreakdown extends AsyncTask<Course, Void, ArrayList<GradeDetail>> {

        @Override
        protected ArrayList<GradeDetail> doInBackground(Course... params) {
            try {
                return (ArrayList<GradeDetail>) API.get().getBreakdown(params[0]);
            } catch (Exception e) {
                RemoteDebug.debugException(e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<GradeDetail> result2) {
            super.onPostExecute(result2);

            GradeDetail[] values = result2.toArray(new GradeDetail[result2.size()]);

            if (values.length > 0) {

                GradeBreakdownAdapter adapter2 = new GradeBreakdownAdapter(GradeDetailsActivity.this, values);

                mDrawerList.setAdapter(adapter2);

            }
        }
    }


    private class GradeBreakdownAdapter extends ArrayAdapter<GradeDetail> {
        private final Context context;
        private final GradeDetail[] values;

        public GradeBreakdownAdapter(Context context, GradeDetail[] values) {
            super(context, R.layout.grade_detail_row, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.score_category_row, parent, false);
            }

            TextView category = (TextView) rowView.findViewById(R.id.score_category);
            TextView weight = (TextView) rowView.findViewById(R.id.score_weight);
            TextView percent = (TextView) rowView.findViewById(R.id.score_percent);

            GradeDetail detail = values[position];

            category.setText(detail.getScoreCategory());

            if (detail.getScoreCategory().isEmpty()) {
                weight.setText("Weight: --");
            } else weight.setText("Weight: " + detail.getScoreWeight());

            percent.setText("Score: " + detail.getScorePercent());


            return rowView;
        }
    }


}
