package com.melon.myapp.functions.fragment;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.melon.myapp.ApiManager;
import com.melon.myapp.BaseFragment;
import com.melon.myapp.R;
import com.melon.myapp.bean.Note;
import com.melon.myapp.db.DatabaseHelper;
import com.melon.mylibrary.util.CommonUtil;
import com.melon.mylibrary.util.ToastUtil;
import com.melon.mylibrary.util.ViewHolder;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.sql.SQLException;
import java.util.List;

import butterknife.BindView;
import okhttp3.Call;

/**
 * 笔记主页
 */
public class NoteFragment extends BaseFragment implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener, View.OnLongClickListener {

    private static final java.lang.String TAG = "NoteFragment";
    private static final String OK_TAG_NOTE = "note";
    @BindView(R.id.lv_note)
    public ListView lv_note;
    private LayoutInflater mInflater;
    private MyAdapter mAdapter;
    private DatabaseHelper mDatabaseHelper;
    private RuntimeExceptionDao mDao;
    private List<Note> mNotes;
    @BindView(R.id.fab_note_add)
    public View addBtn;
    @BindView(R.id.empty)
    public TextView emptyView;

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        this.mInflater = inflater;
        return inflater.inflate(R.layout.fragment_note, container, false);
    }

    @Override
    protected void initData() {
        mDao = getDBHelper().getNoteDao();
        getMyPhoneNotes();
        mAdapter = new MyAdapter();
        lv_note.setAdapter(mAdapter);

        if(mNotes==null || mNotes.size()==0){
            //TODO 从服务器中获取最新的50条数据
            getMyServerNotes();
        }
    }

    @Override
    protected void initView() {
        lv_note.setOnItemLongClickListener(this);
        lv_note.setOnItemClickListener(this);
        initEmptyView();

        addBtn.setOnLongClickListener(this);
    }


    /**
     * 可改进设计 带上上次请求时间（服务器给）：
     * 如果有时间：在服务器上取这时间之后的最多50条数据并且时间倒序（或全取）
     * 如果无时间：取最近50条数据。
     */
    private void getMyServerNotes() {
        OkHttpUtils
                .post()
                .tag(OK_TAG_NOTE)
                .url(ApiManager.API_NOTE_ALL)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        ToastUtil.toast(getContext(), e.getMessage());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try{
                            List<Note> serverNotes = new Gson().fromJson(response, new TypeToken<List<Note>>(){}.getType());
                            if(serverNotes!=null && serverNotes.size()!=0){
                                //TODO 获取本地并显示
                                mNotes.addAll(serverNotes);
                                mAdapter.notifyDataSetChanged();

                                //TODO 存储在本地db
                                mDao.create(serverNotes);
                            }
                        }catch (Exception e){e.printStackTrace();}
                    }
                });
    }

    private void initEmptyView() {
        lv_note.setEmptyView(emptyView);
    }

    //本地db记录
    private void getMyPhoneNotes() {
        try {
            mNotes = mDao.queryBuilder().orderBy("time", false).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_note_add:
                //添加笔记
                addNote();
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void addNote() {
        final EditText et = new EditText(getContext());
        et.setHint("输入内容");
        et.setMinimumHeight(200);
        new AlertDialog.Builder(getActivity())
                .setView(et)
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input = et.getText().toString().trim();
                        if (CommonUtil.isEmpty(input)) {
                            ToastUtil.toast(getContext(), "内容不能为空");
                        } else {
                            //显示在当前列表
                            Note note = new Note(System.currentTimeMillis()/1000 + "", input);
                            mNotes.add(0, note);
                            mAdapter.notifyDataSetChanged();
                            // 记录到数据库
                            mDao.create(note);

                            //上传至服务器
                            uploadNote(note);
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                CommonUtil.hideInputMode(getActivity(), true);
                            }
                        }, 300);

                    }
                })
                .show();
        CommonUtil.hideInputMode(getActivity(), false);

        //对话框点击确定，不自动消失
