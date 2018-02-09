package es.elhaso.gradha.churfthewave.ui;

import android.graphics.Bitmap;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import es.elhaso.gradha.churfthewave.R;
import es.elhaso.gradha.churfthewave.logic.UserModel;
import es.elhaso.gradha.churfthewave.misc.ThreadUtils;
import es.elhaso.gradha.churfthewave.network.Net;

public class UsersListRecyclerViewAdapter
    extends RecyclerView.Adapter<UsersListRecyclerViewAdapter
    .RecyclerViewHolder>
{
    private List<UserModel> mItems = new ArrayList<>(20);

    private @NonNull WeakReference<OnUserClickedListener> mListener = new
        WeakReference<>(null);

    public UsersListRecyclerViewAdapter()
    {
        setHasStableIds(true);
    }

    @AnyThread public void setListener(@Nullable OnUserClickedListener listener)
    {
        mListener = new WeakReference<>(listener);
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

    final class RecyclerViewHolder
        extends RecyclerView.ViewHolder
        implements View.OnClickListener, Net.OnBitmapLoadedCallback
    {
        private @Nullable UserModel mUser = null;

        private ImageView mAvatarImage;
        private TextView mFirstNameText;
        private TextView mLastNameText;
        private TextView mGenderText;

        RecyclerViewHolder(View root)
        {
            super(root);

            mAvatarImage = root.findViewById(R.id.avatar_view);
            mFirstNameText = root.findViewById(R.id.first_name_text);
            mLastNameText = root.findViewById(R.id.last_name_text);
            mGenderText = root.findViewById(R.id.gender_text);
            root.setOnClickListener(this);
        }

        @UiThread void bind(UserModel user)
        {
            mUser = user;
            mFirstNameText.setText(mUser.firstName);
            mLastNameText.setText(mUser.lastName);
            mGenderText.setText(mUser.gender);
            mAvatarImage.setImageBitmap(null);
            if (null != mUser.smallAvatarUrl) {
                Net.getBitmap(mUser.smallAvatarUrl, this);
            }
        }

        @Override public void onClick(View view)
        {
            final OnUserClickedListener listener = mListener.get();
            if (null != listener && null != mUser) {
                listener.onUserClicked(mUser);
            }
        }

        @Override public void onBitmapLoaded(final @NonNull URL url,
            final @Nullable Bitmap bitmap)
        {
            ThreadUtils.runOnUi(new Runnable()
            {
                @Override public void run()
                {
                    if (null != mUser && url.equals(mUser.smallAvatarUrl)) {
                        mAvatarImage.setImageBitmap(bitmap);
                    }
                }
            });
        }
    }

    interface OnUserClickedListener
    {
        void onUserClicked(@NonNull UserModel user);
    }
}
