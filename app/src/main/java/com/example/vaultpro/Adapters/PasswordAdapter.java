package com.example.vaultpro.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.example.vaultpro.R;
import com.example.vaultpro.objects.Password;

import java.util.ArrayList;
import java.util.List;

public class PasswordAdapter extends RecyclerView.Adapter<PasswordAdapter.ViewHolder> implements Filterable {

    private List<Password> Passwordlist;
    private List<Password> temp;
    private Context context;

    public PasswordAdapter( List<Password> bList)
    {
        this.Passwordlist=bList;
        this.temp=bList;
    }

    @Override
    public int getItemCount() {
        return Passwordlist.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.pass_card,parent,false);
        context = parent.getContext();
        view.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                // item clicked
            }
        });
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.title.setText(Passwordlist.get(position).getTitle());
        holder.username.setText(Passwordlist.get(position).getUsername());
        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color2 = generator.getColor(Passwordlist.get(position).getTitle());
        TextDrawable.IBuilder builder = TextDrawable.builder()
                .beginConfig()
                .withBorder(4)
                .endConfig()
                .rect();
        char c =Passwordlist.get(position).getTitle().charAt(0);
        TextDrawable ic2 = builder.build(String.valueOf(c), color2);
        holder.image.setImageDrawable(ic2);
    }

    public class  ViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public TextView title, username;
        ImageView image;

        ViewHolder(View itemView){
            super(itemView);
            mView=itemView;
            title = mView.findViewById(R.id.title);
            username = mView.findViewById(R.id.username);
            image = mView.findViewById(R.id.imageView);
    }

    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint.toString();
                List<Password> filtered = new ArrayList<>();


                if(query.isEmpty()){
                    filtered = Passwordlist;
                } else
                {
                    for(Password search : Passwordlist)
                    {
                        if((search.getTitle().toLowerCase().contains(query.toLowerCase())) ||
                                (search.getWebsite().toLowerCase().contains(query.toLowerCase())))
                        {
                            filtered.add(search);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.count = filtered.size();
                results.values  = filtered;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {


                temp = (ArrayList<Password>) results.values;
                notifyDataSetChanged();


            }
        };
    }

}
