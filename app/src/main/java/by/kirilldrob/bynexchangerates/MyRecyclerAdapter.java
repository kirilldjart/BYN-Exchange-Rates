package by.kirilldrob.bynexchangerates;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.ArrayList;

;
import androidx.recyclerview.widget.RecyclerView;
import by.kirilldrob.bynexchangerates.data.Currency;


public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.RecyclerViewHolder> {
    //public  LinkedList<Currency> mData = new LinkedList<>(); все же не тот случай
    public ArrayList<Currency> mData = new ArrayList<>();

    private Context context;

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false));
    }

    public MyRecyclerAdapter(Context context) {
        this.context = context;

    }

    @Override // установка новых данных
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        Currency newsItem = mData.get(position);
        if (newsItem == null) return;
        holder.charTextView.setText(newsItem.charCode);
        holder.nameTextView.setText(newsItem.name);
        holder.rateTextView.setText(newsItem.rate + " BYN"); //
        holder.scaleTextView.setText(context.getResources().getString(R.string.particleON) + " " + newsItem.scale + " " + context.getResources().getString(R.string.unit)); //
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(ArrayList<Currency> data) {
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }


// class Holder allways static

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        final TextView charTextView;
        final TextView nameTextView;
        final TextView rateTextView;
        final TextView scaleTextView;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            charTextView =  itemView.findViewById(R.id.tvChar);
            nameTextView = itemView.findViewById(R.id.tvName);
            rateTextView = itemView.findViewById(R.id.tvRate);
            scaleTextView =  itemView.findViewById(R.id.tvScale);
        }
    }

}