package com.capstone.sanggoroe.view.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.capstone.sanggoroe.R

class PasswordEditText : AppCompatEditText, View.OnTouchListener {
    private lateinit var eyeButtonImage: Drawable
    private lateinit var eyeOffButtonImage: Drawable


    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Menambahkan hint pada editText
        hint = context.getString(R.string.enterPassword)

        // Menambahkan text aligmnet pada editText
        textAlignment = View.TEXT_ALIGNMENT_VIEW_START

        // Mengatur tampilan background pada editText
//        setBackgroundResource(R.drawable.edittext_background)
    }

    private fun init() {
        // Menginisialisasi gambar eye button
        eyeButtonImage = ContextCompat.getDrawable(context, R.drawable.ic_eye_on) as Drawable
        eyeOffButtonImage =
            ContextCompat.getDrawable(context, R.drawable.ic_eye_off) as Drawable

        // Menambahkan aksi kepada eye button
        setOnTouchListener(this)

        // Menambahkan aksi ketika ada perubahan text akan memunculkan clear button
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Do nothing.
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty() && s.length < 8) {
                    error = context.getString(R.string.errorPassword)
                    hideEyeButton()
                } else {
                    showEyeButton()
                }
            }


            override fun afterTextChanged(s: Editable) {
                // Do nothing.
            }
        })

        setButtonDrawables(endOfTheText = eyeOffButtonImage)
        setOnTouchListener(this)
    }

    // Menampilkan eye button
    private fun showEyeButton() {
        setButtonDrawables(endOfTheText = eyeButtonImage)
    }

    // Menghilangkan eye button
    private fun hideEyeButton() {
        setButtonDrawables(endOfTheText = null)
    }

    // Mengkonfigurasi button
    private fun setButtonDrawables(startOfTheText: Drawable? = null, topOfTheText: Drawable? = null, endOfTheText: Drawable? = null, bottomOfTheText: Drawable? = null){
        // Sets the Drawables (if any) to appear to the left of,
        // above, to the right of, and below the text.
        setCompoundDrawablesWithIntrinsicBounds(startOfTheText, topOfTheText, endOfTheText, bottomOfTheText)
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (compoundDrawables[2] != null) {
            val eyeButtonStart: Float
            val eyeButtonEnd: Float
            var isEyeButtonClicked = false

            if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                eyeButtonEnd = (eyeButtonImage.intrinsicWidth + paddingStart).toFloat()
                when {
                    event.x < eyeButtonEnd -> isEyeButtonClicked = true
                }
            } else {
                eyeButtonStart = (width - paddingEnd - eyeButtonImage.intrinsicWidth).toFloat()
                when {
                    event.x > eyeButtonStart -> isEyeButtonClicked = true
                }
            }
            return if (isEyeButtonClicked) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        showPassword()
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        hidePassword()
                        true
                    }
                    else -> false
                }
            } else false
        }
        return false
    }

    // Method untuk menampilkan password
    private fun showPassword() {
        transformationMethod = HideReturnsTransformationMethod.getInstance()
        setButtonDrawables(endOfTheText = eyeButtonImage)
    }

    // Method untuk menyembunyikan password
    private fun hidePassword() {
        transformationMethod = PasswordTransformationMethod.getInstance()
        setButtonDrawables(endOfTheText = eyeOffButtonImage)
    }
}