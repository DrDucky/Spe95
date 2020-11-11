package com.pomplarg.spe95.utils

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.text.TextPaint
import com.pomplarg.spe95.R

/**
 * Generator for avatar; display the firstletter of the user into a Circle with material color background picked randomly
 */
class AvatarGenerator {
    companion object {
        lateinit var uiContext: Context
        var textSizeSp = 40
        var size = 200

        fun avatarImage(context: Context, name: String): BitmapDrawable {
            uiContext = context
            val width = size
            val height = size

            val label = firstCharacter(name)
            val textPaint = textPainter()
            val painter = painter()
            val areaRect = Rect(0, 0, width, width)

            painter.color = Color.TRANSPARENT

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawRect(areaRect, painter)

            painter.color = getRandomMaterialColor(context)

            val bounds = RectF(areaRect)
            bounds.right = textPaint.measureText(label, 0, 1)
            bounds.bottom = textPaint.descent() - textPaint.ascent()

            bounds.left += (areaRect.width() - bounds.right) / 2.0f
            bounds.top += (areaRect.height() - bounds.bottom) / 2.0f

            canvas.drawCircle(
                width.toFloat() / 2,
                height.toFloat() / 2,
                width.toFloat() / 2,
                painter
            )
            canvas.drawText(label, bounds.left, bounds.top - textPaint.ascent(), textPaint)
            return BitmapDrawable(uiContext.resources, bitmap)

        }

        private fun firstCharacter(name: String): String {
            return name.first().toString().toUpperCase()
        }

        private fun textPainter(): TextPaint {
            val textPaint = TextPaint()
            textPaint.textSize = textSizeSp * uiContext.resources.displayMetrics.scaledDensity
            textPaint.color = Color.WHITE
            return textPaint
        }

        private fun painter(): Paint {
            return Paint()
        }

        private fun getRandomMaterialColor(context: Context): Int {
            val returnColor: Int
            val colors: TypedArray = context.resources.obtainTypedArray(R.array.mdcolor_500)
            val index = (Math.random() * colors.length()).toInt()
            returnColor = colors.getColor(index, Color.GRAY)
            colors.recycle()
            return returnColor
        }
    }
}