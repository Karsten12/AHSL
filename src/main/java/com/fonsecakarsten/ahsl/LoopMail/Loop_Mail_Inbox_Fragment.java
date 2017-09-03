package com.fonsecakarsten.ahsl.LoopMail;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.eevoskos.robotoviews.widget.RobotoTextView;
import com.fonsecakarsten.ahsl.Constants;
import com.fonsecakarsten.ahsl.Misc.API;
import com.fonsecakarsten.ahsl.Misc.Utils;
import com.fonsecakarsten.ahsl.R;
import com.fonsecakarsten.ahsl.RefreshTask;
import com.fonsecakarsten.ahsl.RemoteDebug;
import com.fonsecakarsten.ahsl.SortType;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Loop_Mail_Inbox_Fragment extends ListFragment {


    private LoopMailAdapter adapter;

    private enum LoopMailBoxType {
        INBOX
    }

    public Loop_Mail_Inbox_Fragment() {
    }

    private static final Comparator<MailEntry> DATE_COMPARATOR = new Comparator<MailEntry>() {
        @Override
        public int compare(MailEntry lhs, MailEntry rhs) {
            Date d1 = null;
            Date d2 = null;

            try {
                d1 = Constants.LOOPED_DATE_FORMAT.parse(lhs.getTimestamp());
                d2 = Constants.LOOPED_DATE_FORMAT.parse(rhs.getTimestamp());
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

        ScrapeLoopMailTask task = new ScrapeLoopMailTask();
        task.execute(LoopMailBoxType.INBOX);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.google_card, container, false);
        setHasOptionsMenu(true);

        return rootView;
    }

    private class ScrapeLoopMailTask extends AsyncTask<LoopMailBoxType, Void, List<MailEntry>> {
        @Override
        protected List<MailEntry> doInBackground(LoopMailBoxType... params) {
            try {
                return API.get().getMailInbox();
            } catch (Exception e) {
                RemoteDebug.debugException(e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<MailEntry> result) {
            super.onPostExecute(result);

            MailEntry[] values = result.toArray(new MailEntry[result.size()]);
            if (values.length > 0) {

                CardView spaceshipImage = (CardView) getView().findViewById(R.id.card_view);
                Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_card);
                spaceshipImage.startAnimation(hyperspaceJumpAnimation);

                ListView listView = (ListView) getView().findViewById(android.R.id.list);

                adapter = new LoopMailAdapter(getActivity(), values);

                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new MailItemClickAdapter(adapter, getActivity()));

            } else {
                ListView listView = (ListView) getView().findViewById(android.R.id.list);
                TextView emptyText = Utils.getCenteredTextView(getActivity(), getString(R.string.empty_mail));

                Utils.showViewOnTop(listView, emptyText);
            }
        }
    }

    private class LoopMailAdapter extends ArrayAdapter<MailEntry> {
        private final Context context;
        private final MailEntry[] values;
        private boolean sortDir;

        public LoopMailAdapter(Context context, MailEntry[] values) {
            super(context, R.layout.loopmail_row, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.loopmail_row, parent, false);
            }

            TextView timestamp = (TextView) rowView.findViewById(R.id.mail_date);
            TextView parties = (TextView) rowView.findViewById(R.id.mail_sender);
            TextView subject = (TextView) rowView.findViewById(R.id.mail_subject);

            MailEntry entry = values[position];

            timestamp.setText(entry.getTimestamp());
            parties.setText(entry.getInvolvedParties());
            subject.setText(entry.getSubject());

            return rowView;
        }

        public MailEntry[] getValues() {
            return values;
        }

        public boolean getSortOrder() {
            return sortDir;
        }

        public void toggleSortOrder() {
            sortDir = !sortDir;
        }
    }

    private class ScrapeMailContentTask extends AsyncTask<MailEntry, Void, MailDetail> {
        private View flow;
        private View contwrap;
        private ProgressBar bar;

        public ScrapeMailContentTask(View flow, View contwrap, ProgressBar bar) {
            this.flow = flow;
            this.bar = bar;
            this.contwrap = contwrap;
        }

        @Override
        protected MailDetail doInBackground(MailEntry... params) {
            try {
                return API.get().getMailDetails(params[0]);
            } catch (Exception e) {
                RemoteDebug.debugException(e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(MailDetail mailDetail) {
            super.onPostExecute(mailDetail);

            if (mailDetail == null) {
                Toast.makeText(getActivity(),
                        "Session may be expired, try logging out and back in again.", Toast.LENGTH_LONG).show();
                return;
            }

            TextView to = (TextView) flow.findViewById(R.id.maildet_to);
            TextView from = (TextView) flow.findViewById(R.id.maildet_from);
            TextView date = (TextView) flow.findViewById(R.id.maildet_date);
            RobotoTextView subject = (RobotoTextView) flow.findViewById(R.id.popup_title);
            WebView content = (WebView) flow.findViewById(R.id.maildet_content);


            to.setText(Html.fromHtml(mailDetail.getTo()));
            from.setText(Html.fromHtml(mailDetail.getFrom()));
            subject.setText(Html.fromHtml(mailDetail.getSubject()));
            date.setText(Html.fromHtml(mailDetail.getDate()));


            if (mailDetail.getContent().length() != 0)
                content.loadDataWithBaseURL(null, mailDetail.getContent(), "text/html", "UTF-8", null);
            else
                ((LinearLayout) content.getParent()).removeView(content);

            content.setFocusable(true);
            content.setFocusableInTouchMode(true);

            bar.setVisibility(View.GONE);
            contwrap.setVisibility(View.VISIBLE);
        }
    }

    private class MailItemClickAdapter implements AdapterView.OnItemClickListener {
        LoopMailAdapter adapter;
        Activity parent;

        public MailItemClickAdapter(LoopMailAdapter adapter, Activity parent) {
            this.adapter = adapter;
            this.parent = parent;
        }

        @Override
        public void onItemClick(AdapterView<?> list, View view, int position, long id) {
            if (Utils.isNetworkOffline(parent)) return;

            Point size = new Point();
            parent.getWindowManager().getDefaultDisplay().getSize(size);
            int width = size.x;

            LayoutInflater inflater = (LayoutInflater) parent.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout flow = (LinearLayout) inflater.inflate(R.layout.mail_details_popup, null, false);
            LinearLayout contwrap = (LinearLayout) flow.findViewById(R.id.maildet_contwrap);
            ProgressBar load = (ProgressBar) flow.findViewById(R.id.popup_prog);

            Dialog popup = Utils.createLoopedDialog(parent, flow, width);

            load.setVisibility(View.VISIBLE);
            contwrap.setVisibility(View.GONE);

            popup.show();

            MailEntry selected = adapter.getItem(position);
            ScrapeMailContentTask task = new ScrapeMailContentTask(flow, contwrap, load);
            task.execute(selected);
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

        //System.out.println("Refreshing LoopMail");
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.refresh));

        Runnable firstJob = new Runnable() {
            @Override
            public void run() {
                try {
                    API.get().refreshLoopMail();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Utils.safelyDismissDialog(progressDialog);
                }

                ScrapeLoopMailTask task = new ScrapeLoopMailTask();
                task.execute(LoopMailBoxType.INBOX);
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

                //System.out.println("Finished refreshing LoopMail");
                postRefresh();
            }
        };

        RefreshTask refreshTask = new RefreshTask(firstJob, secondJob);
        refreshTask.execute();
    }

    public void sort(SortType type) {
        if (adapter != null) {
            adapter.toggleSortOrder();

            Collections.sort(Arrays.asList(adapter.getValues()),
                    adapter.getSortOrder() ? DATE_COMPARATOR : Collections.reverseOrder(DATE_COMPARATOR));
            adapter.notifyDataSetChanged();
        }
    }

    void preRefresh() {
        Utils.lockOrientation(getActivity());
    }

    void postRefresh() {
        Utils.unlockOrientation(getActivity());
    }
}

