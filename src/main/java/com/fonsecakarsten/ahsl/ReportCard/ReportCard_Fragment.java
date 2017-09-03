package com.fonsecakarsten.ahsl.ReportCard;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

public class ReportCard_Fragment extends Fragment {


    public ReportCard_Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        return inflater.inflate(R.layout.google_card, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        ScrapeReportCard task = new ScrapeReportCard();
        task.execute();
    }

    private class ScrapeReportCard extends AsyncTask<String, Void, List<ReportCourse>> {
        @Override
        protected List<ReportCourse> doInBackground(String... params) {
            try {
                return API.get().getReportCard();
            } catch (Exception e) {
                RemoteDebug.debugException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<ReportCourse> result) {
            super.onPostExecute(result);

            ReportCourse[] values = result.toArray(new ReportCourse[result.size()]);

            if (values.length > 0) {
                ListView listView = (ListView) getView().findViewById(android.R.id.list);

                ReportCardAdapter adapter = new ReportCardAdapter(getActivity(), values);

                listView.setAdapter(adapter);
            } else {
                ListView listView = (ListView) getView().findViewById(android.R.id.list);
                TextView emptyText = Utils.getCenteredTextView(getActivity(), getString(R.string.empty_courses));

                Utils.showViewOnTop(listView, emptyText);
            }
        }
    }

    private class ReportCardAdapter extends ArrayAdapter<ReportCourse> {
        private Context context;
        private ReportCourse[] values;

        public ReportCardAdapter(Context context, ReportCourse[] values) {
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

            ReportCourse reportCard = values[position];

            zero.setVisibility(View.INVISIBLE);
            pctGrade.setVisibility(View.INVISIBLE);
            courseName.setText(reportCard.getCourse());
            coursePer.setText(reportCard.getPeriod());
            letterGrade.setText(reportCard.getLetterGrade());

            return rowView;
        }
    }
}
