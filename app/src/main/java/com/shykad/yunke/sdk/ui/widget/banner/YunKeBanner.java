package com.shykad.yunke.sdk.ui.widget.banner;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shykad.yunke.sdk.R;
import com.shykad.yunke.sdk.config.HttpConfig;
import com.shykad.yunke.sdk.engine.YunKeEngine;
import com.shykad.yunke.sdk.manager.ShykadManager;
import com.shykad.yunke.sdk.utils.BaseRealVisibleUtil;
import com.shykad.yunke.sdk.utils.RealVisibleInterface;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/**
 * Create by wanghong.he on 2019/3/6.
 * description：
 */
public class YunKeBanner extends RelativeLayout implements YunKeViewPager.AutoPlayDelegate, ViewPager.OnPageChangeListener {

    private static final int RMP = RelativeLayout.LayoutParams.MATCH_PARENT;

    private static final int RWC = RelativeLayout.LayoutParams.WRAP_CONTENT;

    private static final int LWC = LinearLayout.LayoutParams.WRAP_CONTENT;

    private static final int NO_PLACEHOLDER_DRAWABLE = -1;

    private static final int VEL_THRESHOLD = 400;

    private YunKeViewPager mViewPager;

    private List<View> mHackyViews;

    private List<View> mViews;

    private List<String> mTips;

    private LinearLayout mPointRealContainerLl;

    private TextView mTipTv;

    private boolean mAutoPlayAble = true;

    private int mAutoPlayInterval = 3000;

    private int mPageChangeDuration = 800;

    private int mPointGravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;

    private int mPointLeftRightMargin;

    private int mPointTopBottomMargin;

    private int mPointContainerLeftRightPadding;

    private int mTipTextSize;

    private int mTipTextColor = Color.WHITE;

    private int mPointDrawableResId = R.drawable.yunke_banner_selector_point_solid;

    private Drawable mPointContainerBackgroundDrawable;

    private AutoPlayTask mAutoPlayTask;

    private int mPageScrollPosition;

    private float mPageScrollPositionOffset;

    private TransitionEffect mTransitionEffect;

    private ImageView mPlaceholderIv;

    private ImageView.ScaleType mScaleType = ImageView.ScaleType.CENTER_CROP;

    private int mPlaceholderDrawableResId = NO_PLACEHOLDER_DRAWABLE;

    private int mCancelDrawableResId = NO_PLACEHOLDER_DRAWABLE;

    private List<? extends Object> mModels;

    private BannerClickCallBack bannerClickCallBack;

    private BannerDataCallBack bannerDataCallBack;

    private int mOverScrollMode = OVER_SCROLL_NEVER;

    private ViewPager.OnPageChangeListener mOnPageChangeListener;

    private boolean mIsNumberIndicator = false;

    private boolean mIsShowCancel = true;

    private TextView mNumberIndicatorTv;

    private ImageView mCancelIv;

    private TextView mAdTv;

    private int mNumberIndicatorTextColor = Color.WHITE;

    private int mNumberIndicatorTextSize;

    private Drawable mNumberIndicatorBackground;

    private Drawable mCancelBackground;

    private boolean mIsNeedShowIndicatorOnOnlyOnePage;

    private int mContentBottomMargin;

    private int mCancelTopMargin;

    private int mCancelRightMargin;

    private float mAspectRatio;

    private boolean mAllowUserScrollable = true;

    private View mSkipView;

    private View mEnterView;

    private GuideDelegate mGuideDelegate;

    private boolean mIsFirstInvisible = true;

    private String adId;

    private Context context;

    private static final ImageView.ScaleType[] sScaleTypeArray = {

            ImageView.ScaleType.MATRIX,

            ImageView.ScaleType.FIT_XY,

            ImageView.ScaleType.FIT_START,

            ImageView.ScaleType.FIT_CENTER,

            ImageView.ScaleType.FIT_END,

            ImageView.ScaleType.CENTER,

            ImageView.ScaleType.CENTER_CROP,

            ImageView.ScaleType.CENTER_INSIDE

    };


    private YunKeOnNoDoubleClickListener mGuideOnNoDoubleClickListener = new YunKeOnNoDoubleClickListener() {

        @Override

        public void onNoDoubleClick(View v) {

            if (mGuideDelegate != null) {

                mGuideDelegate.onClickEnterOrSkip();

            }

        }

    };

    public YunKeBanner(Context context) {

        this(context, null, 0);
        this.context = context;
    }


    public YunKeBanner(Context context, AttributeSet attrs) {

        this(context, attrs, 0);

    }


    public YunKeBanner(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);

        initDefaultAttrs(context);

        initCustomAttrs(context, attrs);

