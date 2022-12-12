package org.strongswan.android.manager;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.sijav.reactnativeipsecvpn.R;
import org.strongswan.android.data.VpnProfile;
import org.strongswan.android.data.VpnProfileDataSource;
import org.strongswan.android.data.VpnType;
import org.strongswan.android.logic.VpnStateService;
import org.strongswan.android.utils.Constants;

public class VpnActivityWrapper {
    public static final String START_PROFILE = "org.strongswan.android.action.START_PROFILE";
    public static final String DISCONNECT = "org.strongswan.android.action.DISCONNECT";
    public static final String EXTRA_VPN_PROFILE_ID = "org.strongswan.android.VPN_PROFILE_ID";

    public static final boolean USE_BYOD = true;
    private static final String WAITING_FOR_RESULT = "WAITING_FOR_RESULT";
    private static final String PROFILE_NAME = "PROFILE_NAME";
    private static final String PROFILE_REQUIRES_PASSWORD = "REQUIRES_PASSWORD";
    private static final String PROFILE_RECONNECT = "RECONNECT";
    private static final String PROFILE_DISCONNECT = "DISCONNECT";
    private static final String DIALOG_TAG = "Dialog";

    private Bundle mProfileInfo;
    private boolean mWaitingForResult;
    private VpnStateService mService;

    private AppCompatActivity activity;
    private StateListener stateListener = null;


