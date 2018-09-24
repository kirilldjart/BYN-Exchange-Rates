package by.kirilldrob.bynexchangerates;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;


import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import by.kirilldrob.bynexchangerates.data.Currency;
import by.kirilldrob.bynexchangerates.data.Util;
import by.kirilldrob.bynexchangerates.network.XMLLoader;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<Currency>>, SwipeRefreshLayout.OnRefreshListener {
    Bundle bndl;

    static final int LOADER_ID = 1;
    public MyRecyclerAdapter myRecyclerAdapter;
    final String LOG_TAG = "dks";
    final String URL_ADDRESS = "http://www.nbrb.by/Services/XmlExRates.aspx";
    SwipeRefreshLayout mSwipeRefreshLayout;
    boolean bNeedSave = false; // Config saveness

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //-----------
        setRecyclerView();
        //------------------
        bndl = new Bundle();
        bndl.putString("fileName", URL_ADDRESS);
        //!!!WorkManager doWork() or LiveData<List<Currency>>(ViewModel) or RxJava is better.
        LoaderManager.getInstance(this).initLoader(LOADER_ID, bndl, this);

        Map map = Util.loadMap(this);
        if (map != null) {
            XMLLoader.mConfigPosition = new HashMap<>(map);
            Log.d(LOG_TAG, XMLLoader.mConfigPosition.toString());
        }

    }


    @Override
    public Loader<ArrayList<Currency>> onCreateLoader(int id, Bundle args) {
        Loader<ArrayList<Currency>> loader = null;
        if (id == LOADER_ID) { // не обязательная проверка, оставлена на будущее
            loader = new XMLLoader(this, args);
            Log.d(LOG_TAG, "onCreateLoader: " + loader.hashCode());
            /**
             * Showing Swipe Refresh animation on activity create
             * As animation won't start on onCreate, post runnable is used
             */
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<Currency>> loader, ArrayList<Currency> data) {
        Log.d(LOG_TAG, "onLoadFinished for loader " + loader.hashCode());
        if (data == null) {
            TextView tv = findViewById(R.id.tvError);
                    tv.setVisibility(View.VISIBLE);
                    tv.setText(((XMLLoader) loader).mLastErrorCode);
            findViewById(R.id.list_view).setVisibility(View.GONE);

            return;
        } else {
            findViewById(R.id.tvError).setVisibility(View.GONE);
            findViewById(R.id.list_view).setVisibility(View.VISIBLE);
        }

        mSwipeRefreshLayout.setRefreshing(false);
        myRecyclerAdapter.setData(data);
    }


    @Override
    public void onLoaderReset(Loader<ArrayList<Currency>> loader) {

    }

    @Override
    protected void onStop() {
        if (bNeedSave) Util.saveMap(XMLLoader.mConfigPosition, this);
        super.onStop();
    }

    private void setRecyclerView() {
        RecyclerView listView = findViewById(R.id.list_view);
        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.setHasFixedSize(true); // количество валют постоянно

        myRecyclerAdapter = new MyRecyclerAdapter(this);

        listView.setAdapter(myRecyclerAdapter);
        ItemTouchHelper mIth = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        0) {
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        final int fromPosition = viewHolder.getAdapterPosition();
                        final int toPosition = target.getAdapterPosition();

//                       !! LinkedList!! with newNode.next= prevNode лучше, но данная операция будет
//                                выполняться не часто по сравнению с пересозданием листа
//                        // move item in `fromPos` to `toPos` in adapter.
                        //                              2 way:
                        //                        ArrayList list=myRecyclerAdapter.mData;
                        //                       list.add(toPosition-1,list.remove(fromPosition));
                        //                       list.add(toPosition-1,list.remove(toPosition+1));

                        if (fromPosition < toPosition) {
                            for (int i = fromPosition; i < toPosition; i++) {
                                Collections.swap(myRecyclerAdapter.mData, i, i + 1);
                            }
                        } else {
                            for (int i = fromPosition; i > toPosition; i--) {
                                Collections.swap(myRecyclerAdapter.mData, i, i - 1);
                            }
                        }  // Можно также newNode.next= prevNode  для LinkedList


                        //---------------Сохраняем конфигурацию на будущее:
                        for (int counter = 0; counter < myRecyclerAdapter.mData.size(); counter++) {
                            XMLLoader.mConfigPosition.put(myRecyclerAdapter.mData.get(counter).charCode, counter);
                        }
                        bNeedSave = true;
                        //---------------------for recreating of screen
                        Loader<ArrayList<Currency>> loader = LoaderManager.getInstance(MainActivity.this).getLoader(LOADER_ID);
                        ((XMLLoader) loader).mCurrencyList = myRecyclerAdapter.mData;
                        //-----------------------------------------------------
                        myRecyclerAdapter.notifyItemMoved(fromPosition, toPosition);
                        return true;// true if moved, false otherwise
                    }

                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        // remove from adapter
                    }
                });
        mIth.attachToRecyclerView(listView);
        //--------------------------------------------------end DRag&Drop

        // SwipeRefreshLayout   - для возможности обновления списка
        mSwipeRefreshLayout = findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);


    }

    //----------------- Pull to refresh------
    @Override
    public void onRefresh() {
        if (bndl == null) return;
        LoaderManager.getInstance(this).restartLoader(LOADER_ID, bndl, this);
    }


}


//        RecyclerItemClickSupport.addTo(listView).setOnItemClickListener(new RecyclerItemClickSupport.OnItemClickListener() {
//            @Override
//            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
//                // do something
//            }
//        });