package test.hjz.zhuo;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import test.hjz.zhuo.utils.HtmlCache;
import test.hjz.zhuo.utils.UrlLoader;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_DRAGGING;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_SETTLING;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_NAME = "name";
    private static final String KEY_URL = "url";
    private static final int PRELOAD_PREPARE = 1;
    private static final int PRELOAD_READY = 2;
    private static final int PRELOAD_STARTED = 3;
    private static final int PRELOADED = 0;
    private static final String KEY_PRELOAD_STATUS = "preload_status";

    private static final String TAG = "MainActivity";

    private RecyclerView mListView;
    private LinearLayoutManager mLayoutManager;
    private ListAdapter mAdapter;
    private Handler mPreLoadHandler;
    private Handler mUIHandler;
    private HandlerThread mWorkThread;
    private Handler mWorkHandler;

    private ComponentName mWebActivity;

    // when activity recreate,static data can be reserve.
    private static List<Map<String, Object>> datas;

    private static final String[][] TEST_URL = {
            {"baidu", "www.baidu.com"},
            {"360", "www.so.com"},
            {"bing", "www.bing.com"},
            {"sogou", "www.sogou.com"},
            {"sogou", "www.sogou.com"},
            {"sogou", "www.sogou.com"},
            {"sogou", "www.sogou.com"},
            {"sogou", "www.sogou.com"},
            {"sogou", "www.sogou.com"},
            {"sogou", "www.sogou.com"}
    };

    private OnScrollListener mScrollListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebActivity = new ComponentName(WebActivity.class.getPackage().getName(), WebActivity.class.getCanonicalName());

        mPreLoadHandler = new Handler();
        mUIHandler = new UIHandler();

        // background priority.
        mWorkThread = new HandlerThread("UrlLoaderThread", Process.THREAD_PRIORITY_BACKGROUND);
        mWorkThread.start();

        // work looper may be null.
        mWorkHandler = new WorkHandler(mWorkThread.getLooper());

        mListView = findViewById(R.id.list);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new ListAdapter();
        mScrollListener = new ListOnScrollListener();

        mListView.setLayoutManager(mLayoutManager);
        mListView.setAdapter(mAdapter);
        mListView.addOnScrollListener(mScrollListener);

        //mLayoutManager.findFirstVisibleItemPosition();
        //mLayoutManager.findViewByPosition(0);
        //mLayoutManager.getChildAt(0);
    }

    /**
     * create sample data set
     */
    private List<Map<String, Object>> createList() {

        List<Map<String, Object>> ret = new ArrayList<>();
        for (String[] item : TEST_URL) {
            Map map = new HashMap<>();
            map.put(KEY_NAME, item[0]);
            map.put(KEY_URL, item[1]);
            map.put(KEY_PRELOAD_STATUS, PRELOAD_PREPARE);
            ret.add(map);
        }

        return ret;
    }


    class ListAdapter extends RecyclerView.Adapter<Holder> {

        public ListAdapter() {
            if (datas == null){
                datas = createList();
            }
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // viewType no use at here
            return new Holder(parent, R.layout.list_item);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            Log.d(TAG, "position:" + position + " ,holder position:" + holder.getLayoutPosition());
            holder.bindData(datas.get(position));
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }
    }

    /**
     * holder maintain item state.
     */
    class Holder<T extends Map<String, Object>> extends RecyclerView.ViewHolder {

        private Holder mHolder;
        private ItemOnAttachStateChangeListener mItemOnAttachStateChangeListener;

        public Holder(ViewGroup parent, int viewId) {
            super(LayoutInflater.from(parent.getContext()).inflate(viewId, parent, false));
            mHolder = this;
            mItemOnAttachStateChangeListener = new ItemOnAttachStateChangeListener();
            itemView.addOnAttachStateChangeListener(mItemOnAttachStateChangeListener);
        }

        public void bindData(T data) {
            String name = (String) data.get(KEY_NAME);
            TextView item = itemView.findViewById(R.id.item);
            item.setText(name);
            // data
            item.setTag(data);

            // set item status
            int status = (int) data.get(KEY_PRELOAD_STATUS);
            if (PRELOAD_STARTED == status) {
                item.setBackgroundColor(Color.GRAY);
            } else if (PRELOADED == status) {
                item.setBackgroundColor(Color.GREEN);
            } else {
                item.setBackgroundColor(Color.WHITE);
            }

            //item click
            item.setOnClickListener(view -> {
                //todo on item click
                Toast.makeText(view.getContext(), (String) data.get(KEY_URL), Toast.LENGTH_SHORT).show();
                //
                Intent intent = new Intent();
                intent.putExtra(KEY_URL, (String) data.get(KEY_URL));
                intent.setComponent(mWebActivity);
                startActivity(intent);
            });

        }

        class ItemOnAttachStateChangeListener implements View.OnAttachStateChangeListener {
            @Override
            public void onViewAttachedToWindow(View v) {
                //preloadReady flag to true
                int position = mHolder.getAdapterPosition();
                if (PRELOAD_PREPARE == (int) datas.get(position ).get(KEY_PRELOAD_STATUS)) {
                    datas.get(position ).put(KEY_PRELOAD_STATUS, PRELOAD_READY);
                }
                Log.d(TAG, "onViewAttachedToWindow:" + mHolder.getAdapterPosition());
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                //preloadReady to false
                int position = mHolder.getAdapterPosition();
                if (position == -1) {
                    return;
                }
                if (PRELOAD_READY == (int) datas.get(position ).get(KEY_PRELOAD_STATUS)) {
                    datas.get(position ).put(KEY_PRELOAD_STATUS, PRELOAD_PREPARE);
                }
                Log.d(TAG, "onViewDetachedFromWindow:" + mHolder.getAdapterPosition());
            }
        }
    }

    class ListOnScrollListener extends OnScrollListener {
        private int lastState = SCROLL_STATE_IDLE;
        private PreLoadReadyTask preLoadTask;

        public ListOnScrollListener() {
            super();
            preLoadTask = new PreLoadReadyTask();
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            // stop scroll
            // can check position preload here,but there is no position message.
            if (newState == SCROLL_STATE_IDLE) {
                Log.d(TAG, "SCROLL_STATE_IDLE postAtTime 3s");
                mPreLoadHandler.postAtTime(preLoadTask, SystemClock.uptimeMillis() + 3000);

            } else if (newState == SCROLL_STATE_DRAGGING) {
                Log.d(TAG, "SCROLL_STATE_DRAGGING cancel postAtTime 3s");
                mPreLoadHandler.removeCallbacks(preLoadTask);
            }
        }
    }

    class PreLoadReadyTask implements Runnable {
        @Override
        public void run() {
            //
            int pos_top = mLayoutManager.findFirstVisibleItemPosition();
            int pos_bottom = mLayoutManager.findLastVisibleItemPosition();

            for (int i = pos_top; i <= pos_bottom; i++) {
                int status = (int) datas.get(i).get(KEY_PRELOAD_STATUS);
                if (status == PRELOAD_STARTED || status == PRELOADED) {
                    continue;
                }
                datas.get(i).put(KEY_PRELOAD_STATUS, PRELOAD_STARTED);


                Message msg = Message.obtain();
                // position = arg1
                msg.arg1 = i;

                //todo start preload url
                // start preload url
                // mPreLoadHandler is attach on UI Thread.
                // pass url and position
                mWorkHandler.post(new PreLoadTask((String) datas.get(i).get(KEY_URL),i));

                mUIHandler.sendMessage(msg);
            }

            //mAdapter.notifyDataSetChanged();
            Log.d(TAG, "notify SCROLL_STATE_IDLE 3s,load url!");
        }
    }

    class PreLoadTask implements Runnable {
        private String url;
        private int pos;
        public PreLoadTask(String url,int pos){
            this.url = url;
            this.pos = pos;
        }
        @Override
        public void run() {
            if (HtmlCache.get(url) == null) {
                String ret = UrlLoader.load(url);
                if (ret != null) {
                    HtmlCache.put(url, ret);
                }
            }
            //todo url load complete.

            // update preload complete.
            datas.get(pos).put(KEY_PRELOAD_STATUS, PRELOADED);
            Message msg = Message.obtain();
            msg.arg1 = pos;

            // update UI
            mUIHandler.sendMessage(msg);
        }
    }

    class UIHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // there is in UI Thread.
            // update the item at pos.
            mAdapter.notifyItemChanged(msg.arg1);
        }
    }

    class WorkHandler extends Handler {

        // init work looper.
        public WorkHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // there is in Work Thread.
            // can cancel task.

        }
    }
}
