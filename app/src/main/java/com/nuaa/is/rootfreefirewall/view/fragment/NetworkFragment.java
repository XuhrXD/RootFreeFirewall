package com.nuaa.is.rootfreefirewall.view.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.nuaa.is.rootfreefirewall.R;
import com.nuaa.is.rootfreefirewall.service.FirewallVpnService;

/**
 * Network模块 Fragment
 */
public class NetworkFragment extends Fragment {

    // TAG
    private static final String TAG = "RFF-NetworkFragment";

    // Service & Activity 请求码
    private static final int REQUEST_CODE__FIREWALL_VPN_SERVICE = 678;

    // 等待 Vpn 启动状态
    private boolean waittingVpnStart;

    // UI组件
    private Button startFirewallButton;
    private Button configFirewallButton;

    private BroadcastReceiver vpnStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 如果收到了服务已经运行的广播，则刚更新 flag
            if (FirewallVpnService.BROADCAST_VPN_STATE.equals(intent.getAction()) &&
                    intent.getBooleanExtra("running", false)) {
                waittingVpnStart = false;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        // 设置按钮状态
        this.setButtonEnableStatus(!waittingVpnStart && !FirewallVpnService.isRunning());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_network, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 获取UI组件
        this.getUIComponent();
        // 添加组件监听事件
        this.addComponentListener();
        // 注册广播接收器
        this.registerBroadcastReceiver();

        this.waittingVpnStart = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 如果是启动VpnInterface的请求
        if (requestCode == REQUEST_CODE__FIREWALL_VPN_SERVICE) {
            if (resultCode == getActivity().RESULT_OK) {
                // 开启服务
                getActivity().startService(new Intent(getActivity(), FirewallVpnService.class));
                this.waittingVpnStart = true;

                // 调整启动 VPN 按钮状态
                this.setButtonEnableStatus(false);
            }
        }
    }

    // 注册广播接收器函数
    private void registerBroadcastReceiver() {
        // VpnState 广播
        LocalBroadcastManager
                .getInstance(getActivity())
                .registerReceiver(vpnStateReceiver, new IntentFilter(FirewallVpnService.BROADCAST_VPN_STATE));
    }

    // 获取UI组件函数
    private void getUIComponent() {
        this.startFirewallButton = getActivity().findViewById(R.id.fragment_network__start_firewall_button);
        this.configFirewallButton = getActivity().findViewById(R.id.fragment_network__config_firewall_button);
    }

    // 添加组件监听事件函数
    private void addComponentListener() {
        // 开启防火墙监听事件
        this.startFirewallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 准备VpnService
                prepareVpnService();
            }
        });
    }

    // 准备VpnService函数
    private void prepareVpnService() {
        // 让用户确认是否允许使用VpnService的权限
        Intent intent = VpnService.prepare(getActivity());

        if (intent != null) {
            // 如果用户没有同意，则询问用户是否允许，并且在用户同意之后启动VpnInterface
            startActivityForResult(intent, REQUEST_CODE__FIREWALL_VPN_SERVICE);
        } else {
            // 如果用户已经确认过了，则直接开启VpnInterface
            onActivityResult(REQUEST_CODE__FIREWALL_VPN_SERVICE, getActivity().RESULT_OK, null);
        }
    }

    // 设置启动防火墙按钮状态
    private void setButtonEnableStatus(boolean enable) {
        // 设置激活状态
        this.startFirewallButton.setEnabled(enable);
        this.configFirewallButton.setEnabled(enable);
        // 设置文字
        this.startFirewallButton.setText(
                enable ? getString(R.string.fragment_network__start_firewall_button__enable__text) :
                        getString(R.string.fragment_network__start_firewall_button__disabled__text)
        );
    }
}
