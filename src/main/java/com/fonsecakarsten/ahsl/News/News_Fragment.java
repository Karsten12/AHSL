package com.fonsecakarsten.ahsl.News;

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
import android.view.Menu;
import android.view.MenuInflater;
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

@SuppressWarnings("ALL")
public class News_Fragment extends ListFragment {
    private NewsAdapter adapter;

    private static final Comparator<NewsArticle> DATE_COMPARATOR = new Comparator<NewsArticle>() {
        @Override
        public int compare(NewsArticle lhs, NewsArticle rhs) {
            Date d1 = null;
            Date d2 = null;

            try {
                d1 = Constants.LOOPED_DATE_FORMAT.parse(lhs.getDatePosted());
                d2 = Constants.LOOPED_DATE_FORMAT.parse(rhs.getDatePosted());
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

        ScrapeNewsTask task = new ScrapeNewsTask();
        task.execute();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.google_card, container, false);
        setHasOptionsMenu(true);

        return rootView;
    }

    private class ScrapeNewsTask extends AsyncTask<String, Void, List<NewsArticle>> {
        @Override
        protected List<NewsArticle> doInBackground(String... params) {
            try {
                return API.get().getNews();
            } catch (Exception e) {
                RemoteDebug.debugException(e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<NewsArticle> result) {
            super.onPostExecute(result);

            NewsArticle[] values = result.toArray(new NewsArticle[result.size()]);
            if (values.length > 0) {

                CardView spaceshipImage = (CardView) getView().findViewById(R.id.card_view);
                Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_card);
                spaceshipImage.startAnimation(hyperspaceJumpAnimation);

                ListView listView = (ListView) getView().findViewById(android.R.id.list);

                adapter = new NewsAdapter(getActivity(), values);

                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new NewsItemClickAdapter(adapter, getActivity()));

            } else {
                ListView listView = (ListView) getView().findViewById(android.R.id.list);
                TextView emptyText = Utils.getCenteredTextView(getActivity(), getString(R.string.empty_news));

                Utils.showViewOnTop(listView, emptyText);
            }
        }
    }

    private class NewsAdapter extends ArrayAdapter<NewsArticle> {
        private final Context context;
        private final NewsArticle[] values;
        private boolean sortDir;

        public NewsAdapter(Context context, NewsArticle[] values) {
            super(context, R.layout.news_row, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.news_row, parent, false);
            }

            TextView articleName = (TextView) rowView.findViewById(R.id.news_post_title);
            TextView articleAuthor = (TextView) rowView.findViewById(R.id.news_post_author);
            TextView articleDate = (TextView) rowView.findViewById(R.id.news_post_date);

            NewsArticle article = values[position];

            articleName.setText(article.getArticleName());

            if (article.getDisplayAuthor() == null)
                articleAuthor.setText(Html.fromHtml("<b>" + article.getAuthor() + "</b>" +
                        " - " + article.getAuthorType()));
            else
                articleAuthor.setText(Html.fromHtml("<b>" + article.getDisplayAuthor() + "</b>"));

            articleDate.setText(article.getDatePosted());

            return rowView;
        }

        public NewsArticle[] getValues() {
            return values;
        }

        public boolean getSortOrder() {
            return sortDir;
        }

        public void toggleSortOrder() {
            sortDir = !sortDir;
        }
    }

    private class ScrapeNewsDetailsTask extends AsyncTask<NewsArticle, Void, NewsDetail> {
        private View flow;
        private View contwrap;
        private ProgressBar bar;

        public ScrapeNewsDetailsTask(View flow, View contwrap, ProgressBar bar) {
            this.flow = flow;
            this.bar = bar;
            this.contwrap = contwrap;
        }

        @Override
        protected NewsDetail doInBackground(NewsArticle... params) {
            try {
                return API.get().getNewsDetails(params[0]);
            } catch (Exception e) {
                RemoteDebug.debugException(e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(NewsDetail newsDetail) {
            super.onPostExecute(newsDetail);

            RobotoTextView title = (RobotoTextView) flow.findViewById(R.id.popup_title);
            TextView info = (TextView) flow.findViewById(R.id.newsdet_info);
            WebView content = (WebView) flow.findViewById(R.id.newsdet_content);

            title.setText(newsDetail.getTitle());

            if (newsDetail.getContent().length() != 0)
                content.loadDataWithBaseURL(null, newsDetail.getContent(), "text/html", "UTF-8", null);
            else ((LinearLayout) content.getParent()).removeView(content);

            String infoStr = "";
            for (String detail : newsDetail.getDetails()) {
                String parts[] = detail.split(":");
                infoStr += "<b>" + parts[0] + "</b>: " + parts[1] + "<br />";
            }

            info.setText(Html.fromHtml(infoStr));

            content.setFocusable(true);
            content.setFocusableInTouchMode(true);

            bar.setVisibility(View.GONE);

            contwrap.setVisibility(View.VISIBLE);
        }
    }

    private class NewsItemClickAdapter implements AdapterView.OnItemClickListener {
        NewsAdapter adapter;
        Activity parent;

        public NewsItemClickAdapter(NewsAdapter adapter, Activity parent) {
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
            LinearLayout flow = (LinearLayout) inflater.inflate(R.layout.news_details_popup, null, false);
            LinearLayout contwrap = (LinearLayout) flow.findViewById(R.id.newsdet_contwrap);
            ProgressBar load = (ProgressBar) flow.findViewById(R.id.popup_prog);

            Dialog popup = Utils.createLoopedDialog(parent, flow, width);

            load.setVisibility(View.VISIBLE);
            contwrap.setVisibility(View.GONE);

            popup.show();

            NewsArticle selected = adapter.getItem(position);
            ScrapeNewsDetailsTask task = new ScrapeNewsDetailsTask(flow, contwrap, load);
            task.execute(selected);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        refresh();
        return true;
    }

    void refresh() {
        if (Utils.isNetworkOffline(getActivity())) return;

        preRefresh();

        //System.out.println("Refreshing News");
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

                ScrapeNewsTask task = new ScrapeNewsTask();
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

                //System.out.println("Finished refreshing News");
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
