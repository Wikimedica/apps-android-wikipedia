package org.wikipedia.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.material.snackbar.Snackbar;
import com.skydoves.balloon.ArrowConstraints;
import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.Balloon;

import org.wikipedia.R;
import org.wikipedia.analytics.SuggestedEditsFunnel;
import org.wikipedia.dataclient.WikiSite;
import org.wikipedia.main.MainActivity;
import org.wikipedia.page.PageActivity;
import org.wikipedia.page.PageTitle;
import org.wikipedia.random.RandomActivity;
import org.wikipedia.readinglist.ReadingListActivity;
import org.wikipedia.staticdata.SpecialAliasData;
import org.wikipedia.staticdata.UserAliasData;
import org.wikipedia.suggestededits.SuggestionsActivity;

import java.util.concurrent.TimeUnit;

import static org.wikipedia.util.UriUtil.visitInExternalBrowser;

public final class FeedbackUtil {
    public static final int LENGTH_DEFAULT = (int) TimeUnit.SECONDS.toMillis(5);
    public static final int LENGTH_MEDIUM = (int) TimeUnit.SECONDS.toMillis(8);
    public static final int LENGTH_LONG = (int) TimeUnit.SECONDS.toMillis(15);
    private static View.OnLongClickListener TOOLBAR_LONG_CLICK_LISTENER = (v) -> {
        showToastOverView(v, v.getContentDescription(), LENGTH_DEFAULT);
        return true;
    };

    public static void showError(Activity activity, Throwable e) {
        ThrowableUtil.AppError error = ThrowableUtil.getAppError(activity, e);
        makeSnackbar(activity, error.getError(), LENGTH_DEFAULT).show();
    }

    public static void showMessageAsPlainText(Activity activity, CharSequence possibleHtml) {
        CharSequence richText = StringUtil.fromHtml(possibleHtml.toString());
        showMessage(activity, richText.toString());
    }

    public static void showMessage(Fragment fragment, @StringRes int text) {
        makeSnackbar(fragment.requireActivity(), fragment.getString(text), Snackbar.LENGTH_LONG).show();
    }

    public static void showMessage(Fragment fragment, @NonNull String text) {
        makeSnackbar(fragment.requireActivity(), text, Snackbar.LENGTH_LONG).show();
    }

    public static void showMessage(Activity activity, @StringRes int resId) {
        showMessage(activity, activity.getString(resId), Snackbar.LENGTH_LONG);
    }

    public static void showMessage(Activity activity, CharSequence text) {
        showMessage(activity, text, Snackbar.LENGTH_LONG);
    }

    public static void showMessage(Activity activity, @StringRes int resId, int duration) {
        showMessage(activity, activity.getString(resId), duration);
    }

    public static void showMessage(Activity activity, CharSequence text, int duration) {
        makeSnackbar(activity, text, duration).show();
    }

    public static void showPrivacyPolicy(Context context) {
        visitInExternalBrowser(context, Uri.parse(context.getString(R.string.privacy_policy_url)));
    }

    public static void showOfflineReadingAndData(Context context) {
        visitInExternalBrowser(context, Uri.parse(context.getString(R.string.offline_reading_and_data_url)));
    }

    public static void showAboutWikipedia(Context context) {
        visitInExternalBrowser(context, Uri.parse(context.getString(R.string.about_wikipedia_url)));
    }

    public static void showAndroidAppFAQ(Context context) {
        visitInExternalBrowser(context, Uri.parse(context.getString(R.string.android_app_faq_url)));
    }

    public static void showAndroidAppRequestAnAccount(Context context) {
        visitInExternalBrowser(context, Uri.parse(context.getString(R.string.android_app_request_an_account_url)));
    }

    public static void showAndroidAppEditingFAQ(Context context) {
        showAndroidAppEditingFAQ(context, R.string.android_app_edit_help_url);
    }

