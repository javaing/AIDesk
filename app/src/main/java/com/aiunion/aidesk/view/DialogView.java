package com.aiunion.aidesk.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.aiunion.aidesk.R;
import com.aiunion.aidesk.model.ThreadModel;
import com.aiunion.aidesk.model.entity.DeviceParameters;
import com.aiunion.aidesk.viewmodel.IpconfParams;
import com.aiunion.aidesk.viewmodel.MainViewModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class DialogView extends Dialog  implements OnClickListener{
    private String TAG = this.getClass().getSimpleName();
    private LinearLayout mServerLinerLayout;
 
    public DeviceParameters params;
    private View mView;
    private MainViewModel mViewModel;

    public DialogView(Context context, View view) {
        super(context);
        mView=view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mView);

        mServerLinerLayout = (LinearLayout) findViewById(R.id.ll_dia);
        params = DeviceParameters.getInstance(this.getContext());
        //findViewById(R.id.test_btn).setOnClickListener(this);
        initViewItem();

//        addViewItem(null);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.moreOption_btn://點選新增按鈕就動態新增Item
                addViewItem(v);
                break;

        }
    }
    /**初始化(當dialog打開時要做的事情)*/
    private void initViewItem(){
        //把json轉成list格式
        Gson gson = new Gson();
        Type listType = new TypeToken<List<IpconfParams>>(){}.getType();
        List<IpconfParams> myList = gson.fromJson(params.getServerIp(), listType);
        for(IpconfParams i : myList) {
            System.out.println("mylist"+i.getServerIP().toString());
            System.out.println("mylist"+i.getDeviceId());

        }
        ThreadModel.getInstance().setmIplist(myList);
        /**對於在list內的每個元素寫入dialognewItem*/
        for(IpconfParams i : myList){
            View hotelEvaluateView = View.inflate(this.getContext(), R.layout.recyclerview_newdialog_item, null);
            EditText serverName = (EditText)  hotelEvaluateView.findViewById(R.id.ipItem);
            EditText deviceid   = hotelEvaluateView.findViewById(R.id.deviceid_ed);
            EditText channelid =hotelEvaluateView.findViewById(R.id.channel_ed);
            EditText threadhold= hotelEvaluateView.findViewById(R.id.Threadhold_ed);
            Button btn_remove = (Button) hotelEvaluateView.findViewById(R.id.moreOption_btn);
            serverName.setText(i.getServerIP());
            deviceid.setText(String.valueOf(i.getDeviceId()));
            threadhold.setText(String.valueOf(i.getThreshoId()));
            channelid.setText(i.getChannelID());
            btn_remove.setText("刪除");
            btn_remove.setTag("remove");//設定刪除標記
            mServerLinerLayout.addView(hotelEvaluateView);

        }
            View hotelEvaluateView = View.inflate(this.getContext(), R.layout.recyclerview_newdialog_item, null);
            Button btn_add = (Button) hotelEvaluateView.findViewById(R.id.moreOption_btn);
            btn_add.setText("+新增");
            btn_add.setTag("add");
            btn_add.setOnClickListener(this);
            mServerLinerLayout.addView(hotelEvaluateView);
            sortHotelViewItem();
    }
    //新增新的View
    private void addViewItem(View view) {
        if (mServerLinerLayout.getChildCount() == 0) {//如果一個都沒有，就新增一個
            View hotelEvaluateView = View.inflate(this.getContext(), R.layout.recyclerview_newdialog_item, null);
            Button btn_add = (Button) hotelEvaluateView.findViewById(R.id.moreOption_btn);
            btn_add.setText("+新增");
            btn_add.setTag("add");
            btn_add.setOnClickListener(this);
            mServerLinerLayout.addView(hotelEvaluateView);
            //sortHotelViewItem();
        } else if (((String) view.getTag()).equals("add")) {//如果有一個以上的Item,點選為新增的Item則新增
            View hotelEvaluateView = View.inflate(this.getContext(), R.layout.recyclerview_newdialog_item, null);
            mServerLinerLayout.addView(hotelEvaluateView);
            sortHotelViewItem();
        }
        //else {
        // sortHotelViewItem();
        //}
    }

    /**重新刷新排列並針對最後一個元素進行判斷*/
    private void sortHotelViewItem() {
        //獲取LinearLayout裡面所有的view
        for (int i = 0; i < mServerLinerLayout.getChildCount(); i++) {
            final View childAt = mServerLinerLayout.getChildAt(i);
            final Button btn_remove = (Button) childAt.findViewById(R.id.moreOption_btn);
            btn_remove.setText("刪除");
            btn_remove.setTag("remove");//設定刪除標記
            btn_remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //從LinearLayout容器中刪除當前點選到的ViewItem
                    mServerLinerLayout.removeView(childAt);
                }
            });
            //如果是最後一個ViewItem，就設定為新增
            if (i == (mServerLinerLayout.getChildCount() - 1)) {
                Button btn_add = (Button) childAt.findViewById(R.id.moreOption_btn);
                btn_add.setText("+新增");
                btn_add.setTag("add");
                btn_add.setOnClickListener(this);
            }
        }
    }





}