        initView(context);

    }

    private void initDefaultAttrs(Context context) {

        mAutoPlayTask = new AutoPlayTask(this);

        mCancelRightMargin = YunKeBannerUtil.dp2px(context, 6);

        mCancelTopMargin = YunKeBannerUtil.dp2px(context, 6);

        mPointLeftRightMargin = YunKeBannerUtil.dp2px(context, 3);

        mPointTopBottomMargin = YunKeBannerUtil.dp2px(context, 6);

        mPointContainerLeftRightPadding = YunKeBannerUtil.dp2px(context, 10);

        mTipTextSize = YunKeBannerUtil.sp2px(context, 10);

        mPointContainerBackgroundDrawable = new ColorDrawable(Color.parseColor("#00aaaaaa"));

        mTransitionEffect = TransitionEffect.Default;

        mNumberIndicatorTextSize = YunKeBannerUtil.sp2px(context, 10);


        mContentBottomMargin = 0;

        mAspectRatio = 0;

        mCancelTopMargin = YunKeBannerUtil.dp2px(context, 6);

        mCancelRightMargin = YunKeBannerUtil.dp2px(context, 6);

    }


    private void initCustomAttrs(Context context, AttributeSet attrs) {

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.YunKeBanner);

        final int N = typedArray.getIndexCount();

        for (int i = 0; i < N; i++) {

            initCustomAttr(typedArray.getIndex(i), typedArray);

        }

        typedArray.recycle();

    }


    private void initCustomAttr(int attr, TypedArray typedArray) {

        if (attr == R.styleable.YunKeBanner_banner_pointDrawable) {

            mPointDrawableResId = typedArray.getResourceId(attr, R.drawable.yunke_banner_selector_point_solid);

        } else if (attr == R.styleable.YunKeBanner_banner_pointContainerBackground) {

            mPointContainerBackgroundDrawable = typedArray.getDrawable(attr);

        } else if (attr == R.styleable.YunKeBanner_banner_pointLeftRightMargin) {

            mPointLeftRightMargin = typedArray.getDimensionPixelSize(attr, mPointLeftRightMargin);

        } else if (attr == R.styleable.YunKeBanner_banner_pointContainerLeftRightPadding) {

            mPointContainerLeftRightPadding = typedArray.getDimensionPixelSize(attr, mPointContainerLeftRightPadding);

        } else if (attr == R.styleable.YunKeBanner_banner_pointTopBottomMargin) {

            mPointTopBottomMargin = typedArray.getDimensionPixelSize(attr, mPointTopBottomMargin);

        } else if (attr == R.styleable.YunKeBanner_banner_indicatorGravity) {

            mPointGravity = typedArray.getInt(attr, mPointGravity);

        } else if (attr == R.styleable.YunKeBanner_banner_pointAutoPlayAble) {

            mAutoPlayAble = typedArray.getBoolean(attr, mAutoPlayAble);

        } else if (attr == R.styleable.YunKeBanner_banner_pointAutoPlayInterval) {

            mAutoPlayInterval = typedArray.getInteger(attr, mAutoPlayInterval);

        } else if (attr == R.styleable.YunKeBanner_banner_pageChangeDuration) {

            mPageChangeDuration = typedArray.getInteger(attr, mPageChangeDuration);

        } else if (attr == R.styleable.YunKeBanner_banner_transitionEffect) {

            int ordinal = typedArray.getInt(attr, TransitionEffect.Accordion.ordinal());

            mTransitionEffect = TransitionEffect.values()[ordinal];

        } else if (attr == R.styleable.YunKeBanner_banner_tipTextColor) {

            mTipTextColor = typedArray.getColor(attr, mTipTextColor);

        } else if (attr == R.styleable.YunKeBanner_banner_tipTextSize) {

            mTipTextSize = typedArray.getDimensionPixelSize(attr, mTipTextSize);

        } else if (attr == R.styleable.YunKeBanner_banner_placeholderDrawable) {

            mPlaceholderDrawableResId = typedArray.getResourceId(attr, mPlaceholderDrawableResId);

        } else if (attr == R.styleable.YunKeBanner_banner_cancelRightMargin) {

            mCancelDrawableResId = typedArray.getResourceId(attr, mCancelDrawableResId);

        } else if (attr == R.styleable.YunKeBanner_banner_isNumberIndicator) {

            mIsNumberIndicator = typedArray.getBoolean(attr, mIsNumberIndicator);

        } else if (attr == R.styleable.YunKeBanner_banner_numberIndicatorTextColor) {

            mNumberIndicatorTextColor = typedArray.getColor(attr, mNumberIndicatorTextColor);

        } else if (attr == R.styleable.YunKeBanner_banner_numberIndicatorTextSize) {

            mNumberIndicatorTextSize = typedArray.getDimensionPixelSize(attr, mNumberIndicatorTextSize);

        } else if (attr == R.styleable.YunKeBanner_banner_numberIndicatorBackground) {

            mNumberIndicatorBackground = typedArray.getDrawable(attr);

        } else if (attr == R.styleable.YunKeBanner_banner_isNeedShowIndicatorOnOnlyOnePage) {

            mIsNeedShowIndicatorOnOnlyOnePage = typedArray.getBoolean(attr, mIsNeedShowIndicatorOnOnlyOnePage);

        } else if (attr == R.styleable.YunKeBanner_banner_contentBottomMargin) {

            mContentBottomMargin = typedArray.getDimensionPixelSize(attr, mContentBottomMargin);

        } else if (attr == R.styleable.YunKeBanner_banner_aspectRatio) {

            mAspectRatio = typedArray.getFloat(attr, mAspectRatio);

        } else if (attr == R.styleable.YunKeBanner_android_scaleType) {

            final int index = typedArray.getInt(attr, -1);

            if (index >= 0 && index < sScaleTypeArray.length) {

                mScaleType = sScaleTypeArray[index];

            }

        } else if (attr == R.styleable.YunKeBanner_banner_cancelBackground){

            mCancelBackground = typedArray.getDrawable(attr);

        } else if (attr == R.styleable.YunKeBanner_banner_cancelTopMargin){

            mCancelTopMargin = typedArray.getDimensionPixelSize(attr, mCancelTopMargin);

        } else if (attr == R.styleable.YunKeBanner_banner_cancelRightMargin){

            mCancelRightMargin = typedArray.getDimensionPixelSize(attr, mCancelRightMargin);
        }

    }


    private void initView(Context context) {

        RelativeLayout pointContainerRl = new RelativeLayout(context);

        if (Build.VERSION.SDK_INT >= 16) {

            pointContainerRl.setBackground(mPointContainerBackgroundDrawable);

        } else {

            pointContainerRl.setBackgroundDrawable(mPointContainerBackgroundDrawable);

        }

        pointContainerRl.setPadding(mPointContainerLeftRightPadding, mPointTopBottomMargin, mPointContainerLeftRightPadding, mPointTopBottomMargin);

        RelativeLayout.LayoutParams pointContainerLp = new RelativeLayout.LayoutParams(RMP, RWC);

        // 处理圆点在顶部还是底部

        if ((mPointGravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.TOP) {

            pointContainerLp.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        } else {

            pointContainerLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        }

        addView(pointContainerRl, pointContainerLp);

        RelativeLayout.LayoutParams indicatorLp = new RelativeLayout.LayoutParams(RWC, RWC);

        indicatorLp.addRule(CENTER_VERTICAL);

        if (mIsNumberIndicator) {

            mNumberIndicatorTv = new TextView(context);

            mNumberIndicatorTv.setId(R.id.banner_indicatorId);

            mNumberIndicatorTv.setGravity(Gravity.CENTER_VERTICAL);

            mNumberIndicatorTv.setSingleLine(true);

            mNumberIndicatorTv.setEllipsize(TextUtils.TruncateAt.END);

            mNumberIndicatorTv.setTextColor(mNumberIndicatorTextColor);

            mNumberIndicatorTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mNumberIndicatorTextSize);

            mNumberIndicatorTv.setVisibility(View.INVISIBLE);

            if (mNumberIndicatorBackground != null) {

                if (Build.VERSION.SDK_INT >= 16) {

                    mNumberIndicatorTv.setBackground(mNumberIndicatorBackground);

                } else {

                    mNumberIndicatorTv.setBackgroundDrawable(mNumberIndicatorBackground);

                }

            }

            pointContainerRl.addView(mNumberIndicatorTv, indicatorLp);

        } else {

            mPointRealContainerLl = new LinearLayout(context);

            mPointRealContainerLl.setId(R.id.banner_indicatorId);

            mPointRealContainerLl.setOrientation(LinearLayout.HORIZONTAL);

            mPointRealContainerLl.setGravity(Gravity.CENTER_VERTICAL);

            pointContainerRl.addView(mPointRealContainerLl, indicatorLp);

        }


        RelativeLayout.LayoutParams tipLp = new RelativeLayout.LayoutParams(RMP, RWC);

        tipLp.addRule(CENTER_VERTICAL);

        mTipTv = new TextView(context);

        mTipTv.setGravity(Gravity.CENTER_VERTICAL);

        mTipTv.setSingleLine(true);

        mTipTv.setEllipsize(TextUtils.TruncateAt.END);

        mTipTv.setTextColor(mTipTextColor);

        mTipTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTipTextSize);

        pointContainerRl.addView(mTipTv, tipLp);


        int horizontalGravity = mPointGravity & Gravity.HORIZONTAL_GRAVITY_MASK;

        // 处理圆点在左边、右边还是水平居中

        if (horizontalGravity == Gravity.LEFT) {

            indicatorLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

            tipLp.addRule(RelativeLayout.RIGHT_OF, R.id.banner_indicatorId);

            mTipTv.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);

        } else if (horizontalGravity == Gravity.RIGHT) {

            indicatorLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            tipLp.addRule(RelativeLayout.LEFT_OF, R.id.banner_indicatorId);

        } else {

            indicatorLp.addRule(RelativeLayout.CENTER_HORIZONTAL);

            tipLp.addRule(RelativeLayout.LEFT_OF, R.id.banner_indicatorId);

        }


        showPlaceholder();

        initCancel(context);

        initAdTextView(context);
    }

    public void initCancel(Context context){
        //处理关闭按钮
        RelativeLayout.LayoutParams closeLp = new RelativeLayout.LayoutParams(60, 60);

        closeLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mCancelIv = new ImageView(context);
        mCancelIv.setImageDrawable(context.getResources().getDrawable(R.drawable.yunke_dislike_icon));
        closeLp.setMargins(0, mCancelTopMargin, mCancelRightMargin, 0);

        addView(mCancelIv,closeLp);

        //关闭按钮 end
    }

    public void initAdTextView(Context context){
        RelativeLayout.LayoutParams viewLp = new RelativeLayout.LayoutParams(RWC, RMP);

        viewLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.ALIGN_PARENT_BOTTOM);
        mAdTv = new TextView(context);
        mAdTv.setSingleLine(true);
        mAdTv.setGravity(Gravity.BOTTOM);
        mAdTv.setEllipsize(TextUtils.TruncateAt.END);
        mAdTv.setText(context.getResources().getString(R.string.ad_view_desc));
        mAdTv.setTextColor(mNumberIndicatorTextColor);
        mAdTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mNumberIndicatorTextSize);
        mAdTv.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.yunke_common_black_view));
        mAdTv.setVisibility(View.VISIBLE);
        viewLp.setMargins(0, 0, mCancelRightMargin, mCancelRightMargin);

        addView(mAdTv,viewLp);
    }

    public YunKeBanner setPointContainerBackground(Drawable mPointContainerBackgroundDrawable){
        RelativeLayout pointContainerRl = new RelativeLayout(context);

        if (Build.VERSION.SDK_INT >= 16) {

            pointContainerRl.setBackground(mPointContainerBackgroundDrawable);

        } else {

            pointContainerRl.setBackgroundDrawable(mPointContainerBackgroundDrawable);

        }

        pointContainerRl.setPadding(mPointContainerLeftRightPadding, mPointTopBottomMargin, mPointContainerLeftRightPadding, mPointTopBottomMargin);
        return this;
    }

    public YunKeBanner showPlaceholder() {

        if (mPlaceholderIv == null && mPlaceholderDrawableResId != NO_PLACEHOLDER_DRAWABLE) {

            mPlaceholderIv = YunKeBannerUtil.getItemImageView(getContext(), mPlaceholderDrawableResId, new YunKeLocalImageSize(720, 360, 640, 320), mScaleType);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RMP, RMP);

            layoutParams.setMargins(0, 0, 0, mContentBottomMargin);

            addView(mPlaceholderIv, layoutParams);

        }
        return this;
    }


    /**
     * 设置页码切换过程的时间长度
     *
     * @param duration 页码切换过程的时间长度
     */

    public YunKeBanner setPageChangeDuration(int duration) {

        if (duration >= 0 && duration <= 2*1000) {

            mPageChangeDuration = duration;

            if (mViewPager != null) {

                mViewPager.setPageChangeDuration(duration);

            }

        }
        return this;
    }


    /**
     * 设置是否开启自动轮播，需要在 lanchBanner 方法之前调用，并且调了该方法后必须再调用一次 lanchBanner 方法
     * <p>
     * 例如根据图片当图片数量大于 1 时开启自动轮播，等于 1 时不开启自动轮播
     */

    public YunKeBanner setAutoPlayAble(boolean autoPlayAble) {

        mAutoPlayAble = autoPlayAble;


        stopAutoPlay();


        if (mViewPager != null && mViewPager.getAdapter() != null) {

            mViewPager.getAdapter().notifyDataSetChanged();

        }
        return this;
    }


    /**
     * 设置自动轮播的时间间隔
     */

    public YunKeBanner setAutoPlayInterval(int autoPlayInterval) {

        mAutoPlayInterval = autoPlayInterval;
        return this;
    }


    /**
     * 设置每一页的控件、数据模型和文案
     *
     * @param views  每一页的控件集合
     * @param models 每一页的数据模型集合
     * @param tips   每一页的提示文案集合
     */

    public YunKeBanner lanchBanner(List<View> views, List<? extends Object> models, List<String> tips) {

        if (YunKeBannerUtil.isCollectionEmpty(views)) {

            mAutoPlayAble = false;

            views = new ArrayList<>();

            models = new ArrayList<>();

            tips = new ArrayList<>();

        }

        if (mAutoPlayAble && views.size() < 3 && mHackyViews == null) {

            mAutoPlayAble = false;

        }


        mModels = models;

        mViews = views;

        mTips = tips;


        initIndicator();

        initViewPager();

        removePlaceholder();
        return this;
    }


    /**
     * 设置布局资源id、数据模型和文案
     *
     * @param layoutResId item布局文件资源id
     * @param models      每一页的数据模型集合
     * @param tips        每一页的提示文案集合
     */

    public YunKeBanner lanchBanner(@LayoutRes int layoutResId, List<? extends Object> models, List<String> tips) {

        mViews = new ArrayList<>();

        if (models == null) {

            models = new ArrayList<>();

            tips = new ArrayList<>();

        }

        for (int i = 0; i < models.size(); i++) {

            mViews.add(View.inflate(getContext(), layoutResId, null));

        }

        if (mAutoPlayAble && mViews.size() < 3) {

            mHackyViews = new ArrayList<>(mViews);

            mHackyViews.add(View.inflate(getContext(), layoutResId, null));

            if (mHackyViews.size() == 2) {

                mHackyViews.add(View.inflate(getContext(), layoutResId, null));

            }

        }

        lanchBanner(mViews, models, tips);
        return this;
    }


    /**
     * 设置数据模型和文案，布局资源默认为 ImageView
     *
     * @param models 每一页的数据模型集合
     * @param tips   每一页的提示文案集合
     */

    public YunKeBanner lanchBanner(List<? extends Object> models,List<String> tips) {
        lanchBanner(R.layout.yunke_banner_item_image, models, tips);
        return this;
    }


    /**
     * 设置每一页的控件集合，主要针对引导页的情况
     *
     * @param views 每一页的控件集合
     */

    public YunKeBanner lanchBanner(List<View> views) {
        lanchBanner(views, null, null);
        return this;
    }


    /**
     * 设置每一页图片的资源 id，主要针对引导页的情况
     *
     * @param localImageSize 内存优化，Bitmap 的宽高在 maxWidth maxHeight 和 minWidth minHeight 之间，传 null 的话默认为 720, 1280, 320, 640
     * @param scaleType      图片缩放模式，传 null 的话默认为 CENTER_CROP
     * @param resIds         每一页图片资源 id
     */

    public YunKeBanner lanchBanner(@Nullable YunKeLocalImageSize localImageSize, @Nullable ImageView.ScaleType scaleType, @DrawableRes int... resIds) {
        if (localImageSize == null) {

            localImageSize = new YunKeLocalImageSize(720, 1280, 320, 640);

        }

        if (scaleType != null) {

            mScaleType = scaleType;

        }

        List<View> views = new ArrayList<>();

        for (int resId : resIds) {

            views.add(YunKeBannerUtil.getItemImageView(getContext(), resId, localImageSize, mScaleType));

        }

        lanchBanner(views);
        return this;
    }

    /**
     * 设置是否展示关闭按钮
     * @param mIsShowCancel 标识
     * @return
     */
    public YunKeBanner setShowCancel(boolean mIsShowCancel){
        this.mIsShowCancel = mIsShowCancel;
        if (mIsShowCancel){
            mCancelIv.setVisibility(VISIBLE);
        }else {
            mCancelIv.setVisibility(GONE);
        }
        return this;
    }

    /**
     * 设置广告id
     * @param adId 广告id
     * @return
     */
    public YunKeBanner setAdId(String adId){
        this.adId = adId;
        return this;
    }

    /**
     * 设置是否允许用户手指滑动
     *
     * @param allowUserScrollable true表示允许跟随用户触摸滑动，false反之
     */

    public YunKeBanner setAllowUserScrollable(boolean allowUserScrollable) {

        mAllowUserScrollable = allowUserScrollable;

        if (mViewPager != null) {

            mViewPager.setAllowUserScrollable(mAllowUserScrollable);

        }
        return this;
    }


    /**
     * 添加ViewPager滚动监听器
     */

    public YunKeBanner setOnPageChangeListener(ViewPager.OnPageChangeListener onPageChangeListener) {

        mOnPageChangeListener = onPageChangeListener;
        return this;
    }


    /**
     * 设置进入按钮和跳过按钮控件资源 id，需要开发者自己处理这两个按钮的点击事件
     *
     * @param enterResId 进入按钮控件
     * @param skipResId  跳过按钮控件
     */

    public YunKeBanner setEnterSkipViewId(int enterResId, int skipResId) {

        if (enterResId != 0) {

            mEnterView = ((Activity) getContext()).findViewById(enterResId);

        }

        if (skipResId != 0) {

            mSkipView = ((Activity) getContext()).findViewById(skipResId);

        }
        return this;
    }


    /**
     * 设置进入按钮和跳过按钮控件资源 id 及其点击事件监听器
     * <p>
     * 如果进入按钮和跳过按钮有一个不存在的话就传 0
     * <p>
     * 在 BGABanner 里已经帮开发者处理了重复点击事件
     * <p>
     * 在 BGABanner 里已经帮开发者处理了「跳过按钮」和「进入按钮」的显示与隐藏
     *
     * @param enterResId    进入按钮控件资源 id，没有的话就传 0
     * @param skipResId     跳过按钮控件资源 id，没有的话就传 0
     * @param guideDelegate 引导页「进入」和「跳过」按钮点击事件监听器
     */

    public YunKeBanner setEnterSkipViewIdAndDelegate(int enterResId, int skipResId, GuideDelegate guideDelegate) {

        if (guideDelegate != null) {

            mGuideDelegate = guideDelegate;

            if (enterResId != 0) {

                mEnterView = ((Activity) getContext()).findViewById(enterResId);

                mEnterView.setOnClickListener(mGuideOnNoDoubleClickListener);

            }

            if (skipResId != 0) {

                mSkipView = ((Activity) getContext()).findViewById(skipResId);

                mSkipView.setOnClickListener(mGuideOnNoDoubleClickListener);

            }

        }
        return this;
    }

    private YunKeCancleCallBack cancleCallBack;

    public YunKeBanner setCancelClick(YunKeCancleCallBack cancleCallBack){
        mCancelIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cancleCallBack!=null){
                    cancleCallBack.cancelClick();
                }
            }
        });
        return this;
    }

    public interface YunKeCancleCallBack{
        void cancelClick();
    }
    /**
     * 获取当前选中界面索引
     */
    public int getCurrentItem() {

        if (mViewPager == null || YunKeBannerUtil.isCollectionEmpty(mViews)) {

            return -1;

        } else {

            return mViewPager.getCurrentItem() % mViews.size();

        }

    }


    /**
     * 获取广告页面总个数
     */

    public int getItemCount() {

        return mViews == null ? 0 : mViews.size();

    }


    public List<? extends View> getViews() {

        return mViews;

    }


    public <VT extends View> VT getItemView(int position) {

        return mViews == null ? null : (VT) mViews.get(position);

    }


    public ImageView getItemImageView(int position) {

        return getItemView(position);

    }


    public List<String> getTips() {

        return mTips;

    }


    public YunKeViewPager getViewPager() {

        return mViewPager;

    }


    public void setOverScrollMode(int overScrollMode) {

        mOverScrollMode = overScrollMode;

        if (mViewPager != null) {

            mViewPager.setOverScrollMode(mOverScrollMode);

        }

    }


    private void initIndicator() {

        if (mPointRealContainerLl != null) {

            mPointRealContainerLl.removeAllViews();


            if (mIsNeedShowIndicatorOnOnlyOnePage || (!mIsNeedShowIndicatorOnOnlyOnePage && mViews.size() > 1)) {

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LWC, LWC);

                lp.setMargins(mPointLeftRightMargin, 0, mPointLeftRightMargin, 0);

                ImageView imageView;

                for (int i = 0; i < mViews.size(); i++) {

                    imageView = new ImageView(getContext());

                    imageView.setLayoutParams(lp);

                    imageView.setImageResource(mPointDrawableResId);

                    mPointRealContainerLl.addView(imageView);

                }

            }

        }

        if (mNumberIndicatorTv != null) {

            if (mIsNeedShowIndicatorOnOnlyOnePage || (!mIsNeedShowIndicatorOnOnlyOnePage && mViews.size() > 1)) {

                mNumberIndicatorTv.setVisibility(View.VISIBLE);

            } else {

                mNumberIndicatorTv.setVisibility(View.INVISIBLE);

            }

        }

    }


    private void initViewPager() {

        if (mViewPager != null && this.equals(mViewPager.getParent())) {

            this.removeView(mViewPager);

            mViewPager = null;

        }


        mViewPager = new YunKeViewPager(getContext());

        mViewPager.setOffscreenPageLimit(1);

        mViewPager.setAdapter(new PageAdapter());

        mViewPager.addOnPageChangeListener(this);

        mViewPager.setOverScrollMode(mOverScrollMode);

        mViewPager.setAllowUserScrollable(mAllowUserScrollable);

        mViewPager.setPageTransformer(true, YunKePageTransformer.getPageTransformer(mTransitionEffect));

        setPageChangeDuration(mPageChangeDuration);


        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RMP, RMP);

        layoutParams.setMargins(0, 0, 0, mContentBottomMargin);

        addView(mViewPager, 0, layoutParams);


        if (mAutoPlayAble) {

            mViewPager.setAutoPlayDelegate(this);


            int zeroItem = Integer.MAX_VALUE / 2 - (Integer.MAX_VALUE / 2) % mViews.size();

            mViewPager.setCurrentItem(zeroItem);


            startAutoPlay();

        } else {

            switchToPoint(0);

        }

    }


    public void removePlaceholder() {

        if (mPlaceholderIv != null && this.equals(mPlaceholderIv.getParent())) {

            removeView(mPlaceholderIv);

            mPlaceholderIv = null;

        }

    }


    /**
     * 设置宽高比例，如果大于 0，则会根据宽度来计算高度，否则使用 android:layout_height 指定的高度
     */

    public void setAspectRatio(float aspectRatio) {

        mAspectRatio = aspectRatio;

        requestLayout();

    }


    @Override

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (mAspectRatio > 0) {

            int width = MeasureSpec.getSize(widthMeasureSpec);

            int height = (int) (width / mAspectRatio);

            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        }


        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }


    @Override

    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (mAutoPlayAble) {

            switch (ev.getAction()) {

                case MotionEvent.ACTION_DOWN:

                    stopAutoPlay();

                    break;

                case MotionEvent.ACTION_UP:

                case MotionEvent.ACTION_CANCEL:

                    startAutoPlay();

                    break;

            }

        }

        return super.dispatchTouchEvent(ev);

    }


    /**
     * 设置当只有一页数据时是否显示指示器
     */

    public void setIsNeedShowIndicatorOnOnlyOnePage(boolean isNeedShowIndicatorOnOnlyOnePage) {

        mIsNeedShowIndicatorOnOnlyOnePage = isNeedShowIndicatorOnOnlyOnePage;

    }


    public void setCurrentItem(int item) {

        if (mViewPager == null || mViews == null || item > getItemCount() - 1) {

            return;

        }


        if (mAutoPlayAble) {

            int realCurrentItem = mViewPager.getCurrentItem();

            int currentItem = realCurrentItem % mViews.size();

            int offset = item - currentItem;


            // 这里要使用循环递增或递减设置，否则会ANR

            if (offset < 0) {

                for (int i = -1; i >= offset; i--) {

                    mViewPager.setCurrentItem(realCurrentItem + i, false);

                }

            } else if (offset > 0) {

                for (int i = 1; i <= offset; i++) {

                    mViewPager.setCurrentItem(realCurrentItem + i, false);

                }

            }


            startAutoPlay();

        } else {

            mViewPager.setCurrentItem(item, false);

        }

    }


    @Override

    protected void onVisibilityChanged(View changedView, int visibility) {

        super.onVisibilityChanged(changedView, visibility);

        if (visibility == VISIBLE) {

            startAutoPlay();

        } else if (visibility == INVISIBLE || visibility == GONE) {

            onInvisibleToUser();

        }

    }


    private void onInvisibleToUser() {

        stopAutoPlay();


        // 处理 RecyclerView 中从对用户不可见变为可见时卡顿的问题

        if (!mIsFirstInvisible && mAutoPlayAble && mViewPager != null && getItemCount() > 0 && mPageScrollPositionOffset != 0) {

            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);

            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);

        }

        mIsFirstInvisible = false;

    }


    @Override

    protected void onDetachedFromWindow() {

        super.onDetachedFromWindow();

        onInvisibleToUser();

    }


    @Override

    protected void onAttachedToWindow() {

        super.onAttachedToWindow();

        startAutoPlay();

    }


    public void startAutoPlay() {

        stopAutoPlay();

        if (mAutoPlayAble) {

            postDelayed(mAutoPlayTask, mAutoPlayInterval);

        }

    }


    public void stopAutoPlay() {

        if (mAutoPlayTask != null) {

            removeCallbacks(mAutoPlayTask);

        }

    }


    private void switchToPoint(int newCurrentPoint) {

        if (mTipTv != null) {

            if (mTips == null || mTips.size() < 1 || newCurrentPoint >= mTips.size()) {

                mTipTv.setVisibility(View.GONE);

            } else {

                mTipTv.setVisibility(View.VISIBLE);

                mTipTv.setText(mTips.get(newCurrentPoint));

            }

        }


        if (mPointRealContainerLl != null) {

            if (mViews != null && mViews.size() > 0 && newCurrentPoint < mViews.size() && ((mIsNeedShowIndicatorOnOnlyOnePage || (

                    !mIsNeedShowIndicatorOnOnlyOnePage && mViews.size() > 1)))) {

                mPointRealContainerLl.setVisibility(View.VISIBLE);

                for (int i = 0; i < mPointRealContainerLl.getChildCount(); i++) {

                    mPointRealContainerLl.getChildAt(i).setSelected(i == newCurrentPoint);

                    // 处理指示器选中和未选中状态图片尺寸不相等

                    mPointRealContainerLl.getChildAt(i).requestLayout();

                }

            } else {

                mPointRealContainerLl.setVisibility(View.GONE);

            }

        }


        if (mNumberIndicatorTv != null) {

            if (mViews != null && mViews.size() > 0 && newCurrentPoint < mViews.size() && ((mIsNeedShowIndicatorOnOnlyOnePage || (

                    !mIsNeedShowIndicatorOnOnlyOnePage && mViews.size() > 1)))) {

                mNumberIndicatorTv.setVisibility(View.VISIBLE);

                mNumberIndicatorTv.setText((newCurrentPoint + 1) + "/" + mViews.size());

            } else {

                mNumberIndicatorTv.setVisibility(View.GONE);

            }

        }

    }


    /**
     * 设置页面切换换动画
     */

    public void setTransitionEffect(TransitionEffect effect) {

        mTransitionEffect = effect;

        if (mViewPager != null) {

            initViewPager();

            if (mHackyViews == null) {

                YunKeBannerUtil.resetPageTransformer(mViews);

            } else {

                YunKeBannerUtil.resetPageTransformer(mHackyViews);

            }

        }

    }


    /**
     * 设置自定义页面切换动画
     */

    public void setPageTransformer(ViewPager.PageTransformer transformer) {

        if (transformer != null && mViewPager != null) {

            mViewPager.setPageTransformer(true, transformer);

        }

    }


    /**
     * 切换到下一页
     */

    private void switchToNextPage() {

        if (mViewPager != null) {

            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);

        }

    }


    @Override

    public void handleAutoPlayActionUpOrCancel(float xVelocity) {

        if (mViewPager != null) {

            if (mPageScrollPosition < mViewPager.getCurrentItem()) {

                // 往右滑

                if (xVelocity > VEL_THRESHOLD || (mPageScrollPositionOffset < 0.7f && xVelocity > -VEL_THRESHOLD)) {

                    mViewPager.setBannerCurrentItemInternal(mPageScrollPosition, true);

                } else {

                    mViewPager.setBannerCurrentItemInternal(mPageScrollPosition + 1, true);

                }

            } else {

                // 往左滑

                if (xVelocity < -VEL_THRESHOLD || (mPageScrollPositionOffset > 0.3f && xVelocity < VEL_THRESHOLD)) {

                    mViewPager.setBannerCurrentItemInternal(mPageScrollPosition + 1, true);

                } else {

                    mViewPager.setBannerCurrentItemInternal(mPageScrollPosition, true);

                }

            }

        }

    }


    @Override

    public void onPageSelected(int position) {

        position = position % mViews.size();

        switchToPoint(position);


        if (mOnPageChangeListener != null) {

            mOnPageChangeListener.onPageSelected(position);

        }

    }


    @Override

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        handleGuideViewVisibility(position, positionOffset);


        mPageScrollPosition = position;

        mPageScrollPositionOffset = positionOffset;


        if (mTipTv != null) {

            if (YunKeBannerUtil.isCollectionNotEmpty(mTips)) {

                mTipTv.setVisibility(View.VISIBLE);


                int leftPosition = position % mTips.size();

                int rightPosition = (position + 1) % mTips.size();

                if (rightPosition < mTips.size() && leftPosition < mTips.size()) {

                    if (positionOffset > 0.5) {

                        mTipTv.setText(mTips.get(rightPosition));

                        ViewCompat.setAlpha(mTipTv, positionOffset);

                    } else {

                        ViewCompat.setAlpha(mTipTv, 1 - positionOffset);

                        mTipTv.setText(mTips.get(leftPosition));

                    }

                }

            } else {

                mTipTv.setVisibility(View.GONE);

            }

        }


        if (mOnPageChangeListener != null) {

            mOnPageChangeListener.onPageScrolled(position % mViews.size(), positionOffset, positionOffsetPixels);

        }

    }


    @Override

    public void onPageScrollStateChanged(int state) {

        if (mOnPageChangeListener != null) {

            mOnPageChangeListener.onPageScrollStateChanged(state);

        }

    }


    private void handleGuideViewVisibility(int position, float positionOffset) {

        if (mEnterView == null && mSkipView == null) {

            return;

        }


        if (position == getItemCount() - 2) {

            if (mEnterView != null) {

                ViewCompat.setAlpha(mEnterView, positionOffset);

            }

            if (mSkipView != null) {

                ViewCompat.setAlpha(mSkipView, 1.0f - positionOffset);

            }


            if (positionOffset > 0.5f) {

                if (mEnterView != null) {

                    mEnterView.setVisibility(View.VISIBLE);

                }

                if (mSkipView != null) {

                    mSkipView.setVisibility(View.GONE);

                }

            } else {

                if (mEnterView != null) {

                    mEnterView.setVisibility(View.GONE);

                }

                if (mSkipView != null) {

                    mSkipView.setVisibility(View.VISIBLE);

                }

            }

        } else if (position == getItemCount() - 1) {

            if (mSkipView != null) {

                mSkipView.setVisibility(View.GONE);

            }

            if (mEnterView != null) {

                mEnterView.setVisibility(View.VISIBLE);

                ViewCompat.setAlpha(mEnterView, 1.0f);

            }

        } else {

            if (mSkipView != null) {

                mSkipView.setVisibility(View.VISIBLE);

                ViewCompat.setAlpha(mSkipView, 1.0f);

            }

            if (mEnterView != null) {

                mEnterView.setVisibility(View.GONE);

            }

        }

    }



    public YunKeBanner setBannerClickListener(BannerClickCallBack clickCallBack) {

        bannerClickCallBack = clickCallBack;
        return this;
    }


    public YunKeBanner setBannerDataListener(BannerDataCallBack dataCallBack) {

        bannerDataCallBack = dataCallBack;
        return this;
    }


    private class PageAdapter extends PagerAdapter {


        @Override

        public int getCount() {

            return mViews == null ? 0 : (mAutoPlayAble ? Integer.MAX_VALUE : mViews.size());

        }


        @Override

        public Object instantiateItem(ViewGroup container, int position) {

            if (YunKeBannerUtil.isCollectionEmpty(mViews)) {

                return null;

            }


            final int finalPosition = position % mViews.size();


            View view;

            if (mHackyViews == null) {

                view = mViews.get(finalPosition);

            } else {

                view = mHackyViews.get(position % mHackyViews.size());

            }


            if (bannerClickCallBack != null) {

                view.setOnClickListener(new YunKeOnNoDoubleClickListener() {

                    @Override

                    public void onNoDoubleClick(View view) {
                        clickAdTask(adId,view);
                    }

                });

            }


            if (bannerDataCallBack != null) {
                if (BaseRealVisibleUtil.getInstance(context).isViewCovered(view)){
                    showAdTask(adId,finalPosition,view);//广告展示成功并回调调用
                }

            }


            ViewParent viewParent = view.getParent();

            if (viewParent != null) {

                ((ViewGroup) viewParent).removeView(view);

            }


            container.addView(view);

            return view;

        }


        @Override

        public void destroyItem(ViewGroup container, int position, Object object) {

        }


        @Override

        public boolean isViewFromObject(View view, Object object) {

            return view == object;

        }


        @Override

        public int getItemPosition(Object object) {

            return POSITION_NONE;

        }

    }

    /**
     * 展示广告
     * @param slotid
     * @param finalPosition
     * @param view
     */
    private void showAdTask(String slotid,int finalPosition,View view){
        ShykadManager.INIT_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                (new Handler(Looper.getMainLooper())).post(new Runnable() {

                    @Override
                    public void run() {// TODO: 2019/3/7 埋点

                        YunKeEngine.getInstance().yunkeFeedAd(slotid, HttpConfig.AD_SHOW_YUNKE, new YunKeEngine.YunKeFeedCallBack() {
                            @Override
                            public void feedAdSuccess(String response) {
                                if (YunKeBannerUtil.isIndexNotOutOfBounds(finalPosition, mModels)) {

                                    bannerDataCallBack.onBannerData(YunKeBanner.this, view, mModels.get(finalPosition), finalPosition);

                                } else if (YunKeBannerUtil.isCollectionEmpty(mModels)) {

                                    bannerDataCallBack.onBannerData(YunKeBanner.this, view, null, finalPosition);

                                }
                            }

                            @Override
                            public void feedAdFailed(String err) {
                                bannerDataCallBack.onBannerDataError(err);
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * 点击广告
     * @param slotid
     * @param view
     */
    private void clickAdTask(String slotid,View view) {
        ShykadManager.INIT_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                (new Handler(Looper.getMainLooper())).post(new Runnable() {

                    @Override
                    public void run() {// TODO: 2019/3/7 埋点

                        YunKeEngine.getInstance().yunkeFeedAd(slotid, HttpConfig.AD_CLICK_YUNKE, new YunKeEngine.YunKeFeedCallBack() {
                            @Override
                            public void feedAdSuccess(String response) {
                                int currentPosition = mViewPager.getCurrentItem() % mViews.size();

                                if (YunKeBannerUtil.isIndexNotOutOfBounds(currentPosition, mModels)) {
                                    bannerClickCallBack.onBannerItemClick(YunKeBanner.this, view, mModels.get(currentPosition), currentPosition);

                                } else if (YunKeBannerUtil.isCollectionEmpty(mModels)) {
                                    bannerClickCallBack.onBannerItemClick(YunKeBanner.this, view, null, currentPosition);
                                }
                            }

                            @Override
                            public void feedAdFailed(String err) {
                                bannerClickCallBack.onBannerItemError(err);
                            }
                        });
                    }
                });
            }
        });


    }

    private static class AutoPlayTask implements Runnable {

        private final WeakReference<YunKeBanner> mBanner;


        private AutoPlayTask(YunKeBanner banner) {

            mBanner = new WeakReference<>(banner);

        }


        @Override

        public void run() {

            YunKeBanner banner = mBanner.get();

            if (banner != null) {

                banner.switchToNextPage();

                banner.startAutoPlay();

            }

        }

    }


    /**
     * item 点击事件监听器，在 YunKeBanner 里已经帮开发者处理了防止重复点击事件
     *
     * @param <V> item 视图类型，如果没有在 lanchBanner 方法里指定自定义的 item 布局资源文件的话，这里的 V 就是 ImageView
     * @param <M> item 数据模型
     */

    public interface BannerClickCallBack<V extends View, M> {

        void onBannerItemClick(YunKeBanner banner, V itemView, @Nullable M model, int position);

        void onBannerItemError(String error);

    }


    /**
     * 适配器，在 fillBannerItem 方法中填充数据，加载网络图片等
     *
     * @param <V> item 视图类型，如果没有在 lanchBanner 方法里指定自定义的 item 布局资源文件的话，这里的 V 就是 ImageView
     * @param <M> item 数据模型
     */

    public interface BannerDataCallBack<V extends View, M> {

        void onBannerData(YunKeBanner banner, V itemView, @Nullable M model, int position);
        void onBannerDataError(String error);
    }


    /**
     * 引导页「进入」和「跳过」按钮点击事件监听器，在 BGABanner 里已经帮开发者处理了防止重复点击事件
     */

    public interface GuideDelegate {

        void onClickEnterOrSkip();

    }

}