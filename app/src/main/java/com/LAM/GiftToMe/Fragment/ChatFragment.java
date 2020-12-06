package com.LAM.GiftToMe.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.LAM.GiftToMe.Adapter.ChatListAdapter;
import com.LAM.GiftToMe.R;
import com.LAM.GiftToMe.UsefulClass.MyGift;
import com.LAM.GiftToMe.UsefulClass.UsersGift;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ChatFragment extends Fragment {

    private Context mContext;
    private ArrayList<UsersGift> userGifts;
    private ChatListAdapter chatListAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.chat_fragment, container, false);

        mContext = getActivity().getApplicationContext();

        return v;
    }
}