    private VpnStateService.VpnStateListener vpnStateListener = new VpnStateService.VpnStateListener() {
        @Override
        public void stateChanged() {
            long connectionID = mService.getConnectionID();
            VpnProfile profile = mService.getProfile();
            VpnStateService.State state = mService.getState();
            VpnStateService.ErrorState error = mService.getErrorState();
            String name = "";

            if (profile != null) {
                name = profile.getName();
            }

            if (reportError(connectionID, name, error)) {
                return;
            }

            stateListener.stateChanged();

            switch (state) {
                case DISABLED:
                    if (stateListener != null) {
                        stateListener.disabled();
                    }

                    break;
                case CONNECTING:
                    if (stateListener != null) {
                        stateListener.connecting();
                    }



                    break;
                case CONNECTED:
                    if (stateListener != null) {
                        stateListener.connected();
                    }

                    break;
                case DISCONNECTING:
                    if (stateListener != null) {
                        stateListener.disconnecting();
                    }

                    break;
            }

        }
    };
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((VpnStateService.LocalBinder) service).getService();
            mService.registerListener(vpnStateListener);
            // handleIntent();
        }
    };

    private  ActivityResultLauncher<Intent> mPrepareVpnService;
    private  ActivityResultLauncher<Intent> mAddToPowerWhitelist;

    public VpnActivityWrapper(AppCompatActivity _activity, StateListener _vpnStateListener) {
        this.activity = _activity;
        this.stateListener = _vpnStateListener;

        mPrepareVpnService = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    mWaitingForResult = false;
                    if (result.getResultCode() == RESULT_OK && mProfileInfo != null) {
                        onVpnServicePrepared();
                    } else {    /* this happens if the always-on VPN feature is activated by a different app or the user declined */
                        VpnNotSupportedError.showWithMessage(activity, R.string.vpn_not_supported_no_permission);
                    }
                }
        );


        mAddToPowerWhitelist = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    mWaitingForResult = false;
                    if (mProfileInfo != null && mService != null) {
                        mService.connect(mProfileInfo, true);
                    }

                }
        );
    }


    private boolean reportError(long connectionID, String name, VpnStateService.ErrorState error) {
        if (error == VpnStateService.ErrorState.NO_ERROR) {
            return false;
        }

        int retry = mService.getRetryIn();
        if (retry > 0) {
            Log.e("stswan:reportError:", "Retry in " + retry + " second");
        } else if (mService.getRetryTimeout() <= 0) {
            // mProgress.setVisibility(View.GONE);
        }
        Log.e("stswan:reportError:", "Failed to establish VPN:");

        return true;
    }

    public void connectVpn(VpnProfile vpnProfile) {
        startVpnProfile(vpnProfile);
    }

    public void disconnectVpn() {
        if (mService != null) {
            mService.disconnect();
        }
    }

    public void reconnectVpn() {
        if (mService != null) {
            mService.reconnect();
        }
    }

    public void bindService() {

        activity.bindService(new Intent(activity, VpnStateService.class),
                mServiceConnection, Service.BIND_AUTO_CREATE);
    }


    public void onStart() {
        if (mService != null) {
            mService.registerListener(vpnStateListener);
        }
    }


    public void onStop() {
        if (mService != null) {
            mService.unregisterListener(vpnStateListener);
        }
    }


    public void onSaveInstanceState(Bundle outState) {

        outState.putBoolean(WAITING_FOR_RESULT, mWaitingForResult);
    }


    public void onDestroy() {
        if (mService != null) {
            activity.unbindService(mServiceConnection);
        }
    }

    /**
     * Due to launchMode=singleTop this is called if the Activity already exists
     */

    public void onNewIntent(Intent intent) {
        if (mService != null) {
            //  handleIntent();
        }
    }

    /**
     * Prepare the VpnService. If this succeeds the current VPN profile is
     * started.
     *
     * @param profileInfo a bundle containing the information about the profile to be started
     */
    public void prepareVpnService(Bundle profileInfo) {
        Intent intent;

        if (mWaitingForResult) {
            mProfileInfo = profileInfo;
            return;
        }

        try {
            intent = VpnService.prepare(activity);
        } catch (IllegalStateException ex) {
            /* this happens if the always-on VPN feature (Android 4.2+) is activated */
            VpnNotSupportedError.showWithMessage(activity, R.string.vpn_not_supported_during_lockdown);
            return;
        } catch (NullPointerException ex) {
            /* not sure when this happens exactly, but apparently it does */
            VpnNotSupportedError.showWithMessage(activity, R.string.vpn_not_supported);
            return;
        }
        /* store profile info until the user grants us permission */
        mProfileInfo = profileInfo;
        if (intent != null) {
            try {
                mWaitingForResult = true;
                mPrepareVpnService.launch(intent);
            } catch (ActivityNotFoundException ex) {
                /* it seems some devices, even though they come with Android 4,
                 * don't have the VPN components built into the system image.
                 * com.android.vpndialogs/com.android.vpndialogs.ConfirmDialog
                 * will not be found then */
                VpnNotSupportedError.showWithMessage(activity, R.string.vpn_not_supported);
                mWaitingForResult = false;
            }
        } else {    /* user already granted permission to use VpnService */
            onVpnServicePrepared();
        }
    }

    /**
     * Called once the VpnService has been prepared and permission has been granted
     * by the user.
     */
    public void onVpnServicePrepared() {
        if (checkPowerWhitelist()) {
            if (mService != null) {
                mService.connect(mProfileInfo, true); // connect
            }

        }
    }

    /**
     * Check if we are on the system's power whitelist, if necessary, or ask the user
     * to add us.
     *
     * @return true if profile can be initiated immediately
     */
    private boolean checkPowerWhitelist() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
            if (!pm.isIgnoringBatteryOptimizations(activity.getPackageName()) &&
                    !pref.getBoolean(Constants.PREF_IGNORE_POWER_WHITELIST, false)) {
                if (activity.getSupportFragmentManager().isStateSaved()) {    /* we might get called via service connection and manual onActivityResult()
                 * call when the activity is not active anymore and fragment transactions
                 * would cause an illegalStateException */
                    return false;
                }
                PowerWhitelistRequired whitelist = new PowerWhitelistRequired(new PowerWhitelistRequired.PowerWhitelistRequiredListener() {
                    @Override
                    public void onPositiveClicked() {

                        mWaitingForResult = true;
                        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                Uri.parse("package:" + activity.getPackageName()));
                        mAddToPowerWhitelist.launch(intent);
                    }
                });
                whitelist.show(activity.getSupportFragmentManager(), DIALOG_TAG);
                return false;
            }
        }
        return true;
    }

    /**
     * Check if we are currently connected to a VPN connection
     *
     * @return true if currently connected
     */
    private boolean isConnected() {
        if (mService == null) {
            return false;
        }
        if (mService.getErrorState() != VpnStateService.ErrorState.NO_ERROR) {    /* allow reconnecting (even to a different profile) without confirmation if there is an error */
            return false;
        }
        return (mService.getState() == VpnStateService.State.CONNECTED || mService.getState() == VpnStateService.State.CONNECTING);
    }

    /**
     * Start the given VPN profile
     *
     * @param profile VPN profile
     */
    public void startVpnProfile(VpnProfile profile) {
        Bundle profileInfo = new Bundle();
        profileInfo.putString(VpnProfileDataSource.KEY_UUID, profile.getUUID().toString());
        profileInfo.putString(VpnProfileDataSource.KEY_USERNAME, profile.getUsername());
        profileInfo.putString(VpnProfileDataSource.KEY_PASSWORD, profile.getPassword());
        profileInfo.putBoolean(PROFILE_REQUIRES_PASSWORD, profile.getVpnType().has(VpnType.VpnTypeFeature.USER_PASS));
        profileInfo.putString(PROFILE_NAME, profile.getName());

        removeFragmentByTag(DIALOG_TAG);

        if (isConnected()) {
            profileInfo.putBoolean(PROFILE_RECONNECT, mService.getProfile().getUUID().equals(profile.getUUID()));

            ConfirmationDialog dialog = new ConfirmationDialog(new ConfirmationDialog.ConfirmationDialogListener() {
                @Override
                public void connectListener(Bundle profileInfo) {

                }

                @Override
                public void disconnectListener() {

                }

                @Override
                public void cancelListener() {

                }
            });
            dialog.setArguments(profileInfo);
            dialog.show(activity.getSupportFragmentManager(), DIALOG_TAG);
            return;
        }
        startVpnProfile(profileInfo);
    }

    /**
     * Start the given VPN profile asking the user for a password if required.
     *
     * @param profileInfo data about the profile
     */
    private void startVpnProfile(Bundle profileInfo) {
        prepareVpnService(profileInfo);
    }

    /**
     * Start the VPN profile referred to by the given intent. Displays an error
     * if the profile doesn't exist.
     *
     * @param intent Intent that caused us to start this
     */
    private void startVpnProfile(Intent intent) {
        VpnProfile profile = null;

        VpnProfileDataSource dataSource = new VpnProfileDataSource(activity);
        dataSource.open();
        String profileUUID = intent.getStringExtra(EXTRA_VPN_PROFILE_ID);
        if (profileUUID != null) {
            profile = dataSource.getVpnProfile(profileUUID);
        } else {
            long profileId = intent.getLongExtra(EXTRA_VPN_PROFILE_ID, 0);
            if (profileId > 0) {
                profile = dataSource.getVpnProfile(profileId);
            }
        }
        dataSource.close();

        if (profile != null) {
            startVpnProfile(profile);
        } else {
            Toast.makeText(activity, R.string.profile_not_found, Toast.LENGTH_LONG).show();

        }
    }

    /**
     * Disconnect the current connection, if any (silently ignored if there is no connection).
     *
     * @param intent Intent that caused us to start this
     */
    private void disconnect(Intent intent) {
        VpnProfile profile = null;

        removeFragmentByTag(DIALOG_TAG);

        String profileUUID = intent.getStringExtra(EXTRA_VPN_PROFILE_ID);
        if (profileUUID != null) {
            VpnProfileDataSource dataSource = new VpnProfileDataSource(activity);
            dataSource.open();
            profile = dataSource.getVpnProfile(profileUUID);
            dataSource.close();
        }

        if (mService != null) {
            if (mService.getState() == VpnStateService.State.CONNECTED ||
                    mService.getState() == VpnStateService.State.CONNECTING) {
                if (profile != null && profile.equals(mService.getProfile())) {    /* allow explicit termination without confirmation */
                    mService.disconnect();

                    return;
                }
                Bundle args = new Bundle();
                args.putBoolean(PROFILE_DISCONNECT, true);

                ConfirmationDialog dialog = new ConfirmationDialog(new ConfirmationDialog.ConfirmationDialogListener() {
                    @Override
                    public void connectListener(Bundle profileInfo) {
                        startVpnProfile(profileInfo);
                    }

                    @Override
                    public void disconnectListener() {

                        if (mService != null) {
                            mService.disconnect();
                        }
                    }

                    @Override
                    public void cancelListener() {

                    }
                });
                dialog.setArguments(args);
                dialog.show(activity.getSupportFragmentManager(), DIALOG_TAG);
            } else {

            }
        }
    }

    /**
     * Handle the Intent of this Activity depending on its action
     */
    private void handleIntent(Intent intent) {

        if (START_PROFILE.equals(intent.getAction())) {
            //  startVpnProfile(intent);
        } else if (DISCONNECT.equals(intent.getAction())) {
            disconnect(intent);
        }
    }

    /**
     * Dismiss dialog if shown
     */
    public void removeFragmentByTag(String tag) {
        FragmentManager fm = activity.getSupportFragmentManager();
        Fragment login = fm.findFragmentByTag(tag);
        if (login != null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.remove(login);
            ft.commit();
        }
    }

    /**
     * Class that displays a confirmation dialog if a VPN profile is already connected
     * and then initiates the selected VPN profile if the user confirms the dialog.
     */
    public static class ConfirmationDialog extends AppCompatDialogFragment {

        private ConfirmationDialogListener listener = null;

        public ConfirmationDialog(ConfirmationDialogListener _listener) {
            listener = _listener;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Bundle profileInfo = getArguments();
            int icon = android.R.drawable.ic_dialog_alert;
            int title = R.string.connect_profile_question;
            int message = R.string.replaces_active_connection;
            int button = R.string.connect;

            if (profileInfo.getBoolean(PROFILE_RECONNECT)) {
                icon = android.R.drawable.ic_dialog_info;
                title = R.string.vpn_connected;
                message = R.string.vpn_profile_connected;
                button = R.string.reconnect;
            } else if (profileInfo.getBoolean(PROFILE_DISCONNECT)) {
                title = R.string.disconnect_question;
                message = R.string.disconnect_active_connection;
                button = R.string.disconnect;
            }

            DialogInterface.OnClickListener connectListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (listener != null) {
                        listener.connectListener(profileInfo);
                    }
                }
            };
            DialogInterface.OnClickListener disconnectListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (listener != null) {
                        listener.disconnectListener();
                    }

                }
            };
            DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (listener != null) {
                        listener.cancelListener();
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setIcon(icon)
                    .setTitle("asd")
                    .setMessage(message);

            if (profileInfo.getBoolean(PROFILE_DISCONNECT)) {
                builder.setPositiveButton(button, disconnectListener);
            } else {
                builder.setPositiveButton(button, connectListener);
            }

            if (profileInfo.getBoolean(PROFILE_RECONNECT)) {
                builder.setNegativeButton(R.string.disconnect, disconnectListener);
                builder.setNeutralButton(android.R.string.cancel, cancelListener);
            } else {
                builder.setNegativeButton(android.R.string.cancel, cancelListener);
            }
            return builder.create();
        }

        @Override
        public void onCancel(DialogInterface dialog) {

        }

        interface ConfirmationDialogListener {
            public void connectListener(Bundle profileInfo);

            public void disconnectListener();

            public void cancelListener();

        }
    }

    /**
     * Class that displays a warning before asking the user to add the app to the
     * device's power whitelist.
     */
    public static class PowerWhitelistRequired extends AppCompatDialogFragment {
        private PowerWhitelistRequiredListener listener = null;

        public PowerWhitelistRequired(PowerWhitelistRequiredListener _listener) {
            listener = _listener;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.power_whitelist_title)
                    .setMessage(R.string.power_whitelist_text)
                    .setPositiveButton(android.R.string.ok, (dialog, id) -> {

                        if (listener != null) {
                            listener.onPositiveClicked();
                        }
                    }).create();
        }

        @Override
        public void onCancel(@NonNull DialogInterface dialog) {

        }

        interface PowerWhitelistRequiredListener {
            void onPositiveClicked();
        }
    }

    /**
     * Class representing an error message which is displayed if VpnService is
     * not supported on the current device.
     */
    public static class VpnNotSupportedError extends AppCompatDialogFragment {
        static final String ERROR_MESSAGE_ID = "org.strongswan.android.VpnNotSupportedError.MessageId";

        public static void showWithMessage(AppCompatActivity activity, int messageId) {
            Bundle bundle = new Bundle();
            bundle.putInt(ERROR_MESSAGE_ID, messageId);
            VpnNotSupportedError dialog = new VpnNotSupportedError();
            dialog.setArguments(bundle);
            dialog.show(activity.getSupportFragmentManager(), DIALOG_TAG);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Bundle arguments = getArguments();
            final int messageId = arguments.getInt(ERROR_MESSAGE_ID);
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.vpn_not_supported_title)
                    .setMessage(messageId)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    }).create();
        }

        @Override
        public void onCancel(DialogInterface dialog) {

        }
    }


    public interface StateListener {
        void disabled();

        void stateChanged();

        void connecting();

        void connected();

        void disconnecting();
    }

}
