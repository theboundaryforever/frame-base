/*
 * Tencent is pleased to support the open source community by making QMUI_Android available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yuehai.ui.widget.qmui.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.LayoutRes;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatImageView;


import com.adealik.frame.mvvm.util.ActivityUtilKt;
import com.adealik.frame.mvvm.util.ThreadUtilKt;
import com.yuehai.ui.R;
import com.yuehai.ui.widget.qmui.widget.QMUILoadingView;
import com.yuehai.ui.widget.qmui.widget.util.QMUIResHelper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 提供一个浮层展示在屏幕中间, 一般使用 {@link Builder} 或 {@link CustomBuilder} 生成。
 * <ul>
 * <li>{@link Builder} 提供了一个图标和一行文字的样式, 其中图标有几种类型可选, 见 {@link Builder.IconType}</li>
 * <li>{@link CustomBuilder} 支持传入自定义的 layoutResId, 达到自定义 TipDialog 的效果。</li>
 * </ul>
 *
 * @author cginechen
 * @date 2016-10-14
 */

public class QMUITipDialog extends QMUIBaseDialog {

    private long mDismissDelayMills = 0;

    public QMUITipDialog(Context context) {
        this(context, R.style.QMUI_TipDialog);
    }

    public QMUITipDialog(Context context, int themeResId) {
        super(context, themeResId);
        setCanceledOnTouchOutside(false);
    }

    /**
     * 生成默认的 {@link QMUITipDialog}
     * <p>
     * 提供了一个图标和一行文字的样式, 其中图标有几种类型可选。见 {@link IconType}
     * </p>
     *
     * @see CustomBuilder
     */
    public static class Builder {
        /**
         * 不显示任何icon
         */
        public static final int ICON_TYPE_NOTHING = 0;
        /**
         * 显示 Loading 图标
         */
        public static final int ICON_TYPE_LOADING = 1;
        /**
         * 显示成功图标
         */
        public static final int ICON_TYPE_SUCCESS = 2;
        /**
         * 显示失败图标
         */
        public static final int ICON_TYPE_FAIL = 3;
        /**
         * 显示信息图标
         */
        public static final int ICON_TYPE_INFO = 4;
        /**
         * 显示自定义图标
         */
        public static final int ICON_TYPE_CUSTOM = 5;

        @IntDef({ICON_TYPE_NOTHING, ICON_TYPE_LOADING, ICON_TYPE_SUCCESS, ICON_TYPE_FAIL, ICON_TYPE_INFO, ICON_TYPE_CUSTOM})
        @Retention(RetentionPolicy.SOURCE)
        public @interface IconType {
        }

        private @IconType
        int mCurrentIconType = ICON_TYPE_NOTHING;

        private Context mContext;

        private CharSequence mTipWord;

        private Drawable mCustomIconDrawable;

        private long mDismissDelayMills = 0;

        public Builder(Context context) {
            mContext = context;
        }

        /**
         * 设置 icon 显示的内容
         *
         * @see IconType
         */
        public Builder setIconType(@IconType int iconType) {
            mCurrentIconType = iconType;
            return this;
        }

        /**
         * 设置显示的文案
         */
        public Builder setTipWord(CharSequence tipWord) {
            mTipWord = tipWord;
            return this;
        }

        public Builder setCustomIconDrawable(Drawable drawable) {
            mCustomIconDrawable = drawable;
            return this;
        }

        public Builder setDismissDelayMillis(Long millis) {
            mDismissDelayMills = millis;
            return this;
        }

        public QMUITipDialog create() {
            return create(true);
        }

        public QMUITipDialog create(boolean cancelable) {
            return create(cancelable, R.style.QMUI_TipDialog);
        }

