package test.hjz.zhuo;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity{

    private static final String KEY_NAME = "name";
    private static final String KEY_URL = "url";
    private static final String KEY_PRELOADED = "preloaded";

    private RecyclerView mListView;

    private static final String[][] TEST_URL = {
            {"baidu","www.baidu.com"},
            {"360","www.so.com"},
            {"bing","www.bing.com"},
            {"sogou","www.sogou.com"}
    };

    private OnScrollListener mScrollListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = findViewById(R.id.list);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        ListAdapter adapter = new ListAdapter();
        mScrollListener = new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //
                Handler handler = new Handler();
                //handler.po
                switch (newState) {


                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        };

        mListView.setLayoutManager(layoutManager);
        mListView.setAdapter(adapter);
        mListView.addOnScrollListener(mScrollListener);
    }

    private List<Map<String,Object>> createList(){

        List<Map<String, Object>> ret = new ArrayList<>();
        for (String[] item : TEST_URL) {
            Map map = new HashMap<>();
            map.put(KEY_NAME,item[0]);
            map.put(KEY_URL, item[1]);
            map.put(KEY_PRELOADED, false);
            ret.add(map);
        }

        return ret;
    }


    class ListAdapter extends RecyclerView.Adapter<Holder> {

        private List<Map<String,Object>> data;

        public ListAdapter(){
            data = createList();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // viewType no use at here
            return  new Holder(parent,R.layout.list_item);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            holder.bindData(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    class Holder<T extends Map<String,String>> extends RecyclerView.ViewHolder{

        public Holder(ViewGroup parent,int viewId) {
            super(LayoutInflater.from(parent.getContext()).inflate(viewId,parent,false));
        }

        public void bindData(T data){
            String name = data.get(KEY_NAME);
            TextView item = itemView.findViewById(R.id.item);
            item.setText(name);
            item.setTag(data);
            item.setOnClickListener(view ->{
                //
                Toast.makeText(view.getContext(), data.get(KEY_URL), Toast.LENGTH_SHORT).show();
            }) ;


        }
    }


}
