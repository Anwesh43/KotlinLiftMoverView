package ui.anwesome.com.liftview

/**
 * Created by anweshmishra on 22/04/18.
 */

import android.app.Activity
import android.view.MotionEvent
import android.view.View
import android.graphics.*
import android.content.Context

class LiftView (ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var prevScale : Float = 0f, var dir : Float = 0f, var j : Int = 0) {

        val scales : Array<Float> = arrayOf(0f, 0f, 0f)

        fun update(stopcb : (Float) -> Unit) {
            scales[j] += dir * 0.1f
            if (Math.abs(scales[j] - prevScale) > 1) {
                scales[j] = prevScale + dir
                j += dir.toInt()
                if (j == scales.size || j == -1) {
                    j -= dir.toInt()
                    dir = 0f
                    prevScale = scales[j]
                    stopcb(prevScale)
                }

            }
        }

        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                startcb()
            }
        }
    }

    data class Animator (var view : View, var animated : Boolean = false) {

        fun animate(updatecb : () -> Unit) {
            if (animated) {
                updatecb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                }
                catch (ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class LiftMover (var i : Int, private val state : State = State()) {

        fun draw(canvas : Canvas, paint : Paint) {
            val w : Float = canvas.width.toFloat()
            val h : Float = canvas.height.toFloat()
            val rSize : Float = Math.min(w, h)/10
            paint.strokeWidth = Math.min(w, h)/ 60
            paint.strokeCap = Paint.Cap.ROUND
            canvas.save()
            canvas.translate(w/2, 0f)
            paint.style = Paint.Style.STROKE
            paint.color = Color.parseColor("#00BCD4")
            val yUpdated : Float = (h - 2 * rSize) * (1 - this.state.scales[1])
            canvas.save()
            canvas.translate(0f, yUpdated)
            canvas.drawRect(-rSize/2, 0f, rSize/2, 2 * rSize, paint)
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#0097A7")
            val barW1 : Float = (rSize/2) * this.state.scales[0]
            val barW2 : Float = -(rSize/2) * this.state.scales[2]
            val getX1 : (Int) -> Float = {i -> -(rSize/2)  + (rSize - (barW1 + barW2)) * i }
            val getW : () -> Float = { (barW1 + barW2) }
            for (i in 0..1) {
                canvas.save()
                canvas.translate(getX1(i), 0f)
                canvas.drawRect(0f, 0f, getW(), 2 * rSize, paint)
                canvas.restore()
            }
            canvas.restore()
            canvas.drawLine(0f, 0f, 0f, yUpdated, paint)
            canvas.restore()
        }

        fun update(stopcb : (Float) -> Unit) {
            state.update(stopcb)
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }
    }

    data class Renderer (var view : LiftView) {

        private val liftMover : LiftMover = LiftMover(0)

        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            liftMover.draw(canvas, paint)
            animator.animate {
                liftMover.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            liftMover.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : LiftView {
            val view : LiftView = LiftView(activity)
            activity.setContentView(view)
            return view
        }
    }
}