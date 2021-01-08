package com.monolith.picturecanvasfragmenttest

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.random.Random

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    val GLOBAL = MyApp.getInstance()

    val handler = Handler()//メインスレッド処理用ハンドラ
    var moveview: MoveView? = null //キャンバスリフレッシュ用インスタンス保持変数
    var image: Bitmap?=null

    var _fabListener:OnFabListener?=null
    private lateinit var mScaleDetector: ScaleGestureDetector

    var scale: Float = 1F   //地図表示のスケール
    var posX: Int = 0    //地図表示の相対X座標
    var posY: Int = 0    //地図表示の絶対Y座標
    var logX: Int? = null  //タップ追従用X座標
    var logY: Int? = null  //タップ追従用Y座標

    var size: Rect? = null  //画面サイズ取得用

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_first,container,false)
        val layout=view.findViewById<ConstraintLayout>(R.id.constfrag)
        moveview=MoveView(this.activity)

        layout.addView(moveview)
        layout.setWillNotDraw(false)

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //画面サイズ取得
        size = Rect()
        activity?.window?.decorView?.getWindowVisibleDisplayFrame(size)


        HandlerDraw(moveview!!)

        view.findViewById<FloatingActionButton>(R.id.fab_gallery).setOnClickListener{
            _fabListener?.onClick_fab_gallery()
        }

        view.findViewById<FloatingActionButton>(R.id.fab_save).setOnClickListener{
            ImageCreate()
        }



        view.setOnTouchListener{_,event->
            onTouch(view,event)
            return@setOnTouchListener true
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            _fabListener = context as OnFabListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + "must implement OnArticleSelectedListener.")
        }


        //ピンチ処理関係
        mScaleDetector = ScaleGestureDetector(context,
            object : ScaleGestureDetector.OnScaleGestureListener {

                //スケール変更処理
                override fun onScale(detector: ScaleGestureDetector): Boolean {

                    scale *= detector.scaleFactor

                    /*posX += ((LogScale - (scale * 500 + scale)) / 2).toInt()
                    posY += ((LogScale - (scale * 500 + scale)) / 2).toInt()

                    posX += detector.focusX.toInt() - logX!!
                    posY += detector.focusY.toInt() - logY!!
                    logX = detector.focusX.toInt()
                    logY = detector.focusY.toInt()*/
                    return true
                }

                //ピンチ開始時処理
                override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                    logX = detector.focusX.toInt()
                    logY = detector.focusY.toInt()
                    return true
                }

                //ピンチ終了時処理
                override fun onScaleEnd(detector: ScaleGestureDetector) {
                }
            }
        )

    }

    override fun onDetach(){
        super.onDetach()
        _fabListener=null
    }

    //Activityにイベントを通知
    interface OnFabListener {
        fun onClick_fab_gallery()
    }

    fun onTouch(view:View,event: MotionEvent){
        //複数本タッチの場合はピンチ処理
        if (event.pointerCount > 1) {
            mScaleDetector.onTouchEvent(event)
        }

        //一本指タッチの場合は画面移動処理
        else {
            when {
                event.action == MotionEvent.ACTION_DOWN -> {
                    logX = event.x.toInt()
                    logY = event.y.toInt()
                }
                event.action == MotionEvent.ACTION_MOVE -> {
                    posX += event.x.toInt() - logX!!
                    posY += event.y.toInt() - logY!!
                    logX = event.x.toInt()
                    logY = event.y.toInt()
                }
            }
        }
    }


    fun ImageCreate(){
        //https://qiita.com/yuukiw00w/items/6fc0af6ac829b8a5af45を参考に作成
    }


    //描画関数　再描画用
    fun HandlerDraw(mv: MoveView) {
        handler.post(object : Runnable {
            override fun run() {
                //再描画
                mv.invalidate()
                handler.postDelayed(this, 25)
            }
        })
    }

    inner class MoveView : View {
        constructor(context: Context?) : super(context)
        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
        constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
        )

        @SuppressLint("DrawAllocation")
        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)
            val paint= Paint()
            val FramePaint=Paint()
            val BackPaint=Paint()

            FramePaint.strokeWidth=15f
            FramePaint.style = Paint.Style.STROKE
            FramePaint.color= Color.parseColor("#00FF00")

            BackPaint.color=Color.parseColor("#000000")
            BackPaint.alpha=128

            //フレーム及び背景に必要な座標を計算
            val FrameRect:RectF= RectF(size!!.width()/2-size!!.width()/3f,size!!.height()/2-size!!.width()/3f,size!!.width()/2+size!!.width()/3f,size!!.height()/2+size!!.width()/3f)
            val TopRect:RectF=RectF(0f,0f,size!!.width()*1f,size!!.height()/2-size!!.width()/3f)
            val BottomRect:RectF=RectF(0f,size!!.height()/2+size!!.width()/3f,size!!.width()*1f,size!!.height()*1f)
            val RightRect:RectF=RectF(size!!.width()/2+size!!.width()/3f,size!!.height()/2-size!!.width()/3f,size!!.width()*1f,size!!.height()/2+size!!.width()/3f)
            val LeftRect:RectF=RectF(0f,size!!.height()/2-size!!.width()/3f,size!!.width()/2-size!!.width()/3f,size!!.height()/2+size!!.width()/3f)

            //キャンバスのスケールを保存しておく
            canvas!!.save()
            canvas.scale(scale,scale)

            if(GLOBAL.ImageBuffer!=null){
                canvas.drawBitmap(GLOBAL.ImageBuffer!!,posX*1f,posY*1f,paint)
            }

            //キャンバスのスケールをもとに戻す
            canvas.restore()

            //フレーム及び半透明の背景処理
            canvas.drawRect(TopRect,BackPaint)
            canvas.drawRect(BottomRect,BackPaint)
            canvas.drawRect(RightRect,BackPaint)
            canvas.drawRect(LeftRect,BackPaint)
            canvas.drawRect(FrameRect,FramePaint)

        }
    }
}