    public static void showUserContributionsPage(@NonNull Context context, @NonNull String username, String languageCode) {
        PageTitle title = new PageTitle(SpecialAliasData.valueFor(languageCode) + ":" + "Contributions/" + username, WikiSite.forLanguageCode(languageCode));
        visitInExternalBrowser(context, Uri.parse(title.getUri()));
    }

    public static void showUserProfilePage(@NonNull Context context, @NonNull String username, String languageCode) {
        PageTitle title = new PageTitle(UserAliasData.valueFor(languageCode) + ":" + username, WikiSite.forLanguageCode(languageCode));
        visitInExternalBrowser(context, Uri.parse(title.getUri()));
    }

    public static void showAndroidAppEditingFAQ(Context context, @StringRes int urlStr) {
        SuggestedEditsFunnel.get().helpOpened();
        visitInExternalBrowser(context, Uri.parse(context.getString(urlStr)));
    }

    public static void showProtectionStatusMessage(@NonNull Activity activity, @Nullable String status) {
        if (TextUtils.isEmpty(status)) {
            return;
        }
        String message;
        switch (status) {
            case "sysop":
                message = activity.getString(R.string.page_protected_sysop);
                break;
            case "autoconfirmed":
                message = activity.getString(R.string.page_protected_autoconfirmed);
                break;
            default:
                message = activity.getString(R.string.page_protected_other, status);
                break;
        }
        showMessage(activity, message);
    }

    public static void setButtonLongPressToast(View... views) {
        for (View v : views) {
            v.setOnLongClickListener(TOOLBAR_LONG_CLICK_LISTENER);
        }
    }

    public static void showTapTargetView(@NonNull Activity activity, @NonNull View target,
                                         @StringRes int titleId, @StringRes int descriptionId,
                                         @Nullable TapTargetView.Listener listener) {
        final float tooltipAlpha = 0.9f;
        TapTargetView.showFor(activity,
                TapTarget.forView(target, activity.getString(titleId),
                        activity.getString(descriptionId))
                        .targetCircleColor(ResourceUtil.getThemedAttributeId(activity, R.attr.colorAccent))
                        .outerCircleColor(ResourceUtil.getThemedAttributeId(activity, R.attr.colorAccent))
                        .outerCircleAlpha(tooltipAlpha)
                        .cancelable(true)
                        .transparentTarget(true),
                listener);
    }

    public static Snackbar makeSnackbar(Activity activity, CharSequence text, int duration) {
        View view = findBestView(activity);
        Snackbar snackbar = Snackbar.make(view, StringUtil.fromHtml(text.toString()), duration);
        TextView textView = snackbar.getView().findViewById(R.id.snackbar_text);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        TextView actionView = snackbar.getView().findViewById(R.id.snackbar_action);
        actionView.setTextColor(ResourceUtil.getThemedColor(view.getContext(), R.attr.color_group_52));
        return snackbar;
    }

    public static Toast showToastOverView(View view, CharSequence text, int duration) {
        Toast toast = Toast.makeText(view.getContext(), text, duration);
        View v = LayoutInflater.from(view.getContext()).inflate(R.layout.abc_tooltip, null);
        TextView message = v.findViewById(R.id.message);
        message.setText(text);
        message.setMaxLines(Integer.MAX_VALUE);
        toast.setView(v);
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        toast.setGravity(Gravity.TOP | Gravity.START, location[0], location[1]);
        toast.show();
        return toast;
    }

    public static Balloon showTooltip(@NonNull View anchor, @NonNull CharSequence text, boolean aboveOrBelow, boolean autoDismiss) {
        return showTooltip(getTooltip(anchor.getContext(), text, aboveOrBelow, autoDismiss), anchor, aboveOrBelow, autoDismiss);
    }