//        final AlertDialog mDialog=new AlertDialog.Builder(this).setPositiveButton("确定", null).setNegativeButton("取消", null).create();
//        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(DialogInterface dialog) {
//                Button positionButton=mDialog.getButton(AlertDialog.BUTTON_POSITIVE);
//                Button negativeButton=mDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
//                positionButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Toast.makeText(MainActivity.this,"确定",Toast.LENGTH_SHORT).show();
//                        mDialog.dismiss();
//                    }
//                });
//                negativeButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Toast.makeText(MainActivity.this,"取消",Toast.LENGTH_SHORT).show();
//
//                    }
//                });
//            }
//        });
//        mDialog.show();
    }

    private void uploadNote(final Note note) {
        OkHttpUtils
                .post()
                .url(ApiManager.API_NOTE_ADD)
                .addParams("content", note.content)
                .addParams("user", Build.MODEL)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        ToastUtil.toast(getContext(), e.getMessage());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        //TODO 更新本地note信息 sid
                        note.sid = response;
                        int update = mDao.update(note);
                        ToastUtil.toast(getContext(), "上传成功: " + response);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mDatabaseHelper != null) {
            OpenHelperManager.releaseHelper();
            mDatabaseHelper = null;
        }

        OkHttpUtils.getInstance().cancelTag(OK_TAG_NOTE);
    }

    private DatabaseHelper getDBHelper() {
        if (mDatabaseHelper == null) {
            mDatabaseHelper = OpenHelperManager.getHelper(getContext(), DatabaseHelper.class);
        }
        return mDatabaseHelper;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        new AlertDialog.Builder(getActivity())
                .setMessage("要删除吗？")
                .setCancelable(true)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //显示在当前列表
                        Note note = mNotes.get(position);
                        mNotes.remove(note);
                        mAdapter.notifyDataSetChanged();
                        // 记录到数据库
                        mDao.delete(note);

                        //上传至服务器
                        delNote(note);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
        return true;
    }

    private void delNote(Note note) {
        if (CommonUtil.isEmpty(note.sid)) {
            ToastUtil.toast(getContext(), "服务器中不存在此信息");
            return;
        }

        OkHttpUtils
                .post()
                .url(ApiManager.API_NOTE_DEL)
                .addParams("sid", note.sid + "")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        ToastUtil.toast(getContext(), e.getMessage());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        ToastUtil.toast(getContext(), response);
                    }
                });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        final Note note = mNotes.get(position);

        final EditText et = new EditText(getContext());
        et.setText(note.content);
        et.setMinimumHeight(200);
        new AlertDialog.Builder(getActivity())
                .setView(et)
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input = et.getText().toString().trim();
                        if (CommonUtil.isEmpty(input)) {
                            ToastUtil.toast(getContext(), "内容不能为空");
                        } else {
                            //显示在当前列表
                            note.content = input;
                            note.time = System.currentTimeMillis()/1000 + "";
                            mNotes.remove(note);
                            mNotes.add(0, note);
                            mAdapter.notifyDataSetChanged();
                            // 记录到数据库
                            mDao.update(note);

                            //上传至服务器
                            updateNote(note);
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                CommonUtil.hideInputMode(getActivity(), true);
                            }
                        }, 300);

                    }
                })
                .show();
        CommonUtil.hideInputMode(getActivity(), false);
    }

    private void updateNote(Note note) {
        if (CommonUtil.isEmpty(note.sid)) {
            ToastUtil.toast(getContext(), "服务器中不存在此信息");
            return;
        }

        OkHttpUtils
                .post()
                .url(ApiManager.API_NOTE_UPDATE)
                .addParams("sid", note.sid + "")
                .addParams("content", note.content)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        ToastUtil.toast(getContext(), e.getMessage());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        ToastUtil.toast(getContext(), response);
                    }
                });
    }

    @Override
    public boolean onLongClick(View v) {
        //长按添加按钮，查询服务器数据
        ToastUtil.toast(getContext(), "查询服务器");
        OkHttpUtils
                .post()
                .url(ApiManager.API_NOTE_QUERY)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        ToastUtil.toast(getContext(), e.getMessage());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        ToastUtil.toast(getContext(), "查询成功,  共：" + response + "条数据");
                    }
                });
        return true;
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mNotes == null ? 0 : mNotes.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_note, parent, false);
            }

            TextView tv_note_time = ViewHolder.get(convertView, R.id.tv_note_time);
            TextView tv_note_content = ViewHolder.get(convertView, R.id.tv_note_content);
            tv_note_content.setText(mNotes.get(position).content);
            tv_note_time.setText(CommonUtil.getMyDateFormat(mNotes.get(position).time));
            return convertView;
        }
    }
}
