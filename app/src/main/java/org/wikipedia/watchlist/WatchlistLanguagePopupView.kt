package org.wikipedia.watchlist

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.PopupWindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_watchlist_language.view.*
import kotlinx.android.synthetic.main.view_watchlist_language_popup.view.*
import org.wikipedia.R
import org.wikipedia.WikipediaApp
import org.wikipedia.settings.Prefs

class WatchlistLanguagePopupView constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs), CompoundButton.OnCheckedChangeListener {
    var callback: Callback? = null
    var popupWindowHost: PopupWindow? = null
    val disabledLangCodes = Prefs.getWatchlistDisabledLanguages()

    init {
        View.inflate(context, R.layout.view_watchlist_language_popup, this)
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        languageRecyclerView.layoutManager = LinearLayoutManager(getContext())
        languageRecyclerView.adapter = RecyclerAdapter()
    }

    fun show(anchorView: View?, callback: Callback?) {
        if (anchorView == null) {
            return
        }
        this.callback = callback
        popupWindowHost = PopupWindow(this, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindowHost!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        PopupWindowCompat.setOverlapAnchor(popupWindowHost!!, true)
        PopupWindowCompat.showAsDropDown(popupWindowHost!!, anchorView, 0, 0, Gravity.END)
    }

    internal inner class WatchlistLangViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bindItem(position: Int) {
            val langCode = WikipediaApp.getInstance().language().appLanguageCodes[position]
            itemView.findViewById<TextView>(R.id.languageText).text = WikipediaApp.getInstance().language().getAppLanguageLocalizedName(langCode)
            itemView.findViewById<TextView>(R.id.langCodeText).text = langCode
            val checkBox = itemView.findViewById<CheckBox>(R.id.languageCheckBox)
            checkBox.tag = langCode
            checkBox.isChecked = !disabledLangCodes.contains(langCode)
            checkBox.setOnCheckedChangeListener(this@WatchlistLanguagePopupView)
        }
    }

    internal inner class RecyclerAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemCount(): Int {
            return WikipediaApp.getInstance().language().appLanguageCodes.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return WatchlistLangViewHolder(LayoutInflater.from(context).inflate(R.layout.item_watchlist_language, parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as WatchlistLangViewHolder).bindItem(position)
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        val langCode = buttonView!!.tag as String
        if (isChecked) {
            disabledLangCodes.remove(langCode)
        } else if (!isChecked) {
            disabledLangCodes.add(langCode)
        }
        Prefs.setWatchlistDisabledLanguages(disabledLangCodes)
        callback?.onLanguageChanged()
    }

    interface Callback {
        fun onLanguageChanged()
    }
}