    public static Balloon showTooltip(@NonNull View anchor, @LayoutRes int layoutRes, int layoutHeight,
                                      int arrowAnchorPadding, int topOrBottomMargin, boolean aboveOrBelow, boolean autoDismiss) {
        return showTooltip(getTooltip(anchor.getContext(), layoutRes, layoutHeight, arrowAnchorPadding, topOrBottomMargin, aboveOrBelow, autoDismiss), anchor, aboveOrBelow, autoDismiss);
    }

    @SuppressWarnings("checkstyle:magicnumber")
    private static Balloon showTooltip(@NonNull Balloon balloon, @NonNull View anchor, boolean aboveOrBelow, boolean autoDismiss) {
        if (aboveOrBelow) {
            balloon.showAlignTop(anchor, 0, DimenUtil.roundedDpToPx(8f));
        } else {
            balloon.showAlignBottom(anchor, 0, -DimenUtil.roundedDpToPx(8f));
        }
        if (!autoDismiss && anchor.getContext() instanceof MainActivity) {
            ((MainActivity) anchor.getContext()).setCurrentTooltip(balloon);
        }
        return balloon;
    }


    @SuppressWarnings("checkstyle:magicnumber")
    public static Balloon getTooltip(@NonNull Context context, @NonNull CharSequence text, boolean aboveOrBelow, boolean autoDismiss) {
        return getTooltipBuilder(context, 0, aboveOrBelow, autoDismiss)
                .setText(text)
                .setTextSize(14f)
                .setTextTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL))
                .setTextColor(Color.WHITE)
                .setPadding(16)
                .build();
    }

    @SuppressWarnings("checkstyle:magicnumber")
    private static Balloon getTooltip(@NonNull Context context, @LayoutRes int layoutRes, int layoutHeight,
                                     int arrowAnchorPadding, int topOrBottomMargin, boolean aboveOrBelow, boolean autoDismiss) {
        return getTooltipBuilder(context, topOrBottomMargin, aboveOrBelow, autoDismiss)
                .setLayout(layoutRes)
                .setHeight(layoutHeight)
                .setWidthRatio(DimenUtil.isLandscape(context) ? 0.4f : 0.8f)
                .setArrowAlignAnchorPadding(arrowAnchorPadding)
                .build();
    }

    @SuppressWarnings("checkstyle:magicnumber")
    private static Balloon.Builder getTooltipBuilder(@NonNull Context context, int topOrBottomMargin, boolean aboveOrBelow, boolean autoDismiss) {
        return new Balloon.Builder(context)
                .setArrowDrawableResource(R.drawable.ic_tooltip_arrow_up)
                .setArrowConstraints(ArrowConstraints.ALIGN_ANCHOR)
                .setArrowOrientation(aboveOrBelow ? ArrowOrientation.BOTTOM : ArrowOrientation.TOP)
                .setArrowSize(24)
                .setMarginLeft(8)
                .setMarginRight(8)
                .setMarginTop(aboveOrBelow ? 0 : topOrBottomMargin)
                .setMarginBottom(aboveOrBelow ? topOrBottomMargin : 0)
                .setBackgroundColorResource(ResourceUtil.getThemedAttributeId(context, R.attr.colorAccent))
                .setDismissWhenTouchOutside(autoDismiss);
    }

    private static View findBestView(Activity activity) {
        if (activity instanceof MainActivity) {
            return activity.findViewById(R.id.fragment_main_coordinator);
        } else if (activity instanceof PageActivity) {
            return activity.findViewById(R.id.fragment_page_coordinator);
        } else if (activity instanceof RandomActivity) {
            return activity.findViewById(R.id.random_coordinator_layout);
        } else if (activity instanceof ReadingListActivity) {
            return activity.findViewById(R.id.fragment_reading_list_coordinator);
        } else if (activity instanceof SuggestionsActivity) {
            return activity.findViewById(R.id.suggestedEditsCardsCoordinator);
        } else {
            return activity.findViewById(android.R.id.content);
        }
    }

    private FeedbackUtil() {
    }
}
