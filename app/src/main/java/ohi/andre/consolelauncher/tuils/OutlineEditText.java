package ohi.andre.consolelauncher.tuils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

public class OutlineEditText extends androidx.appcompat.widget.AppCompatEditText {

    private int drawTimes = -1;
    private final Paint idleCursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean idleCursorVisible = false;
    private int idleCursorColor = 0xffffffff;

    public OutlineEditText(Context context) {
        super(context);
    }

    public OutlineEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OutlineEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void draw(Canvas canvas) {
        if(drawTimes == -1) {
            drawTimes = getTag() == null ? 1 : OutlineTextView.redrawTimes;
        }

        for(int c = 0; c < drawTimes; c++) super.draw(canvas);
        drawIdleCursor(canvas);
    }

    public void setIdleCursorVisible(boolean visible) {
        if (idleCursorVisible == visible) {
            return;
        }
        idleCursorVisible = visible;
        invalidate();
    }

    public void setIdleCursorColor(int color) {
        if (idleCursorColor == color) {
            return;
        }
        idleCursorColor = color;
        invalidate();
    }

    private void drawIdleCursor(Canvas canvas) {
        if (!idleCursorVisible || isCursorVisible()) {
            return;
        }

        CharSequence text = getText();
        int end = text == null ? 0 : text.length();
        float x = getCompoundPaddingLeft() - getScrollX();
        if (end > 0) {
            x += getPaint().measureText(text, 0, end);
        }

        float density = getResources().getDisplayMetrics().density;
        int lineHeight = Math.max(getLineHeight(), (int) (18f * density));
        int centerY = getHeight() / 2;
        int top = Math.max(getPaddingTop(), centerY - (lineHeight / 2));
        int bottom = Math.min(getHeight() - getPaddingBottom(), centerY + (lineHeight / 2));
        float width = Math.max(3f * density, 3f);

        idleCursorPaint.setColor(idleCursorColor);
        idleCursorPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(x, top, x + width, bottom, idleCursorPaint);
    }
}