        /**
         * 创建 Dialog, 但没有弹出来, 如果要弹出来, 请调用返回值的 {@link Dialog#show()} 方法
         *
         * @param cancelable 按系统返回键是否可以取消
         * @return 创建的 Dialog
         */
        public QMUITipDialog create(boolean cancelable, int style) {
            QMUITipDialog dialog = new QMUITipDialog(mContext, style);
            dialog.setCancelable(cancelable);
            Context dialogContext = dialog.getContext();
            QMUITipDialogView dialogView = new QMUITipDialogView(dialogContext);

            dialog.mDismissDelayMills = mDismissDelayMills;
            if (mCurrentIconType == ICON_TYPE_LOADING) {
                QMUILoadingView loadingView = new QMUILoadingView(dialogContext);
                loadingView.setColor(QMUIResHelper.getAttrColor(
                        dialogContext, R.attr.qmui_skin_support_tip_dialog_loading_color));

                loadingView.setSize(QMUIResHelper.getAttrDimen(
                        dialogContext, R.attr.qmui_tip_dialog_loading_size));
                dialogView.addView(loadingView, onCreateIconOrLoadingLayoutParams(dialogContext));

            } else if (mCurrentIconType == ICON_TYPE_SUCCESS ||
                    mCurrentIconType == ICON_TYPE_FAIL ||
                    mCurrentIconType == ICON_TYPE_INFO ||
                    mCurrentIconType == ICON_TYPE_CUSTOM) {
                ImageView imageView = new AppCompatImageView(dialogContext);

                Drawable drawable;
                if (mCurrentIconType == ICON_TYPE_SUCCESS) {
                    drawable = QMUIResHelper.getAttrDrawable(
                            dialogContext, R.attr.qmui_skin_support_tip_dialog_icon_success_src);
                } else if (mCurrentIconType == ICON_TYPE_FAIL) {
                    drawable = QMUIResHelper.getAttrDrawable(
                            dialogContext, R.attr.qmui_skin_support_tip_dialog_icon_error_src);
                } else if (mCurrentIconType == ICON_TYPE_CUSTOM) {
                    drawable = mCustomIconDrawable;
                    if (drawable == null) {
                        drawable = QMUIResHelper.getAttrDrawable(
                                dialogContext, R.attr.qmui_skin_support_tip_dialog_icon_info_src);
                    }
                } else {
                    drawable = QMUIResHelper.getAttrDrawable(
                            dialogContext, R.attr.qmui_skin_support_tip_dialog_icon_info_src);
                }
                imageView.setImageDrawable(drawable);
                dialogView.addView(imageView, onCreateIconOrLoadingLayoutParams(dialogContext));

            }

            if (mTipWord != null && mTipWord.length() > 0) {
                TextView tipView = new AppCompatAutoCompleteTextView(dialogContext);
                tipView.setEllipsize(TextUtils.TruncateAt.END);
                tipView.setId(R.id.qmui_tip_content_id);
                tipView.setGravity(Gravity.CENTER);
                tipView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        QMUIResHelper.getAttrDimen(dialogContext, R.attr.qmui_tip_dialog_text_size));
                tipView.setTextColor(QMUIResHelper.getAttrColor(
                        dialogContext, R.attr.qmui_skin_support_tip_dialog_text_color));
                tipView.setText(mTipWord);
                dialogView.addView(tipView, onCreateTextLayoutParams(dialogContext, mCurrentIconType));
            }
            dialog.setContentView(dialogView);
            return dialog;
        }

        protected LinearLayout.LayoutParams onCreateIconOrLoadingLayoutParams(Context context) {
            return new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }


        protected LinearLayout.LayoutParams onCreateTextLayoutParams(Context context, @IconType int iconType) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (iconType != ICON_TYPE_NOTHING) {
                lp.topMargin = QMUIResHelper.getAttrDimen(context, R.attr.qmui_tip_dialog_text_margin_top);
            }
            return lp;
        }

    }

    private Runnable dismissRunnable = new Runnable() {
        @Override
        public void run() {
            dismiss();
        }
    };

    @Override
    public void show() {
        Context context = getContext();
        if (context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }

        if (ActivityUtilKt.isActivityInValid(context)) {
            return;
        }

        super.show();
        if (mDismissDelayMills > 0) {
            ThreadUtilKt.runOnUiThread(dismissRunnable, mDismissDelayMills);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ThreadUtilKt.removeUiCallbacks(dismissRunnable);
    }

    /**
     * CustomBuilder with xml layout
     */
    public static class CustomBuilder {
        private Context mContext;
        private int mContentLayoutId;

        public CustomBuilder(Context context) {
            mContext = context;
        }

        public CustomBuilder setContent(@LayoutRes int layoutId) {
            mContentLayoutId = layoutId;
            return this;
        }

        public QMUITipDialog create() {
            QMUITipDialog dialog = new QMUITipDialog(mContext);
            Context dialogContext = dialog.getContext();
            QMUITipDialogView tipDialogView = new QMUITipDialogView(dialogContext);
            LayoutInflater.from(dialogContext).inflate(mContentLayoutId, tipDialogView, true);
            dialog.setContentView(tipDialogView);
            return dialog;
        }
    }
}
