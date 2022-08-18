package com.pengxh.autodingding.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.blankj.utilcode.util.CacheDiskUtils;
import com.blankj.utilcode.util.IntentUtils;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.app.multilib.widget.dialog.InputDialog;
import com.pengxh.autodingding.AndroidxBaseFragment;
import com.pengxh.autodingding.BaseApplication;
import com.pengxh.autodingding.BuildConfig;
import com.pengxh.autodingding.R;
import com.pengxh.autodingding.databinding.FragmentSettingsBinding;
import com.pengxh.autodingding.greendao.HistoryRecordBeanDao;
import com.pengxh.autodingding.service.NotificationMonitorService;
import com.pengxh.autodingding.ui.HistoryRecordActivity;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.Utils;
import com.tencent.bugly.beta.Beta;

import java.lang.ref.WeakReference;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;

public class SettingsFragment extends AndroidxBaseFragment<FragmentSettingsBinding> implements View.OnClickListener {

    private Context context;
    private static WeakReferenceHandler weakReferenceHandler;
    private HistoryRecordBeanDao historyBeanDao;

    @Override
    protected void setupTopBarLayout() {
        context = getContext();
    }

    @Override
    protected void initData() {
        weakReferenceHandler = new WeakReferenceHandler(this);
        historyBeanDao = BaseApplication.getDaoSession().getHistoryRecordBeanDao();
        String emailAddress = Utils.readEmailAddress();
        if (!emailAddress.equals("")) {
            viewBinding.emailTextView.setText(emailAddress);
        }
        viewBinding.recordSize.setText(String.valueOf(historyBeanDao.loadAll().size()));
        viewBinding.appVersion.setText(BuildConfig.VERSION_NAME);
        String savedRegId = CacheDiskUtils.getInstance().getString("regId");
        if(savedRegId != null)
            viewBinding.pushTextView.setText(savedRegId);
    }

    public static void sendEmptyMessage() {
        weakReferenceHandler.sendEmptyMessage(2022021402);
    }

    private static class WeakReferenceHandler extends Handler {

        private final WeakReference<SettingsFragment> reference;

        private WeakReferenceHandler(SettingsFragment fragment) {
            reference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            SettingsFragment fragment = reference.get();
            Context context = fragment.getContext();
            assert context != null;
            if (msg.what == 2022021402) {
                fragment.viewBinding.recordSize.setText(String.valueOf(fragment.historyBeanDao.loadAll().size()));
            }
        }
    }

    private final ActivityResultLauncher<Intent> settingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (isNotificationEnable()) {
                startNotificationMonitorService();
            }
        }
    });

    //检测通知监听服务是否被授权
    private boolean isNotificationEnable() {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
        return packageNames.contains(context.getPackageName());
    }

    @Override
    protected void initEvent() {
        if (!isNotificationEnable()) {
            try {
                //打开通知监听设置页面
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                settingsLauncher.launch(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            startNotificationMonitorService();
        }
        viewBinding.tvRegValue.setText(JPushInterface.getRegistrationID(getContext()));
        viewBinding.btnCopy.setOnClickListener(this);
        viewBinding.btnTestLaunchDing.setOnClickListener(this);
        viewBinding.emailLayout.setOnClickListener(this);
        viewBinding.historyLayout.setOnClickListener(this);
        viewBinding.introduceLayout.setOnClickListener(this);
        viewBinding.pushLayout.setOnClickListener(this);
        viewBinding.rlVersion.setOnClickListener(this);
    }

    //切换通知监听器服务
    private void startNotificationMonitorService() {
        //创建常住通知栏
        Utils.createNotification();

        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(new ComponentName(context, NotificationMonitorService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(new ComponentName(context, NotificationMonitorService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        viewBinding.noticeCheckBox.setChecked(isNotificationEnable());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.emailLayout) {
            new InputDialog.Builder()
                    .setContext(context)
                    .setTitle("设置邮箱")
                    .setHintMessage("请输入邮箱")
                    .setNegativeButton("取消")
                    .setPositiveButton("确定")
                    .setOnDialogButtonClickListener(new InputDialog.OnDialogButtonClickListener() {
                        @Override
                        public void onConfirmClick(String value) {
                            if (!value.isEmpty()) {
                                Utils.saveEmailAddress(value);
                                viewBinding.emailTextView.setText(value);
                            } else {
                                EasyToast.showToast("什么都还没输入呢！", EasyToast.ERROR);
                            }
                        }

                        @Override
                        public void onCancelClick() {

                        }
                    }).build().show();
        } else if (id == R.id.historyLayout) {
            startActivity(new Intent(context, HistoryRecordActivity.class));
        } else if (id == R.id.introduceLayout) {
            Utils.showAlertDialog(getActivity(), "功能介绍", context.getString(R.string.about), "看完了", true);
        }else if(id ==R.id.btnCopy){
            ClipboardManager clipboardManager = (ClipboardManager) getContext()
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, JPushInterface.getRegistrationID(getContext())));
            EasyToast.showToast("已复制", EasyToast.SUCCESS);
        }else if(id == R.id.btnTestLaunchDing){
            getActivity().startActivity(IntentUtils.getLaunchAppIntent(Constant.DINGDING));
        }else if(id == R.id.pushLayout){
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, PushFragment.newInstance())
                    .commit();
        }else if (id == R.id.rlVersion){
            Beta.checkUpgrade();
        }
    }
}
