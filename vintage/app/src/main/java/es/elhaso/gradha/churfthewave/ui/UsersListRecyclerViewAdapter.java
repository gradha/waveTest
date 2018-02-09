package es.elhaso.gradha.churfthewave.ui;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import es.elhaso.gradha.churfthewave.R;
import es.elhaso.gradha.churfthewave.logic.UserModel;
import es.elhaso.gradha.churfthewave.misc.ThreadUtils;

public class UsersListRecyclerViewAdapter
    extends RecyclerView.Adapter<UsersListRecyclerViewAdapter
    .RecyclerViewHolder>
{
    private List<UserModel> mItems = new ArrayList<>(20);

    public UsersListRecyclerViewAdapter()
    {
        setHasStableIds(true);
    }

    @UiThread public void load(@NonNull List<UserModel> items)
    {
        ThreadUtils.BLOCK_UI();

        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    @Override public RecyclerViewHolder onCreateViewHolder(ViewGroup parent,
        int viewType)
    {
        return new RecyclerViewHolder(LayoutInflater.from(parent.getContext()
        ).inflate(R.layout.users_list_item, parent, false));
    }

    @Override public void onBindViewHolder(RecyclerViewHolder holder,
        int position)
    {
        ThreadUtils.BLOCK_UI();
        holder.bind(mItems.get(position));
    }

    @Override public int getItemCount()
    {
        return mItems.size();
    }

    @Override public long getItemId(int position)
    {
        return mItems.get(position).id;
    }

    static final class RecyclerViewHolder
        extends RecyclerView.ViewHolder
    {
        private @Nullable UserModel mUser = null;

        private TextView mFirstNameText;
        private TextView mLastNameText;
        private TextView mGenderText;

        public RecyclerViewHolder(View root)
        {
            super(root);

            mFirstNameText = root.findViewById(R.id.first_name_text);
            mLastNameText = root.findViewById(R.id.last_name_text);
            mGenderText = root.findViewById(R.id.gender_text);
        }

        @UiThread public void bind(UserModel user)
        {
            mUser = user;
            mFirstNameText.setText(mUser.firstName);
            mLastNameText.setText(mUser.lastName);
            mGenderText.setText(mUser.gender);
        }
    }
}
