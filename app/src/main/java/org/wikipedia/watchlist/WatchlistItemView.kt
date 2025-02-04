package org.wikipedia.watchlist

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.item_watchlist.view.*
import org.wikipedia.R
import org.wikipedia.WikipediaApp
import org.wikipedia.dataclient.mwapi.MwQueryResult
import org.wikipedia.util.DateUtil
import org.wikipedia.util.ResourceUtil
import org.wikipedia.util.StringUtil

class WatchlistItemView constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {
    var callback: Callback? = null
    private var item: MwQueryResult.WatchlistItem? = null
    private var clickListener = OnClickListener {
        if (item != null) {
            callback?.onItemClick(item!!)
        }
    }

    init {
        View.inflate(context, R.layout.item_watchlist, this)
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        containerView.setOnClickListener(clickListener)
        diffText.setOnClickListener(clickListener)
        userNameText.setOnClickListener {
            if (item != null) {
                callback?.onUserClick(item!!)
            }
        }
        if (WikipediaApp.getInstance().language().appLanguageCodes.size == 1) {
            langCodeBackground.visibility = GONE
            langCodeText.visibility = GONE
        } else {
            langCodeBackground.visibility = VISIBLE
            langCodeText.visibility = VISIBLE
        }
    }

    fun setItem(item: MwQueryResult.WatchlistItem) {
        this.item = item
        titleText.text = item.title
        langCodeText.text = item.wiki.languageCode()
        summaryText.text = StringUtil.fromHtml(item.parsedComment)
        timeText.text = DateUtil.getTimeString(item.date)
        userNameText.text = item.user

        userNameText.setIconResource(if (item.isAnon) R.drawable.ic_anonymous_ooui else R.drawable.ic_user_talk)
        if (item.logType.isNotEmpty()) {
            when (item.logType) {
                context.getString(R.string.page_moved) -> {
                    setButtonTextAndIconColor(context.getString(R.string.watchlist_page_moved), R.attr.suggestions_background_color, R.drawable.ic_info_outline_black_24dp)
                }
                context.getString(R.string.page_protected) -> {
                    setButtonTextAndIconColor(context.getString(R.string.watchlist_page_protected), R.attr.suggestions_background_color, R.drawable.ic_baseline_lock_24)
                }
                context.getString(R.string.page_deleted) -> {
                    setButtonTextAndIconColor(context.getString(R.string.watchlist_page_deleted), R.attr.suggestions_background_color, R.drawable.ic_delete_white_24dp)
                }
            }
        } else {
            val diffByteCount = item.newlen - item.oldlen
            setButtonTextAndIconColor(String.format("%+d", diffByteCount), R.attr.color_group_22)
            if (diffByteCount >= 0) {
                diffText.setTextColor(if (diffByteCount > 0) ContextCompat.getColor(context, R.color.green50)
                else ResourceUtil.getThemedColor(context, R.attr.material_theme_secondary_color))
            } else {
                diffText.setTextColor(ContextCompat.getColor(context, R.color.red50))
            }
        }
    }

    private fun setButtonTextAndIconColor(text: String, @AttrRes backgroundTint: Int, @DrawableRes iconResourceDrawable: Int? = null) {
        val themedTint = ColorStateList.valueOf(ResourceUtil.getThemedColor(context, R.attr.color_group_61))
        diffText.text = text
        diffText.setTextColor(themedTint)
        iconResourceDrawable?.let {
            diffText.icon = ContextCompat.getDrawable(context, it)
        }
        diffText.iconTint = themedTint
        diffText.setBackgroundColor(ResourceUtil.getThemedColor(context, backgroundTint))
    }

    interface Callback {
        fun onItemClick(item: MwQueryResult.WatchlistItem)
        fun onUserClick(item: MwQueryResult.WatchlistItem)
    }
}
