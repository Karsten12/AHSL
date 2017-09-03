package com.fonsecakarsten.ahsl.Grades;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fonsecakarsten.ahsl.Constants;
import com.fonsecakarsten.ahsl.Misc.API;
import com.fonsecakarsten.ahsl.Misc.Utils;
import com.fonsecakarsten.ahsl.R;
import com.fonsecakarsten.ahsl.RefreshTask;
import com.fonsecakarsten.ahsl.RemoteDebug;

import java.io.IOException;
import java.util.List;

public class Grades_Fragment extends ListFragment {

    public static final String COURSE_SELECTED = "COURSE_SELECTED";
    private GradesAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrapeGradesTask task = new ScrapeGradesTask();
        task.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= 21) {
            TransitionInflater inflater1 = TransitionInflater.from(getActivity());
            Transition transition = inflater1.inflateTransition(R.transition.transition);
            getActivity().getWindow().setExitTransition(transition);
            Slide slide = new Slide();
            slide.setDuration(1000);
            getActivity().getWindow().setEnterTransition(slide);
            getActivity().getWindow().setReenterTransition(slide);
        }


        View rootView = inflater.inflate(R.layout.google_card, container, false);
        setHasOptionsMenu(true);

        return rootView;
    }

    private class ScrapeGradesTask extends AsyncTask<String, Void, List<Course>> {
        @Override
        protected List<Course> doInBackground(String... params) {
            try {
                return API.get().getCourses();
            } catch (Exception e) {
                RemoteDebug.debugException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Course> result) {
            super.onPostExecute(result);

            Course[] values = result.toArray(new Course[result.size()]);

            if (values.length > 0) {

                CardView spaceshipImage = (CardView) getView().findViewById(R.id.card_view);
                Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_card);
                spaceshipImage.startAnimation(hyperspaceJumpAnimation);

                ListView listView = (ListView) getView().findViewById(android.R.id.list);

                adapter = new GradesAdapter(getActivity(), values);

                listView.setOnItemClickListener(new GradesItemClickAdapter(adapter, getActivity()));

                setListAdapter(adapter);
            } else {
                ListView listView = (ListView) getView().findViewById(android.R.id.list);
                TextView emptyText = Utils.getCenteredTextView(getActivity(), getString(R.string.empty_courses));

                Utils.showViewOnTop(listView, emptyText);
            }
        }
    }

    private class GradesAdapter extends ArrayAdapter<Course> {
        private Context context;
        private Course[] values;

        public GradesAdapter(Context context, Course[] values) {
            super(context, R.layout.grades_row, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View ConvertView, ViewGroup parent) {
            View rowView = ConvertView;

            if (ConvertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.grades_row, parent, false);
            }

            TextView coursePer = (TextView) rowView.findViewById(R.id.course_period);
            TextView courseName = (TextView) rowView.findViewById(R.id.grades_course_name);
            TextView letterGrade = (TextView) rowView.findViewById(R.id.grades_lett_grade);
            TextView pctGrade = (TextView) rowView.findViewById(R.id.grades_pct_grade);
            TextView zero = (TextView) rowView.findViewById(R.id.grade_zeroes);

            Course course = values[position];

            coursePer.setText(course.getPeriod());
            courseName.setText(course.getName());

            if (!(course.getLetterGrade().length() == 0))
                letterGrade.setText(course.getLetterGrade());
            else
                letterGrade.setText(Constants.EMPTY_INDIC);


            if (!(course.getPercentGrade().length() == 0)) {
                char tensPlace = course.getPercentGrade().charAt(0);

                if (Character.isDigit(tensPlace)) {
                    String gradeHighlight;

                    if (tensPlace == '9' || tensPlace == '1') {
                        gradeHighlight = "#009900";
                    } else if (tensPlace <= '8') {
                        gradeHighlight = "#3333FF";
                    } else if (tensPlace <= '7') {
                        gradeHighlight = "#D1D100";
                    } else if (tensPlace <= '6') {
                        gradeHighlight = "#FFB366";
                    } else {
                        gradeHighlight = "#CC0000";
                    }

                    pctGrade.setText(Html.fromHtml("<font color=\"" + gradeHighlight + "\">" + course.getPercentGrade() + "</font>"));
                } else {
                    pctGrade.setText(Html.fromHtml("<font color=\"#000000\">" + course.getPercentGrade() + "</font>"));
                }
            } else {
                pctGrade.setVisibility(View.INVISIBLE);
            }

            if (course.getNumZeros() > 1) {
                zero.setText(course.getNumZeros() + " zeros");
                courseName.setTextColor(getResources().getColor(R.color.Red));
            } else if (course.getNumZeros() == 1) {
                zero.setText(course.getNumZeros() + " zero");
                courseName.setTextColor(getResources().getColor(R.color.Red));
            } else {
                zero.setVisibility(View.GONE);
                courseName.setTextColor(getResources().getColor(R.color.Black));
            }

            return rowView;
        }
    }
    /*
    public int GPA(Course course){
        Course values = null;
        int value = 0;
        if (values.getName().contains("AP")) {
            if (values.getLetterGrade().contains("A")) {
                value = 5;
            }
            if (values.getLetterGrade().contains("B")) {
                value = 4;
            }
            if (values.getLetterGrade().contains("C")) {
                value = 3;
            }
            if (values.getLetterGrade().contains("D")) {
                value = 2;
            }
            if (values.getLetterGrade().contains("F")) {
                value = 1;
            }

        } else {
            if (values.getLetterGrade().contains("A")) {
                value = 4;
            }
            if (values.getLetterGrade().contains("B")) {
                value = 3;
            }
            if (values.getLetterGrade().contains("C")) {
                value = 2;
            }
            if (values.getLetterGrade().contains("D")) {
                value = 1;
            }
            if (values.getLetterGrade().contains("F")) {
                value = 0;
            }

        }
        return values;
    } */

    private class GradesItemClickAdapter implements AdapterView.OnItemClickListener {
        GradesAdapter adapter;
        Activity parent;

        public GradesItemClickAdapter(GradesAdapter adapter, Activity parent) {
            this.adapter = adapter;
            this.parent = parent;
        }

        @Override
        public void onItemClick(AdapterView<?> list, View view, int position, long id) {
            if (Utils.isNetworkOffline(parent)) return;

            Course selectedCourse = adapter.getItem(position);

            if (selectedCourse == null ||
                    selectedCourse.getDetailsUrl() == null ||
                    selectedCourse.getDetailsUrl().length() == 0) {
                Toast.makeText(parent, "Progress report for course is unpublished/unavailable.", Toast.LENGTH_LONG).show();
                return;
            }

            @SuppressWarnings("NullArgumentToVariableArgMethod") ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), null);

            Intent detailsIntent = new Intent(getActivity(), GradeDetailsActivity.class);
            detailsIntent.putExtra(COURSE_SELECTED, selectedCourse);
            parent.startActivity(detailsIntent, compat.toBundle());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        refresh();
        return true;
    }


    void refresh() {
        if (Utils.isNetworkOffline(getActivity())) return;

        preRefresh();

        //System.out.println("Refreshing Grades");
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

                ScrapeGradesTask task = new ScrapeGradesTask();
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

                //System.out.println("Finished refreshing Grades");
                postRefresh();
            }
        };

        RefreshTask refreshTask = new RefreshTask(firstJob, secondJob);
        refreshTask.execute();
    }


    void preRefresh() {
        Utils.lockOrientation(getActivity());
    }


    void postRefresh() {
        Utils.unlockOrientation(getActivity());
    }

}

